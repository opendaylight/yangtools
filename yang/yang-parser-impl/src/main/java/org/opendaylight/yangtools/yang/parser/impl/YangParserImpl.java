/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.*;
import static org.opendaylight.yangtools.yang.parser.util.TypeUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.model.util.IdentityrefType;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.DeviationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ExtensionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.GroupingUtils;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;
import org.opendaylight.yangtools.yang.parser.util.ParserUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.opendaylight.yangtools.yang.validator.YangModelBasicValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public final class YangParserImpl implements YangModelParser {
    private static final Logger LOG = LoggerFactory.getLogger(YangParserImpl.class);

    private static final String FAIL_DEVIATION_TARGET = "Failed to find deviation target.";

    @Override
    public Set<Module> parseYangModels(final File yangFile, final File directory) {
        Preconditions.checkState(yangFile.exists(), yangFile + " does not exists");
        Preconditions.checkState(directory.exists(), directory + " does not exists");
        Preconditions.checkState(directory.isDirectory(), directory + " is not a directory");

        final String yangFileName = yangFile.getName();
        final String[] fileList = directory.list();
        Preconditions.checkNotNull(fileList, directory + " not found");

        FileInputStream yangFileStream = null;
        LinkedHashMap<InputStream, File> streamToFileMap = new LinkedHashMap<>();

        try {
            yangFileStream = new FileInputStream(yangFile);
            streamToFileMap.put(yangFileStream, yangFile);
        } catch(FileNotFoundException e) {
            LOG.warn("Exception while reading yang file: " + yangFile.getName(), e);
        }

        for (String fileName : fileList) {
            if (fileName.equals(yangFileName)) {
                continue;
            }
            File dependency = new File(directory, fileName);
            try {
                if (dependency.isFile()) {
                    streamToFileMap.put(new FileInputStream(dependency), dependency);
                }
            } catch(FileNotFoundException e) {
                LOG.warn("Exception while reading yang file: " + fileName, e);
            }
        }

        Map<InputStream, ModuleBuilder> parsedBuilders = parseModuleBuilders(
                new ArrayList<>(streamToFileMap.keySet()), new HashMap<ModuleBuilder, InputStream>());
        ModuleBuilder main = parsedBuilders.get(yangFileStream);

        List<ModuleBuilder> moduleBuilders = new ArrayList<>();
        moduleBuilders.add(main);
        filterImports(main, new ArrayList<>(parsedBuilders.values()), moduleBuilders);

        ModuleBuilder[] builders = new ModuleBuilder[moduleBuilders.size()];
        moduleBuilders.toArray(builders);

        // module dependency graph sorted
        List<ModuleBuilder> sorted = ModuleDependencySort.sort(builders);

        final LinkedHashMap<String, TreeMap<Date, ModuleBuilder>> modules = orderModules(sorted);
        return new LinkedHashSet<>(build(modules).values());
    }

    @Override
    public Set<Module> parseYangModels(final List<File> yangFiles) {
        return Sets.newLinkedHashSet(parseYangModelsMapped(yangFiles).values());
    }

    @Override
    public Set<Module> parseYangModels(final List<File> yangFiles, final SchemaContext context) {
        if (yangFiles != null) {
            final Map<InputStream, File> inputStreams = Maps.newHashMap();

            for (final File yangFile : yangFiles) {
                try {
                    inputStreams.put(new FileInputStream(yangFile), yangFile);
                } catch (FileNotFoundException e) {
                    LOG.warn("Exception while reading yang file: " + yangFile.getName(), e);
                }
            }

            Map<ModuleBuilder, InputStream> builderToStreamMap = Maps.newHashMap();
            final Map<String, TreeMap<Date, ModuleBuilder>> modules = resolveModuleBuilders(
                    Lists.newArrayList(inputStreams.keySet()), builderToStreamMap);

            for (InputStream is : inputStreams.keySet()) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.debug("Failed to close stream.");
                }
            }

            return new LinkedHashSet<Module>(buildWithContext(modules, context).values());
        }
        return Collections.emptySet();
    }

    @Override
    public Set<Module> parseYangModelsFromStreams(final List<InputStream> yangModelStreams) {
        return Sets.newHashSet(parseYangModelsFromStreamsMapped(yangModelStreams).values());
    }

    @Override
    public Set<Module> parseYangModelsFromStreams(final List<InputStream> yangModelStreams, SchemaContext context) {
        if (yangModelStreams != null) {
            Map<ModuleBuilder, InputStream> builderToStreamMap = Maps.newHashMap();
            final Map<String, TreeMap<Date, ModuleBuilder>> modules = resolveModuleBuildersWithContext(
                    yangModelStreams, builderToStreamMap, context);
            return new LinkedHashSet<Module>(buildWithContext(modules, context).values());
        }
        return Collections.emptySet();
    }

    @Override
    public Map<File, Module> parseYangModelsMapped(List<File> yangFiles) {
        if (yangFiles != null) {
            final Map<InputStream, File> inputStreams = Maps.newHashMap();

            for (final File yangFile : yangFiles) {
                try {
                    inputStreams.put(new FileInputStream(yangFile), yangFile);
                } catch (FileNotFoundException e) {
                    LOG.warn("Exception while reading yang file: " + yangFile.getName(), e);
                }
            }

            Map<ModuleBuilder, InputStream> builderToStreamMap = Maps.newHashMap();
            final Map<String, TreeMap<Date, ModuleBuilder>> modules = resolveModuleBuilders(
                    Lists.newArrayList(inputStreams.keySet()), builderToStreamMap);

            for (InputStream is : inputStreams.keySet()) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.debug("Failed to close stream.");
                }
            }

            Map<File, Module> retVal = Maps.newLinkedHashMap();
            Map<ModuleBuilder, Module> builderToModuleMap = build(modules);

            for (Entry<ModuleBuilder, Module> builderToModule : builderToModuleMap.entrySet()) {
                retVal.put(inputStreams.get(builderToStreamMap.get(builderToModule.getKey())),
                        builderToModule.getValue());
            }

            return retVal;
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<InputStream, Module> parseYangModelsFromStreamsMapped(final List<InputStream> yangModelStreams) {
        Map<ModuleBuilder, InputStream> builderToStreamMap = Maps.newHashMap();

        final Map<String, TreeMap<Date, ModuleBuilder>> modules = resolveModuleBuilders(yangModelStreams,
                builderToStreamMap);
        Map<InputStream, Module> retVal = Maps.newLinkedHashMap();
        Map<ModuleBuilder, Module> builderToModuleMap = build(modules);

        for (Entry<ModuleBuilder, Module> builderToModule : builderToModuleMap.entrySet()) {
            retVal.put(builderToStreamMap.get(builderToModule.getKey()), builderToModule.getValue());
        }
        return retVal;
    }

    @Override
    public SchemaContext resolveSchemaContext(final Set<Module> modules) {
        return new SchemaContextImpl(modules);
    }

    private Map<InputStream, ModuleBuilder> parseModuleBuilders(List<InputStream> inputStreams,
            Map<ModuleBuilder, InputStream> streamToBuilderMap) {

        final ParseTreeWalker walker = new ParseTreeWalker();
        final Map<InputStream, ParseTree> trees = parseStreams(inputStreams);
        final Map<InputStream, ModuleBuilder> builders = new LinkedHashMap<>();

        // validate yang
        new YangModelBasicValidator(walker).validate(new ArrayList<ParseTree>(trees.values()));

        YangParserListenerImpl yangModelParser;
        for(Map.Entry<InputStream, ParseTree> entry : trees.entrySet()) {
            yangModelParser = new YangParserListenerImpl();
            walker.walk(yangModelParser, entry.getValue());
            ModuleBuilder moduleBuilder = yangModelParser.getModuleBuilder();

            // We expect the order of trees and streams has to be the same
            streamToBuilderMap.put(moduleBuilder, entry.getKey());
            builders.put(entry.getKey(), moduleBuilder);
        }

        return builders;
    }

    private Map<String, TreeMap<Date, ModuleBuilder>> resolveModuleBuilders(final List<InputStream> yangFileStreams,
            Map<ModuleBuilder, InputStream> streamToBuilderMap) {
        return resolveModuleBuildersWithContext(yangFileStreams, streamToBuilderMap, null);
    }

    private Map<String, TreeMap<Date, ModuleBuilder>> resolveModuleBuildersWithContext(
            final List<InputStream> yangFileStreams, final Map<ModuleBuilder, InputStream> streamToBuilderMap,
            final SchemaContext context) {
        Map<InputStream, ModuleBuilder> parsedBuilders = parseModuleBuilders(yangFileStreams, streamToBuilderMap);
        ModuleBuilder[] builders = new ModuleBuilder[parsedBuilders.size()];
        final ModuleBuilder[] moduleBuilders = new ArrayList<>(parsedBuilders.values()).toArray(builders);

        // module dependency graph sorted
        List<ModuleBuilder> sorted;
        if (context == null) {
            sorted = ModuleDependencySort.sort(builders);
        } else {
            sorted = ModuleDependencySort.sortWithContext(context, builders);
        }

        return orderModules(sorted);
    }

    /**
     * Order modules by name and revision.
     *
     * @param modules
     *            modules to order
     * @return modules ordered by name and revision
     */
    private LinkedHashMap<String, TreeMap<Date, ModuleBuilder>> orderModules(List<ModuleBuilder> modules) {
        // LinkedHashMap must be used to preserve order
        LinkedHashMap<String, TreeMap<Date, ModuleBuilder>> result = new LinkedHashMap<>();
        for (final ModuleBuilder builder : modules) {
            if (builder == null) {
                continue;
            }
            final String builderName = builder.getName();
            Date builderRevision = builder.getRevision();
            if (builderRevision == null) {
                builderRevision = new Date(0L);
            }
            TreeMap<Date, ModuleBuilder> builderByRevision = result.get(builderName);
            if (builderByRevision == null) {
                builderByRevision = new TreeMap<Date, ModuleBuilder>();
            }
            builderByRevision.put(builderRevision, builder);
            result.put(builderName, builderByRevision);
        }
        return result;
    }

    private void filterImports(ModuleBuilder main, List<ModuleBuilder> other, List<ModuleBuilder> filtered) {
        for (ModuleImport mi : main.getModuleImports()) {
            for (ModuleBuilder builder : other) {
                if (mi.getModuleName().equals(builder.getModuleName())) {
                    if (mi.getRevision() == null) {
                        if (!filtered.contains(builder)) {
                            filtered.add(builder);
                            filterImports(builder, other, filtered);
                        }
                    } else {
                        if (mi.getRevision().equals(builder.getRevision())) {
                            if (!filtered.contains(builder)) {
                                filtered.add(builder);
                                filterImports(builder, other, filtered);
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<InputStream, ParseTree> parseStreams(final List<InputStream> yangStreams) {
        final Map<InputStream, ParseTree> trees = new HashMap<>();
        for (InputStream yangStream : yangStreams) {
            trees.put(yangStream, parseStream(yangStream));
        }
        return trees;
    }

    private ParseTree parseStream(final InputStream yangStream) {
        ParseTree result = null;
        try {
            final ANTLRInputStream input = new ANTLRInputStream(yangStream);
            final YangLexer lexer = new YangLexer(input);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final YangParser parser = new YangParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new YangErrorListener());

            result = parser.yang();
        } catch (IOException e) {
            LOG.warn("Exception while reading yang file: " + yangStream, e);
        }
        return result;
    }

    private Map<ModuleBuilder, Module> build(final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
        // fix unresolved nodes
        resolveAugmentsTargetPath(modules, null);
        resolveUsesTargetGrouping(modules, null);
        resolveDirtyNodes(modules);
        resolveAugments(modules);
        resolveUses(modules, false);
        resolvedUsesPostProcessing(modules, false);
        resolveDeviations(modules);

        // build
        final Map<ModuleBuilder, Module> result = new LinkedHashMap<ModuleBuilder, Module>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder moduleBuilder = childEntry.getValue();
                final Module module = moduleBuilder.build();
                result.put(moduleBuilder, module);
            }
        }
        return result;
    }

    private Map<ModuleBuilder, Module> buildWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        // fix unresolved nodes
        resolveAugmentsTargetPath(modules, context);
        resolveUsesTargetGrouping(modules, context);
        resolvedDirtyNodesWithContext(modules, context);
        resolveAugmentsWithContext(modules, context);
        resolveUses(modules, true);
        resolvedUsesPostProcessing(modules, true);
        resolveDeviationsWithContext(modules, context);

        // build
        final Map<ModuleBuilder, Module> result = new LinkedHashMap<ModuleBuilder, Module>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder moduleBuilder = childEntry.getValue();
                final Module module = moduleBuilder.build();
                result.put(moduleBuilder, module);
            }
        }
        return result;
    }

    private void resolveDirtyNodes(final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder module = childEntry.getValue();
                resolveUnknownNodes(modules, module);
                resolveIdentities(modules, module);
                resolveDirtyNodes(modules, module);
            }
        }
    }

    private void resolvedDirtyNodesWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder module = childEntry.getValue();
                resolveUnknownNodesWithContext(modules, module, context);
                resolveIdentitiesWithContext(modules, module, context);
                resolveDirtyNodesWithContext(modules, module, context);
            }
        }
    }

    /**
     * Search for dirty nodes (node which contains UnknownType) and resolve
     * unknown types.
     *
     * @param modules
     *            all available modules
     * @param module
     *            current module
     */
    private void resolveDirtyNodes(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final Set<TypeAwareBuilder> dirtyNodes = module.getDirtyNodes();
        if (!dirtyNodes.isEmpty()) {
            for (TypeAwareBuilder nodeToResolve : dirtyNodes) {
                if (nodeToResolve instanceof UnionTypeBuilder) {
                    // special handling for union types
                    resolveTypeUnion((UnionTypeBuilder) nodeToResolve, modules, module);
                } else if (nodeToResolve.getTypedef() instanceof IdentityrefTypeBuilder) {
                    // special handling for identityref types
                    IdentityrefTypeBuilder idref = (IdentityrefTypeBuilder) nodeToResolve.getTypedef();
                    IdentitySchemaNodeBuilder identity = findBaseIdentity(modules, module, idref.getBaseString(),
                            idref.getLine());
                    if (identity == null) {
                        throw new YangParseException(module.getName(), idref.getLine(), "Failed to find base identity");
                    }
                    idref.setBaseIdentity(identity);
                    nodeToResolve.setType(idref.build());
                } else {
                    resolveType(nodeToResolve, modules, module);
                }
            }
        }
    }

    private void resolveDirtyNodesWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, SchemaContext context) {
        final Set<TypeAwareBuilder> dirtyNodes = module.getDirtyNodes();
        if (!dirtyNodes.isEmpty()) {
            for (TypeAwareBuilder nodeToResolve : dirtyNodes) {
                if (nodeToResolve instanceof UnionTypeBuilder) {
                    // special handling for union types
                    resolveTypeUnionWithContext((UnionTypeBuilder) nodeToResolve, modules, module, context);
                } else if (nodeToResolve.getTypedef() instanceof IdentityrefTypeBuilder) {
                    // special handling for identityref types
                    IdentityrefTypeBuilder idref = (IdentityrefTypeBuilder) nodeToResolve.getTypedef();
                    IdentitySchemaNodeBuilder identity = findBaseIdentity(modules, module, idref.getBaseString(),
                            idref.getLine());
                    idref.setBaseIdentity(identity);
                    nodeToResolve.setType(idref.build());
                } else {
                    resolveTypeWithContext(nodeToResolve, modules, module, context);
                }
            }
        }
    }

    /**
     * Correct augment target path.
     *
     * @param modules
     *            all loaded modules
     */
    private void resolveAugmentsTargetPath(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            SchemaContext context) {
        // collect augments from all loaded modules
        final List<AugmentationSchemaBuilder> allAugments = new ArrayList<>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                allAugments.addAll(inner.getValue().getAllAugments());
            }
        }

        for (AugmentationSchemaBuilder augment : allAugments) {
            setCorrectAugmentTargetPath(modules, augment, context);
        }
    }

    private void setCorrectAugmentTargetPath(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final AugmentationSchemaBuilder augment, final SchemaContext context) {
        ModuleBuilder module = ParserUtils.getParentModule(augment);
        SchemaPath oldSchemaPath = augment.getTargetPath();
        List<QName> oldPath = oldSchemaPath.getPath();
        List<QName> newPath = new ArrayList<>();
        for (QName qn : oldPath) {
            URI ns = module.getNamespace();
            Date rev = module.getRevision();
            String pref = module.getPrefix();
            String localPrefix = qn.getPrefix();
            if (localPrefix != null && !("".equals(localPrefix))) {
                ModuleBuilder currentModule = ParserUtils.findModuleFromBuilders(modules, module, localPrefix,
                        augment.getLine());
                if (currentModule == null) {
                    Module m = ParserUtils.findModuleFromContext(context, module, localPrefix, augment.getLine());
                    if (m == null) {
                        throw new YangParseException(module.getName(), augment.getLine(), "Module with prefix "
                                + localPrefix + " not found.");
                    }
                    ns = m.getNamespace();
                    rev = m.getRevision();
                    pref = m.getPrefix();
                } else {
                    ns = currentModule.getNamespace();
                    rev = currentModule.getRevision();
                    pref = currentModule.getPrefix();
                }
            }
            newPath.add(new QName(ns, rev, pref, qn.getLocalName()));
        }
        augment.setTargetNodeSchemaPath(new SchemaPath(newPath, augment.getTargetPath().isAbsolute()));
    }

    /**
     * Go through all augment definitions and perform augmentation. It is
     * expected that modules are already sorted by their dependencies.
     *
     * @param modules
     *            all loaded modules
     */
    private void resolveAugments(final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
        // collect augments from all loaded modules
        final List<AugmentationSchemaBuilder> allAugments = new ArrayList<>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                allAugments.addAll(inner.getValue().getAllAugments());
            }
        }

        checkAugmentMandatoryNodes(allAugments);

        for (int i = 0; i < allAugments.size(); i++) {
            // pick one augment
            final AugmentationSchemaBuilder augment = allAugments.get(i);
            // create collection of others
            List<AugmentationSchemaBuilder> others = new ArrayList<>(allAugments);
            others.remove(augment);

            // try to resolve it
            boolean resolved = resolveAugment(modules, augment);
            // while not resolved
            int j = 0;
            while (!(resolved) && j < others.size()) {
                // try to resolve next augment
                resolveAugment(modules, others.get(j));
                // then try to resolve first again
                resolved = resolveAugment(modules, augment);
                j++;

            }

            if (!resolved) {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                        "Error in augment parsing: failed to find augment target");
            }
        }
    }

    /**
     * Check augments for mandatory nodes. If the target node is in another
     * module, then nodes added by the augmentation MUST NOT be mandatory nodes.
     * If mandatory node is found, throw an exception.
     *
     * @param augments
     *            augments to check
     */
    private void checkAugmentMandatoryNodes(Collection<AugmentationSchemaBuilder> augments) {
        for (AugmentationSchemaBuilder augment : augments) {
            String augmentPrefix = augment.getTargetPath().getPath().get(0).getPrefix();
            ModuleBuilder module = ParserUtils.getParentModule(augment);
            String modulePrefix = module.getPrefix();

            if (augmentPrefix == null || augmentPrefix.isEmpty() || augmentPrefix.equals(modulePrefix)) {
                continue;
            }

            for (DataSchemaNodeBuilder childNode : augment.getChildNodeBuilders()) {
                if (childNode.getConstraints().isMandatory()) {
                    throw new YangParseException(augment.getModuleName(), augment.getLine(),
                            "Error in augment parsing: cannot augment mandatory node");
                }
            }
        }
    }

    /**
     * Search for augment target and perform augmentation.
     *
     * @param modules
     *            all loaded modules
     * @param augment
     *            augment to resolve
     * @return true if target node found, false otherwise
     */
    private boolean resolveAugment(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final AugmentationSchemaBuilder augment) {
        if (augment.isResolved()) {
            return true;
        }

        int line = augment.getLine();
        ModuleBuilder module = getParentModule(augment);
        List<QName> path = augment.getTargetPath().getPath();
        Builder augmentParent = augment.getParent();

        Builder firstNodeParent;
        if (augmentParent instanceof ModuleBuilder) {
            // if augment is defined under module, parent of first node is
            // target module
            final QName firstNameInPath = path.get(0);
            String prefix = firstNameInPath.getPrefix();
            if (prefix == null) {
                prefix = module.getPrefix();
            }
            firstNodeParent = findModuleFromBuilders(modules, module, prefix, line);
        } else if (augmentParent instanceof UsesNodeBuilder) {
            firstNodeParent = augmentParent.getParent();
        } else {
            // augment can be defined only under module or uses
            throw new YangParseException(augment.getModuleName(), line,
                    "Failed to parse augment: Unresolved parent of augment: " + augmentParent);
        }

        return processAugmentation(augment, firstNodeParent, path);
    }

    /**
     * Go through all augment definitions and resolve them. This method works in
     * same way as {@link #resolveAugments(Map)} except that if target node is
     * not found in loaded modules, it search for target node in given context.
     *
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveAugmentsWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        // collect augments from all loaded modules
        final List<AugmentationSchemaBuilder> allAugments = new ArrayList<>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                allAugments.addAll(inner.getValue().getAllAugments());
            }
        }

        for (int i = 0; i < allAugments.size(); i++) {
            // pick augment from list
            final AugmentationSchemaBuilder augment = allAugments.get(i);
            // try to resolve it
            boolean resolved = resolveAugmentWithContext(modules, augment, context);
            // while not resolved
            int j = i + 1;
            while (!(resolved) && j < allAugments.size()) {
                // try to resolve next augment
                resolveAugmentWithContext(modules, allAugments.get(j), context);
                // then try to resolve first again
                resolved = resolveAugmentWithContext(modules, augment, context);
                j++;
            }

            if (!resolved) {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                        "Error in augment parsing: failed to find augment target");
            }
        }
    }

    /**
     * Search for augment target and perform augmentation.
     *
     * @param modules
     *            all loaded modules
     * @param augment
     *            augment to resolve
     * @param context
     *            SchemaContext containing already resolved modules
     * @return true if target node found, false otherwise
     */
    private boolean resolveAugmentWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final AugmentationSchemaBuilder augment, final SchemaContext context) {
        if (augment.isResolved()) {
            return true;
        }
        int line = augment.getLine();
        ModuleBuilder module = getParentModule(augment);
        List<QName> path = augment.getTargetNodeSchemaPath().getPath();
        final QName firstNameInPath = path.get(0);
        String prefix = firstNameInPath.getPrefix();
        if (prefix == null) {
            prefix = module.getPrefix();
        }
        Builder augmentParent = augment.getParent();
        Builder currentParent;
        if (augmentParent instanceof ModuleBuilder) {
            // if augment is defined under module, first parent is target module
            currentParent = findModuleFromBuilders(modules, module, prefix, line);
        } else if (augmentParent instanceof UsesNodeBuilder) {
            currentParent = augmentParent.getParent();
        } else {
            // augment can be defined only under module or uses
            throw new YangParseException(augment.getModuleName(), augment.getLine(),
                    "Error in augment parsing: Unresolved parent of augment: " + augmentParent);
        }

        if (currentParent == null) {
            return processAugmentationOnContext(augment, path, module, prefix, context);
        } else {
            return processAugmentation(augment, currentParent, path);
        }
    }

    /**
     * Go through identity statements defined in current module and resolve
     * their 'base' statement if present.
     *
     * @param modules
     *            all modules
     * @param module
     *            module being resolved
     */
    private void resolveIdentities(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final Set<IdentitySchemaNodeBuilder> identities = module.getIdentities();
        for (IdentitySchemaNodeBuilder identity : identities) {
            final String baseIdentityName = identity.getBaseIdentityName();
            final int line = identity.getLine();
            if (baseIdentityName != null) {
                IdentitySchemaNodeBuilder baseIdentity = findBaseIdentity(modules, module, baseIdentityName, line);
                if (baseIdentity == null) {
                    throw new YangParseException(module.getName(), identity.getLine(), "Failed to find base identity");
                } else {
                    identity.setBaseIdentity(baseIdentity);
                }
            }
        }
    }

    /**
     * Go through identity statements defined in current module and resolve
     * their 'base' statement. Method tries to find base identity in given
     * modules. If base identity is not found, method will search it in context.
     *
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveIdentitiesWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final SchemaContext context) {
        final Set<IdentitySchemaNodeBuilder> identities = module.getIdentities();
        for (IdentitySchemaNodeBuilder identity : identities) {
            final String baseIdentityName = identity.getBaseIdentityName();
            final int line = identity.getLine();
            if (baseIdentityName != null) {
                IdentitySchemaNodeBuilder baseIdentity = findBaseIdentity(modules, module, baseIdentityName, line);
                if (baseIdentity == null) {
                    IdentitySchemaNode baseId = findBaseIdentityFromContext(modules, module, baseIdentityName, line,
                            context);
                    identity.setBaseIdentity(baseId);
                } else {
                    identity.setBaseIdentity(baseIdentity);
                }
            }
        }
    }

    /**
     * Find and add reference of uses target grouping.
     *
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules or null if
     *            context is not available
     */
    private void resolveUsesTargetGrouping(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        final List<UsesNodeBuilder> allUses = new ArrayList<>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                allUses.addAll(inner.getValue().getAllUsesNodes());
            }
        }
        for (UsesNodeBuilder usesNode : allUses) {
            ModuleBuilder module = ParserUtils.getParentModule(usesNode);
            final GroupingBuilder targetGroupingBuilder = GroupingUtils.getTargetGroupingFromModules(usesNode, modules,
                    module);
            if (targetGroupingBuilder == null) {
                if (context == null) {
                    throw new YangParseException(module.getName(), usesNode.getLine(), "Referenced grouping '"
                            + usesNode.getGroupingPathAsString() + "' not found.");
                } else {
                    GroupingDefinition targetGroupingDefinition = GroupingUtils.getTargetGroupingFromContext(usesNode,
                            module, context);
                    usesNode.setGroupingDefinition(targetGroupingDefinition);
                }
            } else {
                usesNode.setGrouping(targetGroupingBuilder);
            }
        }
    }

    /**
     * Copy data from uses target. Augmentations have to be resolved already.
     *
     * @param modules
     *            all loaded modules
     * @param resolveWithContext
     *            boolean value which says whether
     *            {@link GroupingUtils#collectUsesDataFromContext(UsesNodeBuilder)
     *            collectUsesDataFromContext} should be used for processing of
     *            individual uses node.
     */
    private void resolveUses(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final boolean resolveWithContext) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                ModuleBuilder module = inner.getValue();
                boolean dataCollected = module.isAllUsesDataCollected();

                List<UsesNodeBuilder> usesNodes;
                while (!dataCollected) {
                    usesNodes = new ArrayList<>(module.getAllUsesNodes());
                    for (UsesNodeBuilder usesNode : usesNodes) {
                        if (!usesNode.isDataCollected()) {
                            if (resolveWithContext && usesNode.getGroupingBuilder() == null) {
                                GroupingUtils.collectUsesDataFromContext(usesNode);
                            } else {
                                GroupingUtils.collectUsesData(usesNode);
                            }
                        }
                    }
                    dataCollected = module.isAllUsesDataCollected();
                }
            }
        }
    }

    /**
     * Update uses parent and perform refinement.
     *
     * @param modules
     *            all loaded modules
     * @param resolveWithContext
     *            boolean value which says whether
     *            {@link GroupingUtils#collectUsesDataFromContext(UsesNodeBuilder)
     *            collectUsesDataFromContext} should be used for processing of
     *            individual uses node.
     */
    private void resolvedUsesPostProcessing(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final boolean resolveWithContext) {
        // new loop is must because in collecting data process new uses could
        // be created
        final List<UsesNodeBuilder> allModulesUses = new ArrayList<>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                allModulesUses.addAll(inner.getValue().getAllUsesNodes());
            }
        }

        for (UsesNodeBuilder usesNode : allModulesUses) {
            GroupingUtils.updateUsesParent(usesNode);
            GroupingUtils.performRefine(usesNode);
        }

        if (!resolveWithContext) {
            for (UsesNodeBuilder usesNode : allModulesUses) {
                if (usesNode.isCopy()) {
                    usesNode.getParent().getUsesNodes().remove(usesNode);
                }
            }
        }
    }

    private void resolveUnknownNodes(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        for (UnknownSchemaNodeBuilder usnb : module.getAllUnknownNodes()) {
            QName nodeType = usnb.getNodeType();
            try {
                ModuleBuilder dependentModule = findModuleFromBuilders(modules, module, nodeType.getPrefix(),
                        usnb.getLine());
                for (ExtensionBuilder extension : dependentModule.getExtensions()) {
                    if (extension.getQName().getLocalName().equals(nodeType.getLocalName())) {
                        usnb.setNodeType(extension.getQName());
                        usnb.setExtensionBuilder(extension);
                        break;
                    }
                }
            } catch (YangParseException e) {
                throw new YangParseException(module.getName(), usnb.getLine(), "Failed to resolve node " + usnb
                        + ": no such extension definition found.", e);
            }
        }
    }

    private void resolveUnknownNodesWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final SchemaContext context) {
        for (UnknownSchemaNodeBuilder usnb : module.getAllUnknownNodes()) {
            QName nodeType = usnb.getNodeType();
            try {
                ModuleBuilder dependentModuleBuilder = findModuleFromBuilders(modules, module,
                        nodeType.getPrefix(), usnb.getLine());

                if (dependentModuleBuilder == null) {
                    Module dependentModule = findModuleFromContext(context, module, nodeType.getPrefix(),
                            usnb.getLine());
                    for (ExtensionDefinition e : dependentModule.getExtensionSchemaNodes()) {
                        if (e.getQName().getLocalName().equals(nodeType.getLocalName())) {
                            usnb.setNodeType(new QName(e.getQName().getNamespace(), e.getQName().getRevision(),
                                    nodeType.getPrefix(), e.getQName().getLocalName()));
                            usnb.setExtensionDefinition(e);
                            break;
                        }
                    }
                } else {
                    for (ExtensionBuilder extension : dependentModuleBuilder.getExtensions()) {
                        if (extension.getQName().getLocalName().equals(nodeType.getLocalName())) {
                            usnb.setExtensionBuilder(extension);
                            break;
                        }
                    }
                }

            } catch (YangParseException e) {
                throw new YangParseException(module.getName(), usnb.getLine(), "Failed to resolve node " + usnb
                        + ": no such extension definition found.", e);
            }

        }
    }

    /**
     * Traverse through modules and resolve their deviation statements.
     *
     * @param modules
     *            all loaded modules
     */
    private void resolveDeviations(final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                ModuleBuilder b = inner.getValue();
                resolveDeviation(modules, b);
            }
        }
    }

    /**
     * Traverse through module and resolve its deviation statements.
     *
     * @param modules
     *            all loaded modules
     * @param module
     *            module in which resolve deviations
     */
    private void resolveDeviation(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        for (DeviationBuilder dev : module.getDeviations()) {
            int line = dev.getLine();
            SchemaPath targetPath = dev.getTargetPath();
            List<QName> path = targetPath.getPath();
            QName q0 = path.get(0);
            String prefix = q0.getPrefix();
            if (prefix == null) {
                prefix = module.getPrefix();
            }

            ModuleBuilder dependentModuleBuilder = findModuleFromBuilders(modules, module, prefix, line);
            processDeviation(dev, dependentModuleBuilder, path, module);
        }
    }

    /**
     * Traverse through modules and resolve their deviation statements with
     * given context.
     *
     * @param modules
     *            all loaded modules
     * @param context
     *            already resolved context
     */
    private void resolveDeviationsWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                ModuleBuilder b = inner.getValue();
                resolveDeviationWithContext(modules, b, context);
            }
        }
    }

    /**
     * Traverse through module and resolve its deviation statements with given
     * context.
     *
     * @param modules
     *            all loaded modules
     * @param module
     *            module in which resolve deviations
     * @param context
     *            already resolved context
     */
    private void resolveDeviationWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final SchemaContext context) {
        for (DeviationBuilder dev : module.getDeviations()) {
            int line = dev.getLine();
            SchemaPath targetPath = dev.getTargetPath();
            List<QName> path = targetPath.getPath();
            QName q0 = path.get(0);
            String prefix = q0.getPrefix();
            if (prefix == null) {
                prefix = module.getPrefix();
            }
            String name = null;

            ModuleBuilder dependentModuleBuilder = findModuleFromBuilders(modules, module, prefix, line);
            if (dependentModuleBuilder == null) {
                Module dependentModule = findModuleFromContext(context, module, prefix, line);
                Object currentParent = dependentModule;

                for (QName q : path) {
                    if (currentParent == null) {
                        throw new YangParseException(module.getName(), line, FAIL_DEVIATION_TARGET);
                    }
                    name = q.getLocalName();
                    if (currentParent instanceof DataNodeContainer) {
                        currentParent = ((DataNodeContainer) currentParent).getDataChildByName(name);
                    }
                }

                if (currentParent == null) {
                    throw new YangParseException(module.getName(), line, FAIL_DEVIATION_TARGET);
                }
                if (currentParent instanceof SchemaNode) {
                    dev.setTargetPath(((SchemaNode) currentParent).getPath());
                }

            } else {
                processDeviation(dev, dependentModuleBuilder, path, module);
            }
        }
    }

    /**
     * Correct deviation target path in deviation builder.
     *
     * @param dev
     *            deviation
     * @param dependentModuleBuilder
     *            module containing deviation target
     * @param path
     *            current deviation target path
     * @param module
     *            current module
     */
    private void processDeviation(final DeviationBuilder dev, final ModuleBuilder dependentModuleBuilder,
            final List<QName> path, final ModuleBuilder module) {
        final int line = dev.getLine();
        Builder currentParent = dependentModuleBuilder;

        for (QName q : path) {
            if (currentParent == null) {
                throw new YangParseException(module.getName(), line, FAIL_DEVIATION_TARGET);
            }
            String name = q.getLocalName();
            if (currentParent instanceof DataNodeContainerBuilder) {
                currentParent = ((DataNodeContainerBuilder) currentParent).getDataChildByName(name);
            }
        }

        if (!(currentParent instanceof SchemaNodeBuilder)) {
            throw new YangParseException(module.getName(), line, FAIL_DEVIATION_TARGET);
        }
        dev.setTargetPath(((SchemaNodeBuilder) currentParent).getPath());
    }

}
