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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.IdentityrefType;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingMember;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.DeviationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;
import org.opendaylight.yangtools.yang.parser.util.RefineHolder;
import org.opendaylight.yangtools.yang.parser.util.RefineUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.opendaylight.yangtools.yang.validator.YangModelBasicValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public final class YangParserImpl implements YangModelParser {
    private static final Logger LOG = LoggerFactory.getLogger(YangParserImpl.class);

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

    private ModuleBuilder[] parseModuleBuilders(List<InputStream> inputStreams,
            Map<ModuleBuilder, InputStream> streamToBuilderMap) {

        final ParseTreeWalker walker = new ParseTreeWalker();
        final List<ParseTree> trees = parseStreams(inputStreams);
        final ModuleBuilder[] builders = new ModuleBuilder[trees.size()];

        // validate yang
        new YangModelBasicValidator(walker).validate(trees);

        YangParserListenerImpl yangModelParser = null;
        for (int i = 0; i < trees.size(); i++) {
            yangModelParser = new YangParserListenerImpl();
            walker.walk(yangModelParser, trees.get(i));
            ModuleBuilder moduleBuilder = yangModelParser.getModuleBuilder();

            // We expect the order of trees and streams has to be the same
            streamToBuilderMap.put(moduleBuilder, inputStreams.get(i));
            builders[i] = moduleBuilder;
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
        final ModuleBuilder[] builders = parseModuleBuilders(yangFileStreams, streamToBuilderMap);

        // Linked Hash Map MUST be used because Linked Hash Map preserves ORDER
        // of items stored in map.
        final LinkedHashMap<String, TreeMap<Date, ModuleBuilder>> modules = new LinkedHashMap<String, TreeMap<Date, ModuleBuilder>>();

        // module dependency graph sorted
        List<ModuleBuilder> sorted = null;
        if (context == null) {
            sorted = ModuleDependencySort.sort(builders);
        } else {
            sorted = ModuleDependencySort.sortWithContext(context, builders);
        }

        for (final ModuleBuilder builder : sorted) {
            if (builder == null) {
                continue;
            }
            final String builderName = builder.getName();
            Date builderRevision = builder.getRevision();
            if (builderRevision == null) {
                builderRevision = new Date(0L);
            }
            TreeMap<Date, ModuleBuilder> builderByRevision = modules.get(builderName);
            if (builderByRevision == null) {
                builderByRevision = new TreeMap<Date, ModuleBuilder>();
            }
            builderByRevision.put(builderRevision, builder);
            modules.put(builderName, builderByRevision);
        }
        return modules;
    }

    private List<ParseTree> parseStreams(final List<InputStream> yangStreams) {
        final List<ParseTree> trees = new ArrayList<ParseTree>();
        for (InputStream yangStream : yangStreams) {
            trees.add(parseStream(yangStream));
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
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder moduleBuilder = childEntry.getValue();
                fixUnresolvedNodes(modules, moduleBuilder);
            }
        }
        resolveAugments(modules);
        resolveDeviations(modules);

        // build
        final Map<ModuleBuilder, Module> result = new LinkedHashMap<ModuleBuilder, Module>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            final Map<Date, Module> modulesByRevision = new HashMap<Date, Module>();
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder moduleBuilder = childEntry.getValue();
                final Module module = moduleBuilder.build();
                modulesByRevision.put(childEntry.getKey(), module);
                result.put(moduleBuilder, module);
            }
        }
        return result;
    }

    private Map<ModuleBuilder, Module> buildWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            SchemaContext context) {
        // fix unresolved nodes
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder moduleBuilder = childEntry.getValue();
                fixUnresolvedNodesWithContext(modules, moduleBuilder, context);
            }
        }
        resolveAugmentsWithContext(modules, context);
        resolveDeviationsWithContext(modules, context);

        // build
        final Map<ModuleBuilder, Module> result = new LinkedHashMap<ModuleBuilder, Module>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            final Map<Date, Module> modulesByRevision = new HashMap<Date, Module>();
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder moduleBuilder = childEntry.getValue();
                final Module module = moduleBuilder.build();
                modulesByRevision.put(childEntry.getKey(), module);
                result.put(moduleBuilder, module);
            }
        }
        return result;
    }

    private void fixUnresolvedNodes(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder builder) {
        resolveDirtyNodes(modules, builder);
        resolveIdentities(modules, builder);
        resolveUsesNodes(modules, builder);
        resolveUnknownNodes(modules, builder);
    }

    private void fixUnresolvedNodesWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder builder, final SchemaContext context) {
        resolveDirtyNodesWithContext(modules, builder, context);
        resolveIdentitiesWithContext(modules, builder, context);
        resolveUsesNodesWithContext(modules, builder, context);
        resolveUnknownNodesWithContext(modules, builder, context);
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
                    nodeToResolve.setType(new IdentityrefType(findFullQName(modules, module, idref), idref.getPath()));
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
                    nodeToResolve.setType(new IdentityrefType(findFullQName(modules, module, idref), idref.getPath()));
                } else {
                    resolveTypeWithContext(nodeToResolve, modules, module, context);
                }
            }
        }
    }

    /**
     * Go through all augment definitions and resolve them. It is expected that
     * modules are already sorted by their dependencies. This method also finds
     * augment target node and add child nodes to it.
     *
     * @param modules
     *            all available modules
     */
    private void resolveAugments(final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
        final List<ModuleBuilder> allModulesList = new ArrayList<ModuleBuilder>();
        final Set<ModuleBuilder> allModulesSet = new HashSet<ModuleBuilder>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                allModulesList.add(inner.getValue());
                allModulesSet.add(inner.getValue());
            }
        }

        for (int i = 0; i < allModulesList.size(); i++) {
            final ModuleBuilder module = allModulesList.get(i);
            // try to resolve augments in module
            resolveAugment(modules, module);
            // while all augments are not resolved
            final Iterator<ModuleBuilder> allModulesIterator = allModulesSet.iterator();
            while (!(module.getAugmentsResolved() == module.getAllAugments().size())) {
                ModuleBuilder nextModule = null;
                // try resolve other module augments
                try {
                    nextModule = allModulesIterator.next();
                    resolveAugment(modules, nextModule);
                } catch (NoSuchElementException e) {
                    throw new YangParseException("Failed to resolve augments in module '" + module.getName() + "'.", e);
                }
                // then try to resolve first module again
                resolveAugment(modules, module);
            }
        }
    }

    /**
     * Tries to resolve augments in given module. If augment target node is not
     * found, do nothing.
     *
     * @param modules
     *            all available modules
     * @param module
     *            current module
     */
    private void resolveAugment(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        if (module.getAugmentsResolved() < module.getAllAugments().size()) {
            for (AugmentationSchemaBuilder augmentBuilder : module.getAllAugments()) {

                if (!augmentBuilder.isResolved()) {
                    final SchemaPath augmentTargetSchemaPath = augmentBuilder.getTargetPath();
                    final List<QName> path = augmentTargetSchemaPath.getPath();

                    final QName qname = path.get(0);
                    String prefix = qname.getPrefix();
                    if (prefix == null) {
                        prefix = module.getPrefix();
                    }

                    final ModuleBuilder dependentModule = findDependentModuleBuilder(modules, module, prefix,
                            augmentBuilder.getLine());
                    processAugmentation(augmentBuilder, path, module, dependentModule);
                }

            }
        }
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
        final List<ModuleBuilder> allModulesList = new ArrayList<ModuleBuilder>();
        final Set<ModuleBuilder> allModulesSet = new HashSet<ModuleBuilder>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                allModulesList.add(inner.getValue());
                allModulesSet.add(inner.getValue());
            }
        }

        for (int i = 0; i < allModulesList.size(); i++) {
            final ModuleBuilder module = allModulesList.get(i);
            // try to resolve augments in module
            resolveAugmentWithContext(modules, module, context);
            // while all augments are not resolved
            final Iterator<ModuleBuilder> allModulesIterator = allModulesSet.iterator();
            while (!(module.getAugmentsResolved() == module.getAllAugments().size())) {
                ModuleBuilder nextModule = null;
                // try resolve other module augments
                try {
                    nextModule = allModulesIterator.next();
                    resolveAugmentWithContext(modules, nextModule, context);
                } catch (NoSuchElementException e) {
                    throw new YangParseException("Failed to resolve augments in module '" + module.getName() + "'.", e);
                }
                // then try to resolve first module again
                resolveAugmentWithContext(modules, module, context);
            }
        }
    }

    /**
     * Tries to resolve augments in given module. If augment target node is not
     * found, do nothing.
     *
     * @param modules
     *            all available modules
     * @param module
     *            current module
     */
    private void resolveAugmentWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final SchemaContext context) {
        if (module.getAugmentsResolved() < module.getAllAugments().size()) {

            for (AugmentationSchemaBuilder augmentBuilder : module.getAllAugments()) {
                final int line = augmentBuilder.getLine();

                if (!augmentBuilder.isResolved()) {
                    final List<QName> path = augmentBuilder.getTargetPath().getPath();
                    final QName qname = path.get(0);
                    String prefix = qname.getPrefix();
                    if (prefix == null) {
                        prefix = module.getPrefix();
                    }

                    // try to find augment target module in loaded modules...
                    final ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, module, prefix,
                            line);
                    if (dependentModuleBuilder == null) {
                        // perform augmentation on module from context and
                        // continue to next augment
                        processAugmentationOnContext(augmentBuilder, path, module, prefix, line, context);
                        continue;
                    } else {
                        processAugmentation(augmentBuilder, path, module, dependentModuleBuilder);
                    }
                }

            }
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
            if (baseIdentityName != null) {
                String baseIdentityPrefix = null;
                String baseIdentityLocalName = null;
                if (baseIdentityName.contains(":")) {
                    final String[] splitted = baseIdentityName.split(":");
                    baseIdentityPrefix = splitted[0];
                    baseIdentityLocalName = splitted[1];
                } else {
                    baseIdentityPrefix = module.getPrefix();
                    baseIdentityLocalName = baseIdentityName;
                }
                final ModuleBuilder dependentModule = findDependentModuleBuilder(modules, module, baseIdentityPrefix,
                        identity.getLine());

                final Set<IdentitySchemaNodeBuilder> dependentModuleIdentities = dependentModule.getIdentities();
                for (IdentitySchemaNodeBuilder idBuilder : dependentModuleIdentities) {
                    if (idBuilder.getQName().getLocalName().equals(baseIdentityLocalName)) {
                        identity.setBaseIdentity(idBuilder);
                    }
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
            if (baseIdentityName != null) {
                String baseIdentityPrefix = null;
                String baseIdentityLocalName = null;
                if (baseIdentityName.contains(":")) {
                    final String[] splitted = baseIdentityName.split(":");
                    baseIdentityPrefix = splitted[0];
                    baseIdentityLocalName = splitted[1];
                } else {
                    baseIdentityPrefix = module.getPrefix();
                    baseIdentityLocalName = baseIdentityName;
                }
                final ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, module,
                        baseIdentityPrefix, identity.getLine());

                if (dependentModuleBuilder == null) {
                    final Module dependentModule = findModuleFromContext(context, module, baseIdentityPrefix,
                            identity.getLine());
                    final Set<IdentitySchemaNode> dependentModuleIdentities = dependentModule.getIdentities();
                    for (IdentitySchemaNode idNode : dependentModuleIdentities) {
                        if (idNode.getQName().getLocalName().equals(baseIdentityLocalName)) {
                            identity.setBaseIdentity(idNode);
                        }
                    }
                } else {
                    final Set<IdentitySchemaNodeBuilder> dependentModuleIdentities = dependentModuleBuilder
                            .getIdentities();
                    for (IdentitySchemaNodeBuilder idBuilder : dependentModuleIdentities) {
                        if (idBuilder.getQName().getLocalName().equals(baseIdentityLocalName)) {
                            identity.setBaseIdentity(idBuilder);
                        }
                    }
                }
            }
        }
    }

    /**
     * Go through uses statements defined in current module and resolve their
     * refine statements.
     *
     * @param modules
     *            all modules
     * @param module
     *            module being resolved
     */
    private void resolveUsesNodes(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final List<UsesNodeBuilder> allModuleUses = module.getAllUsesNodes();
        for (UsesNodeBuilder usesNode : allModuleUses) {
            // process uses operation
            final GroupingBuilder targetGrouping = getTargetGroupingFromModules(usesNode, modules, module);
            usesNode.setGroupingPath(targetGrouping.getPath());
            processUsesNode(module, usesNode, targetGrouping);
            // refine
            for (RefineHolder refine : usesNode.getRefines()) {
                DataSchemaNodeBuilder nodeToRefine = null;
                for (DataSchemaNodeBuilder dsnb : usesNode.getTargetChildren()) {
                    if (refine.getName().equals(dsnb.getQName().getLocalName())) {
                        nodeToRefine = dsnb;
                        break;
                    }
                }
                if (nodeToRefine == null) {
                    throw new YangParseException(refine.getModuleName(), refine.getLine(), "Refine target node '"
                            + refine.getName() + "' not found");
                }
                if (nodeToRefine instanceof GroupingMember) {
                    ((GroupingMember) nodeToRefine).setAddedByUses(true);
                }
                RefineUtils.performRefine(nodeToRefine, refine);
                usesNode.addRefineNode(nodeToRefine);
            }
        }
        for (UsesNodeBuilder usesNode : allModuleUses) {
            final GroupingBuilder targetGrouping = getTargetGroupingFromModules(usesNode, modules, module);
            processUsesTarget(module, usesNode, targetGrouping);
        }
    }

    /**
     * Tries to search target grouping in given modules and resolve refine
     * nodes. If grouping is not found in modules, method tries to find it in
     * modules from context.
     *
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveUsesNodesWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final SchemaContext context) {
        final List<UsesNodeBuilder> moduleUses = module.getAllUsesNodes();
        for (UsesNodeBuilder usesNode : moduleUses) {
            final GroupingBuilder targetGroupingBuilder = getTargetGroupingFromModules(usesNode, modules, module);
            if (targetGroupingBuilder == null) {
                final GroupingDefinition targetGrouping = getTargetGroupingFromContext(usesNode, module, context);
                usesNode.setGroupingPath(targetGrouping.getPath());
                processUsesNode(usesNode, targetGrouping);
                for (RefineHolder refine : usesNode.getRefines()) {
                    DataSchemaNodeBuilder nodeToRefine = null;
                    for (DataSchemaNodeBuilder dsnb : usesNode.getTargetChildren()) {
                        if (refine.getName().equals(dsnb.getQName().getLocalName())) {
                            nodeToRefine = dsnb;
                            break;
                        }
                    }
                    if (nodeToRefine == null) {
                        throw new YangParseException(refine.getModuleName(), refine.getLine(), "Refine target node '"
                                + refine.getName() + "' not found");
                    }
                    if (nodeToRefine instanceof GroupingMember) {
                        ((GroupingMember) nodeToRefine).setAddedByUses(true);
                    }
                    RefineUtils.performRefine(nodeToRefine, refine);
                    usesNode.addRefineNode(nodeToRefine);
                }
            } else {
                usesNode.setGroupingPath(targetGroupingBuilder.getPath());
                processUsesNode(module, usesNode, targetGroupingBuilder);
                for (RefineHolder refine : usesNode.getRefines()) {
                    DataSchemaNodeBuilder nodeToRefine = null;
                    for (DataSchemaNodeBuilder dsnb : usesNode.getTargetChildren()) {
                        if (refine.getName().equals(dsnb.getQName().getLocalName())) {
                            nodeToRefine = dsnb;
                            break;
                        }
                    }
                    if (nodeToRefine == null) {
                        throw new YangParseException(refine.getModuleName(), refine.getLine(), "Refine target node '"
                                + refine.getName() + "' not found");
                    }
                    if (nodeToRefine instanceof GroupingMember) {
                        ((GroupingMember) nodeToRefine).setAddedByUses(true);
                    }
                    RefineUtils.performRefine(nodeToRefine, refine);
                    usesNode.addRefineNode(nodeToRefine);
                }
            }
        }
    }

    /**
     * Add nodes defined in target grouping to current context.
     *
     * @param module
     *            current module
     * @param usesNode
     * @param targetGrouping
     */
    private void processUsesNode(final ModuleBuilder module, final UsesNodeBuilder usesNode,
            final GroupingBuilder targetGrouping) {
        DataNodeContainerBuilder parent = usesNode.getParent();
        URI namespace = null;
        Date revision = null;
        String prefix = null;
        if (parent instanceof ModuleBuilder || parent instanceof AugmentationSchemaBuilder) {
            namespace = module.getNamespace();
            revision = module.getRevision();
            prefix = module.getPrefix();
        } else {
            QName parentQName = parent.getQName();
            namespace = parentQName.getNamespace();
            revision = parentQName.getRevision();
            prefix = parentQName.getPrefix();
        }
        SchemaPath parentPath = parent.getPath();
        // child nodes
        Set<DataSchemaNodeBuilder> newChildren = processUsesDataSchemaNode(usesNode,
                targetGrouping.getChildNodeBuilders(), parentPath, namespace, revision, prefix);
        usesNode.getTargetChildren().addAll(newChildren);
        // groupings
        Set<GroupingBuilder> newGroupings = processUsesGroupings(targetGrouping.getGroupingBuilders(), parentPath,
                namespace, revision, prefix);
        usesNode.getTargetGroupings().addAll(newGroupings);
        // typedefs
        Set<TypeDefinitionBuilder> newTypedefs = processUsesTypedefs(targetGrouping.getTypeDefinitionBuilders(),
                parentPath, namespace, revision, prefix);
        usesNode.getTargetTypedefs().addAll(newTypedefs);
        // unknown nodes
        List<UnknownSchemaNodeBuilder> newUnknownNodes = processUsesUnknownNodes(
                targetGrouping.getUnknownNodeBuilders(), parentPath, namespace, revision, prefix);
        usesNode.getTargetUnknownNodes().addAll(newUnknownNodes);
    }

    /**
     * Check if target grouping contains uses nodes and if it does, merge
     * current uses with them.
     *
     * @param module
     * @param usesNode
     * @param targetGrouping
     */
    private void processUsesTarget(final ModuleBuilder module, final UsesNodeBuilder usesNode,
            final GroupingBuilder targetGrouping) {
        DataNodeContainerBuilder parent = usesNode.getParent();
        URI namespace = null;
        Date revision = null;
        String prefix = null;
        if (parent instanceof ModuleBuilder) {
            ModuleBuilder m = (ModuleBuilder) parent;
            namespace = m.getNamespace();
            revision = m.getRevision();
            prefix = m.getPrefix();
        } else {
            QName parentQName = parent.getQName();
            namespace = parentQName.getNamespace();
            revision = parentQName.getRevision();
            prefix = parentQName.getPrefix();
        }
        SchemaPath parentPath = parent.getPath();

        for (UsesNodeBuilder unb : targetGrouping.getUses()) {
            Set<DataSchemaNodeBuilder> newChildren = processUsesDataSchemaNode(usesNode, unb.getTargetChildren(),
                    parentPath, namespace, revision, prefix);
            usesNode.getTargetChildren().addAll(newChildren);

            Set<GroupingBuilder> newGroupings = processUsesGroupings(unb.getTargetGroupings(), parentPath, namespace,
                    revision, prefix);
            usesNode.getTargetGroupings().addAll(newGroupings);

            Set<TypeDefinitionBuilder> newTypedefs = processUsesTypedefs(unb.getTargetTypedefs(), parentPath,
                    namespace, revision, prefix);
            usesNode.getTargetTypedefs().addAll(newTypedefs);

            List<UnknownSchemaNodeBuilder> newUnknownNodes = processUsesUnknownNodes(unb.getTargetUnknownNodes(),
                    parentPath, namespace, revision, prefix);
            usesNode.getTargetUnknownNodes().addAll(newUnknownNodes);
        }
    }

    private void processUsesNode(final UsesNodeBuilder usesNode, final GroupingDefinition targetGrouping) {
        final String moduleName = usesNode.getModuleName();
        final int line = usesNode.getLine();
        DataNodeContainerBuilder parent = usesNode.getParent();
        URI namespace = null;
        Date revision = null;
        String prefix = null;
        if (parent instanceof ModuleBuilder) {
            ModuleBuilder m = (ModuleBuilder) parent;
            namespace = m.getNamespace();
            revision = m.getRevision();
            prefix = m.getPrefix();
        } else {
            QName parentQName = parent.getQName();
            namespace = parentQName.getNamespace();
            revision = parentQName.getRevision();
            prefix = parentQName.getPrefix();
        }
        SchemaPath parentPath = parent.getPath();

        final Set<DataSchemaNodeBuilder> newChildren = new HashSet<>();
        for (DataSchemaNode child : targetGrouping.getChildNodes()) {
            if (child != null) {
                DataSchemaNodeBuilder newChild = null;
                QName newQName = new QName(namespace, revision, prefix, child.getQName().getLocalName());
                if (child instanceof AnyXmlSchemaNode) {
                    newChild = createAnyXml((AnyXmlSchemaNode) child, newQName, moduleName, line);
                } else if (child instanceof ChoiceNode) {
                    newChild = createChoice((ChoiceNode) child, newQName, moduleName, line);
                } else if (child instanceof ContainerSchemaNode) {
                    newChild = createContainer((ContainerSchemaNode) child, newQName, moduleName, line);
                } else if (child instanceof LeafListSchemaNode) {
                    newChild = createLeafList((LeafListSchemaNode) child, newQName, moduleName, line);
                } else if (child instanceof LeafSchemaNode) {
                    newChild = createLeafBuilder((LeafSchemaNode) child, newQName, moduleName, line);
                } else if (child instanceof ListSchemaNode) {
                    newChild = createList((ListSchemaNode) child, newQName, moduleName, line);
                }

                if (newChild == null) {
                    throw new YangParseException(moduleName, line,
                            "Unknown member of target grouping while resolving uses node.");
                }
                if (newChild instanceof GroupingMember) {
                    ((GroupingMember) newChild).setAddedByUses(true);
                }

                newChild.setPath(createSchemaPath(parentPath, newQName));
                newChildren.add(newChild);
            }
        }
        usesNode.getTargetChildren().addAll(newChildren);

        final Set<GroupingBuilder> newGroupings = new HashSet<>();
        for (GroupingDefinition g : targetGrouping.getGroupings()) {
            QName newQName = new QName(namespace, revision, prefix, g.getQName().getLocalName());
            GroupingBuilder newGrouping = createGrouping(g, newQName, moduleName, line);
            newGrouping.setAddedByUses(true);
            newGrouping.setPath(createSchemaPath(parentPath, newQName));
            newGroupings.add(newGrouping);
        }
        usesNode.getTargetGroupings().addAll(newGroupings);

        final Set<TypeDefinitionBuilder> newTypedefs = new HashSet<>();
        for (TypeDefinition<?> td : targetGrouping.getTypeDefinitions()) {
            QName newQName = new QName(namespace, revision, prefix, td.getQName().getLocalName());
            TypeDefinitionBuilder newType = createTypedef((ExtendedType) td, newQName, moduleName, line);
            newType.setAddedByUses(true);
            newType.setPath(createSchemaPath(parentPath, newQName));
            newTypedefs.add(newType);
        }
        usesNode.getTargetTypedefs().addAll(newTypedefs);

        final List<UnknownSchemaNodeBuilder> newUnknownNodes = new ArrayList<>();
        for (UnknownSchemaNode un : targetGrouping.getUnknownSchemaNodes()) {
            QName newQName = new QName(namespace, revision, prefix, un.getQName().getLocalName());
            UnknownSchemaNodeBuilder newNode = createUnknownSchemaNode(un, newQName, moduleName, line);
            newNode.setAddedByUses(true);
            newNode.setPath(createSchemaPath(parentPath, newQName));
            newUnknownNodes.add(newNode);
        }
        usesNode.getTargetUnknownNodes().addAll(newUnknownNodes);
    }

    private void resolveUnknownNodes(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        for (UnknownSchemaNodeBuilder usnb : module.getAllUnknownNodes()) {
            QName nodeType = usnb.getNodeType();
            if (nodeType.getNamespace() == null || nodeType.getRevision() == null) {
                try {
                    ModuleBuilder dependentModule = findDependentModuleBuilder(modules, module, nodeType.getPrefix(),
                            usnb.getLine());
                    QName newNodeType = new QName(dependentModule.getNamespace(), dependentModule.getRevision(),
                            nodeType.getPrefix(), nodeType.getLocalName());
                    usnb.setNodeType(newNodeType);
                } catch (YangParseException e) {
                    LOG.debug(module.getName(), usnb.getLine(), "Failed to find unknown node type: " + nodeType);
                }
            }
        }
    }

    private void resolveUnknownNodesWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final SchemaContext context) {
        for (UnknownSchemaNodeBuilder unknownNodeBuilder : module.getAllUnknownNodes()) {
            QName nodeType = unknownNodeBuilder.getNodeType();
            if (nodeType.getNamespace() == null || nodeType.getRevision() == null) {
                try {
                    ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, module,
                            nodeType.getPrefix(), unknownNodeBuilder.getLine());

                    QName newNodeType = null;
                    if (dependentModuleBuilder == null) {
                        Module dependentModule = findModuleFromContext(context, module, nodeType.getPrefix(),
                                unknownNodeBuilder.getLine());
                        newNodeType = new QName(dependentModule.getNamespace(), dependentModule.getRevision(),
                                nodeType.getPrefix(), nodeType.getLocalName());
                    } else {
                        newNodeType = new QName(dependentModuleBuilder.getNamespace(),
                                dependentModuleBuilder.getRevision(), nodeType.getPrefix(), nodeType.getLocalName());
                    }

                    unknownNodeBuilder.setNodeType(newNodeType);
                } catch (YangParseException e) {
                    LOG.debug(module.getName(), unknownNodeBuilder.getLine(), "Failed to find unknown node type: "
                            + nodeType);
                }
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

            ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, module, prefix, line);
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

            ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, module, prefix, line);
            if (dependentModuleBuilder == null) {
                Module dependentModule = findModuleFromContext(context, module, prefix, line);
                Object currentParent = dependentModule;

                for (int i = 0; i < path.size(); i++) {
                    if (currentParent == null) {
                        throw new YangParseException(module.getName(), line, "Failed to find deviation target.");
                    }
                    QName q = path.get(i);
                    name = q.getLocalName();
                    if (currentParent instanceof DataNodeContainer) {
                        currentParent = ((DataNodeContainer) currentParent).getDataChildByName(name);
                    }
                }

                if (currentParent == null) {
                    throw new YangParseException(module.getName(), line, "Failed to find deviation target.");
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

        for (int i = 0; i < path.size(); i++) {
            if (currentParent == null) {
                throw new YangParseException(module.getName(), line, "Failed to find deviation target.");
            }
            QName q = path.get(i);
            String name = q.getLocalName();
            if (currentParent instanceof DataNodeContainerBuilder) {
                currentParent = ((DataNodeContainerBuilder) currentParent).getDataChildByName(name);
            }
        }

        if (currentParent == null || !(currentParent instanceof SchemaNodeBuilder)) {
            throw new YangParseException(module.getName(), line, "Failed to find deviation target.");
        }
        dev.setTargetPath(((SchemaNodeBuilder) currentParent).getPath());
    }

}
