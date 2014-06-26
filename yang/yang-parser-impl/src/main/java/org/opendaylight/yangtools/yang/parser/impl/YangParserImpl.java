/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.fillAugmentTarget;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findBaseIdentity;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findModuleFromBuilders;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findModuleFromContext;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findSchemaNode;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findSchemaNodeInModule;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.processAugmentation;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.setNodeAddedByUses;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.wrapChildNode;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.wrapChildNodes;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.wrapGroupings;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.wrapTypedefs;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.wrapUnknownNodes;
import static org.opendaylight.yangtools.yang.parser.builder.impl.TypeUtils.resolveType;
import static org.opendaylight.yangtools.yang.parser.builder.impl.TypeUtils.resolveTypeUnion;
import static org.opendaylight.yangtools.yang.parser.builder.impl.TypeUtils.resolveTypeUnionWithContext;
import static org.opendaylight.yangtools.yang.parser.builder.impl.TypeUtils.resolveTypeWithContext;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.ExtensionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.DeviationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.GroupingUtils;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder.ModuleImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;
import org.opendaylight.yangtools.yang.parser.util.NamedByteArrayInputStream;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.opendaylight.yangtools.yang.parser.util.NamedInputStream;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YangParserImpl implements YangContextParser {
    private static final Logger LOG = LoggerFactory.getLogger(YangParserImpl.class);

    private static final String FAIL_DEVIATION_TARGET = "Failed to find deviation target.";

    @Override
    @Deprecated
    public Set<Module> parseYangModels(final File yangFile, final File directory) {
        try {
            return parseFile(yangFile, directory).getModules();
        } catch (IOException | YangSyntaxErrorException e) {
            throw new YangParseException("Failed to parse yang data", e);
        }
    }

    @Override
    public SchemaContext parseFile(final File yangFile, final File directory) throws IOException,
    YangSyntaxErrorException {
        Preconditions.checkState(yangFile.exists(), yangFile + " does not exists");
        Preconditions.checkState(directory.exists(), directory + " does not exists");
        Preconditions.checkState(directory.isDirectory(), directory + " is not a directory");

        final String yangFileName = yangFile.getName();
        final String[] fileList = checkNotNull(directory.list(), directory + " not found or is not a directory");

        Map<ByteSource, File> sourceToFile = new LinkedHashMap<>();
        ByteSource mainFileSource = BuilderUtils.fileToByteSource(yangFile);
        sourceToFile.put(mainFileSource, yangFile);

        for (String fileName : fileList) {
            if (fileName.equals(yangFileName)) {
                continue;
            }
            File dependency = new File(directory, fileName);
            if (dependency.isFile()) {
                sourceToFile.put(BuilderUtils.fileToByteSource(dependency), dependency);
            }
        }

        Map<ByteSource, ModuleBuilder> sourceToBuilder = parseSourcesToBuilders(sourceToFile.keySet());
        ModuleBuilder main = sourceToBuilder.get(mainFileSource);

        List<ModuleBuilder> moduleBuilders = new ArrayList<>();
        moduleBuilders.add(main);
        filterImports(main, new ArrayList<>(sourceToBuilder.values()), moduleBuilders);
        Collection<ModuleBuilder> resolved = resolveSubmodules(moduleBuilders);

        // module builders sorted by dependencies
        List<ModuleBuilder> sortedBuilders = ModuleDependencySort.sort(resolved);
        LinkedHashMap<String, TreeMap<Date, ModuleBuilder>> modules = orderModules(sortedBuilders);
        Collection<Module> unsorted = build(modules).values();
        Set<Module> result = new LinkedHashSet<>(
                ModuleDependencySort.sort(unsorted.toArray(new Module[unsorted.size()])));
        return resolveSchemaContext(result);
    }

    @Override
    @Deprecated
    public Set<Module> parseYangModels(final List<File> yangFiles) {
        return parseFiles(yangFiles).getModules();
    }

    @Override
    public SchemaContext parseFiles(final Collection<File> yangFiles) {
        Collection<Module> unsorted = parseYangModelsMapped(yangFiles).values();
        Set<Module> sorted = new LinkedHashSet<>(
                ModuleDependencySort.sort(unsorted.toArray(new Module[unsorted.size()])));
        return resolveSchemaContext(sorted);
    }

    @Override
    @Deprecated
    public Set<Module> parseYangModels(final List<File> yangFiles, final SchemaContext context) {
        try {
            return parseFiles(yangFiles, context).getModules();
        } catch (IOException | YangSyntaxErrorException e) {
            throw new YangParseException("Failed to parse yang data", e);
        }
    }

    @Override
    public SchemaContext parseFiles(final Collection<File> yangFiles, final SchemaContext context) throws IOException,
    YangSyntaxErrorException {
        if (yangFiles == null) {
            return resolveSchemaContext(Collections.<Module> emptySet());
        }

        Collection<ByteSource> sources = BuilderUtils.filesToByteSources(yangFiles);
        SchemaContext result = parseSources(sources, context);
        return result;
    }

    @Override
    @Deprecated
    public Set<Module> parseYangModelsFromStreams(final List<InputStream> yangModelStreams) {
        try {
            Collection<ByteSource> sources = BuilderUtils.streamsToByteSources(yangModelStreams);
            return parseSources(sources).getModules();
        } catch (IOException | YangSyntaxErrorException e) {
            throw new YangParseException("Failed to parse yang data", e);
        }
    }

    @Override
    public SchemaContext parseSources(final Collection<ByteSource> sources) throws IOException,
    YangSyntaxErrorException {
        Collection<Module> unsorted = parseYangModelSources(sources).values();
        Set<Module> sorted = new LinkedHashSet<>(
                ModuleDependencySort.sort(unsorted.toArray(new Module[unsorted.size()])));
        return resolveSchemaContext(sorted);
    }

    @Override
    @Deprecated
    public Set<Module> parseYangModelsFromStreams(final List<InputStream> yangModelStreams, final SchemaContext context) {
        try {
            Collection<ByteSource> sources = BuilderUtils.streamsToByteSources(yangModelStreams);
            return parseSources(sources, context).getModules();
        } catch (IOException | YangSyntaxErrorException e) {
            throw new YangParseException("Failed to parse yang data", e);
        }
    }

    @Override
    public SchemaContext parseSources(final Collection<ByteSource> sources, final SchemaContext context)
            throws IOException, YangSyntaxErrorException {
        if (sources == null) {
            return resolveSchemaContext(Collections.<Module> emptySet());
        }

        final Map<String, TreeMap<Date, ModuleBuilder>> modules = resolveModuleBuilders(sources, context);
        final Set<Module> unsorted = new LinkedHashSet<>(buildWithContext(modules, context).values());
        if (context != null) {
            for (Module m : context.getModules()) {
                if (!unsorted.contains(m)) {
                    unsorted.add(m);
                }
            }
        }
        Set<Module> result = new LinkedHashSet<>(
                ModuleDependencySort.sort(unsorted.toArray(new Module[unsorted.size()])));
        return resolveSchemaContext(result);
    }

    @Override
    public Map<File, Module> parseYangModelsMapped(final Collection<File> yangFiles) {
        if (yangFiles == null || yangFiles.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<ByteSource, File> byteSourceToFile = new HashMap<>();
        for (final File file : yangFiles) {
            ByteSource source = new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    return new NamedFileInputStream(file, file.getPath());
                }
            };
            byteSourceToFile.put(source, file);
        }

        Map<ByteSource, Module> byteSourceToModule;
        try {
            byteSourceToModule = parseYangModelSources(byteSourceToFile.keySet());
        } catch (IOException | YangSyntaxErrorException e) {
            throw new YangParseException("Failed to parse yang data", e);
        }
        Map<File, Module> result = new LinkedHashMap<>();
        for (Map.Entry<ByteSource, Module> entry : byteSourceToModule.entrySet()) {
            result.put(byteSourceToFile.get(entry.getKey()), entry.getValue());
        }
        return result;
    }

    @Override
    public Map<InputStream, Module> parseYangModelsFromStreamsMapped(final Collection<InputStream> yangModelStreams) {
        if (yangModelStreams == null || yangModelStreams.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<ByteSource, InputStream> sourceToStream = new HashMap<>();
        for (final InputStream stream : yangModelStreams) {
            ByteSource source = new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    return NamedByteArrayInputStream.create(stream);
                }
            };
            sourceToStream.put(source, stream);
        }

        Map<ByteSource, Module> sourceToModule;
        try {
            sourceToModule = parseYangModelSources(sourceToStream.keySet());
        } catch (IOException | YangSyntaxErrorException e) {
            throw new YangParseException("Failed to parse yang data", e);
        }
        Map<InputStream, Module> result = new LinkedHashMap<>();
        for (Map.Entry<ByteSource, Module> entry : sourceToModule.entrySet()) {
            result.put(sourceToStream.get(entry.getKey()), entry.getValue());
        }
        return result;
    }

    @Override
    public SchemaContext resolveSchemaContext(final Set<Module> modules) {
        // after merging parse method with this one, add support for getting
        // submodule sources.
        Map<ModuleIdentifier, String> identifiersToSources = new HashMap<>();
        for (Module module : modules) {
            ModuleImpl moduleImpl = (ModuleImpl) module;
            identifiersToSources.put(module, moduleImpl.getSource());
        }
        return new SchemaContextImpl(modules, identifiersToSources);
    }

    private Map<ByteSource, Module> parseYangModelSources(final Collection<ByteSource> sources) throws IOException,
    YangSyntaxErrorException {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<ByteSource, ModuleBuilder> sourceToBuilder = resolveSources(sources);
        // sort and check for duplicates
        List<ModuleBuilder> sorted = ModuleDependencySort.sort(sourceToBuilder.values());
        BuilderUtils.setSourceToBuilder(sourceToBuilder);
        Map<String, TreeMap<Date, ModuleBuilder>> modules = orderModules(sorted);
        Map<ModuleBuilder, Module> builderToModule = build(modules);
        Map<ModuleBuilder, ByteSource> builderToSource = HashBiMap.create(sourceToBuilder).inverse();
        sorted = ModuleDependencySort.sort(builderToModule.keySet());

        Map<ByteSource, Module> result = new LinkedHashMap<>();
        for (ModuleBuilder moduleBuilder : sorted) {
            Module value = checkNotNull(builderToModule.get(moduleBuilder), "Cannot get module for %s", moduleBuilder);
            result.put(builderToSource.get(moduleBuilder), value);
        }

        return result;
    }

    /**
     * Parse streams and resolve submodules.
     *
     * @param streams
     *            collection of streams to parse
     * @return map, where key is source stream and value is module builder
     *         parsed from stream
     * @throws YangSyntaxErrorException
     */
    private Map<ByteSource, ModuleBuilder> resolveSources(final Collection<ByteSource> streams) throws IOException,
    YangSyntaxErrorException {
        Map<ByteSource, ModuleBuilder> builders = parseSourcesToBuilders(streams);
        Map<ByteSource, ModuleBuilder> result = resolveSubmodules(builders);
        return result;
    }

    private Map<ByteSource, ModuleBuilder> parseSourcesToBuilders(final Collection<ByteSource> sources)
            throws IOException, YangSyntaxErrorException {
        final ParseTreeWalker walker = new ParseTreeWalker();
        final Map<ByteSource, ParseTree> sourceToTree = parseYangSources(sources);
        final Map<ByteSource, ModuleBuilder> sourceToBuilder = new LinkedHashMap<>();

        // validate yang
        new YangModelBasicValidator(walker).validate(sourceToTree.values());

        YangParserListenerImpl yangModelParser;
        for (Map.Entry<ByteSource, ParseTree> entry : sourceToTree.entrySet()) {
            ByteSource source = entry.getKey();
            String path = null;
            InputStream stream = source.openStream();
            if (stream instanceof NamedInputStream) {
                path = stream.toString();
            }
            try {
                stream.close();
            } catch (IOException e) {
                LOG.warn("Failed to close stream {}", stream);
            }

            yangModelParser = new YangParserListenerImpl(path);
            walker.walk(yangModelParser, entry.getValue());
            ModuleBuilder moduleBuilder = yangModelParser.getModuleBuilder();
            sourceToBuilder.put(source, moduleBuilder);
        }

        BuilderUtils.setSourceToBuilder(sourceToBuilder);
        return sourceToBuilder;
    }

    private Map<ByteSource, ModuleBuilder> resolveSubmodules(final Map<ByteSource, ModuleBuilder> builders) {
        Map<ByteSource, ModuleBuilder> modules = new HashMap<>();
        Set<ModuleBuilder> submodules = new HashSet<>();
        for (Map.Entry<ByteSource, ModuleBuilder> entry : builders.entrySet()) {
            ModuleBuilder moduleBuilder = entry.getValue();
            if (moduleBuilder.isSubmodule()) {
                submodules.add(moduleBuilder);
            } else {
                modules.put(entry.getKey(), moduleBuilder);
            }
        }

        Collection<ModuleBuilder> values = modules.values();
        for (ModuleBuilder submodule : submodules) {
            for (ModuleBuilder module : values) {
                if (module.getName().equals(submodule.getBelongsTo())) {
                    addSubmoduleToModule(submodule, module);
                }
            }
        }
        return modules;
    }

    /**
     * Traverse collection of builders, find builders representing submodule and
     * add this submodule to its parent module.
     *
     * @param builders
     *            collection of builders containing modules and submodules
     * @return collection of module builders
     */
    private Collection<ModuleBuilder> resolveSubmodules(final Collection<ModuleBuilder> builders) {
        Collection<ModuleBuilder> modules = new HashSet<>();
        Set<ModuleBuilder> submodules = new HashSet<>();
        for (ModuleBuilder moduleBuilder : builders) {
            if (moduleBuilder.isSubmodule()) {
                submodules.add(moduleBuilder);
            } else {
                modules.add(moduleBuilder);
            }
        }

        for (ModuleBuilder submodule : submodules) {
            for (ModuleBuilder module : modules) {
                if (module.getName().equals(submodule.getBelongsTo())) {
                    addSubmoduleToModule(submodule, module);
                }
            }
        }
        return modules;
    }

    private void addSubmoduleToModule(final ModuleBuilder submodule, final ModuleBuilder module) {
        submodule.setParent(module);
        module.getDirtyNodes().addAll(submodule.getDirtyNodes());
        module.getModuleImports().addAll(submodule.getModuleImports());
        module.getAugments().addAll(submodule.getAugments());
        module.getAugmentBuilders().addAll(submodule.getAugmentBuilders());
        module.getAllAugments().addAll(submodule.getAllAugments());
        module.getChildNodeBuilders().addAll(submodule.getChildNodeBuilders());
        module.getChildNodes().putAll(submodule.getChildNodes());
        module.getGroupings().addAll(submodule.getGroupings());
        module.getGroupingBuilders().addAll(submodule.getGroupingBuilders());
        module.getTypeDefinitions().addAll(submodule.getTypeDefinitions());
        module.getTypeDefinitionBuilders().addAll(submodule.getTypeDefinitionBuilders());
        module.getUsesNodes().addAll(submodule.getUsesNodes());
        module.getUsesNodeBuilders().addAll(submodule.getUsesNodeBuilders());
        module.getAllGroupings().addAll(submodule.getAllGroupings());
        module.getAllUsesNodes().addAll(submodule.getAllUsesNodes());
        module.getRpcs().addAll(submodule.getRpcs());
        module.getAddedRpcs().addAll(submodule.getAddedRpcs());
        module.getNotifications().addAll(submodule.getNotifications());
        module.getAddedNotifications().addAll(submodule.getAddedNotifications());
        module.getIdentities().addAll(submodule.getIdentities());
        module.getAddedIdentities().addAll(submodule.getAddedIdentities());
        module.getFeatures().addAll(submodule.getFeatures());
        module.getAddedFeatures().addAll(submodule.getAddedFeatures());
        module.getDeviations().addAll(submodule.getDeviations());
        module.getDeviationBuilders().addAll(submodule.getDeviationBuilders());
        module.getExtensions().addAll(submodule.getExtensions());
        module.getAddedExtensions().addAll(submodule.getAddedExtensions());
        module.getUnknownNodes().addAll(submodule.getUnknownNodes());
        module.getAllUnknownNodes().addAll(submodule.getAllUnknownNodes());
    }

    private Map<String, TreeMap<Date, ModuleBuilder>> resolveModuleBuilders(
            final Collection<ByteSource> yangFileStreams, final SchemaContext context) throws IOException,
            YangSyntaxErrorException {
        Map<ByteSource, ModuleBuilder> parsedBuilders = resolveSources(yangFileStreams);
        ModuleBuilder[] builders = new ModuleBuilder[parsedBuilders.size()];
        parsedBuilders.values().toArray(builders);

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
    private LinkedHashMap<String, TreeMap<Date, ModuleBuilder>> orderModules(final List<ModuleBuilder> modules) {
        final LinkedHashMap<String, TreeMap<Date, ModuleBuilder>> result = new LinkedHashMap<>();
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
                builderByRevision = new TreeMap<>();
            }
            builderByRevision.put(builderRevision, builder);
            result.put(builderName, builderByRevision);
        }
        return result;
    }

    /**
     * Find {@code main} dependencies from {@code other} and add them to
     * {@code filtered}.
     *
     * @param main
     *            main yang module
     * @param other
     *            all loaded modules
     * @param filtered
     *            collection to fill up
     */
    private void filterImports(final ModuleBuilder main, final Collection<ModuleBuilder> other,
            final Collection<ModuleBuilder> filtered) {
        Set<ModuleImport> imports = main.getModuleImports();

        // if this is submodule, add parent to filtered and pick its imports
        if (main.isSubmodule()) {
            TreeMap<Date, ModuleBuilder> dependencies = new TreeMap<>();
            for (ModuleBuilder mb : other) {
                if (mb.getName().equals(main.getBelongsTo())) {
                    dependencies.put(mb.getRevision(), mb);
                }
            }
            ModuleBuilder parent = dependencies.get(dependencies.firstKey());
            filtered.add(parent);
            imports.addAll(parent.getModuleImports());
        }

        for (ModuleImport mi : imports) {
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

    private Map<ByteSource, ParseTree> parseYangSources(final Collection<ByteSource> sources) throws IOException,
    YangSyntaxErrorException {
        final Map<ByteSource, ParseTree> trees = new HashMap<>();
        for (ByteSource source : sources) {
            trees.put(source, parseYangSource(source));
        }
        return trees;
    }

    private YangContext parseYangSource(final ByteSource source) throws IOException, YangSyntaxErrorException {
        try (InputStream stream = source.openStream()) {
            final ANTLRInputStream input = new ANTLRInputStream(stream);
            final YangLexer lexer = new YangLexer(input);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final YangParser parser = new YangParser(tokens);
            parser.removeErrorListeners();

            final YangErrorListener errorListener = new YangErrorListener();
            parser.addErrorListener(errorListener);

            final YangContext result = parser.yang();
            errorListener.validate();

            return result;
        }
    }

    public static YangContext parseStreamWithoutErrorListeners(final InputStream yangStream) {
        YangContext result = null;
        try {
            final ANTLRInputStream input = new ANTLRInputStream(yangStream);
            final YangLexer lexer = new YangLexer(input);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final YangParser parser = new YangParser(tokens);
            parser.removeErrorListeners();
            result = parser.yang();
        } catch (IOException e) {
            LOG.warn("Exception while reading yang file: " + yangStream, e);
        }
        return result;
    }

    /**
     * Creates builder-to-module map based on given modules. Method first
     * resolve unresolved type references, instantiate groupings through uses
     * statements and perform augmentation.
     *
     * Node resolving must be performed in following order:
     * <ol>
     * <li>
     * unresolved type references</li>
     * <li>
     * uses in groupings</li>
     * <li>
     * uses in other nodes</li>
     * <li>
     * augments</li>
     * </ol>
     *
     * @param modules
     *            all loaded modules
     * @return modules mapped on their builders
     */
    private Map<ModuleBuilder, Module> build(final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
        resolveDirtyNodes(modules);
        resolveAugmentsTargetPath(modules, null);
        resolveUsesTargetGrouping(modules, null);
        resolveUsesForGroupings(modules, null);
        resolveUsesForNodes(modules, null);
        resolveAugments(modules, null);
        resolveIdentities(modules);
        resolveDeviations(modules);

        // build
        final Map<ModuleBuilder, Module> result = new LinkedHashMap<>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder moduleBuilder = childEntry.getValue();
                final Module module = moduleBuilder.build();
                result.put(moduleBuilder, module);
            }
        }
        return result;
    }

    /**
     * Creates builder-to-module map based on given modules. Method first
     * resolve unresolved type references, instantiate groupings through uses
     * statements and perform augmentation.
     *
     * Node resolving must be performed in following order:
     * <ol>
     * <li>
     * unresolved type references</li>
     * <li>
     * uses in groupings</li>
     * <li>
     * uses in other nodes</li>
     * <li>
     * augments</li>
     * </ol>
     *
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     * @return modules mapped on their builders
     */
    private Map<ModuleBuilder, Module> buildWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        resolvedDirtyNodesWithContext(modules, context);
        resolveAugmentsTargetPath(modules, context);
        resolveUsesTargetGrouping(modules, context);
        resolveUsesForGroupings(modules, context);
        resolveUsesForNodes(modules, context);
        resolveAugments(modules, context);
        resolveIdentitiesWithContext(modules, context);
        resolveDeviationsWithContext(modules, context);

        // build
        final Map<ModuleBuilder, Module> result = new LinkedHashMap<>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder moduleBuilder = childEntry.getValue();
                final Module module = moduleBuilder.build();
                result.put(moduleBuilder, module);
            }
        }
        return result;
    }

    /**
     * Resolve all unresolved type references.
     *
     * @param modules
     *            all loaded modules
     */
    private void resolveDirtyNodes(final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder module = childEntry.getValue();
                resolveUnknownNodes(modules, module);
                resolveDirtyNodes(modules, module);
            }
        }
    }

    /**
     * Resolve all unresolved type references.
     *
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolvedDirtyNodesWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder module = childEntry.getValue();
                resolveUnknownNodesWithContext(modules, module, context);
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
            final ModuleBuilder module, final SchemaContext context) {
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
     * Traverse through augmentations of modules and fix their child nodes
     * schema path.
     *
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveAugmentsTargetPath(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
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

    /**
     * Find augment target and set correct schema path for all its child nodes.
     *
     * @param modules
     *            all loaded modules
     * @param augment
     *            augment to resolve
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void setCorrectAugmentTargetPath(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final AugmentationSchemaBuilder augment, final SchemaContext context) {
        ModuleBuilder module = BuilderUtils.getParentModule(augment);
        final SchemaPath oldSchemaPath = augment.getTargetPath();
        List<QName> newPath = new ArrayList<>();

        Builder parent = augment.getParent();
        if (parent instanceof UsesNodeBuilder) {
            DataNodeContainerBuilder usesParent = ((UsesNodeBuilder) parent).getParent();
            newPath.addAll(usesParent.getPath().getPath());

            URI ns;
            Date revision;
            String prefix;
            QName baseQName = usesParent.getQName();
            if (baseQName == null) {
                ModuleBuilder m = BuilderUtils.getParentModule(usesParent);
                ns = m.getNamespace();
                revision = m.getRevision();
                prefix = m.getPrefix();
            } else {
                ns = baseQName.getNamespace();
                revision = baseQName.getRevision();
                prefix = baseQName.getPrefix();
            }

            final QNameModule qm = QNameModule.create(ns, revision);
            for (QName qn : oldSchemaPath.getPathFromRoot()) {
                newPath.add(QName.create(qm, prefix, qn.getLocalName()));
            }
        } else {
            for (QName qn : oldSchemaPath.getPathFromRoot()) {
                URI ns = module.getNamespace();
                Date rev = module.getRevision();
                String localPrefix = qn.getPrefix();
                if (localPrefix != null && !("".equals(localPrefix))) {
                    ModuleBuilder currentModule = BuilderUtils.findModuleFromBuilders(modules, module, localPrefix,
                            augment.getLine());
                    if (currentModule == null) {
                        Module m = BuilderUtils.findModuleFromContext(context, module, localPrefix, augment.getLine());
                        if (m == null) {
                            throw new YangParseException(module.getName(), augment.getLine(), "Module with prefix "
                                    + localPrefix + " not found.");
                        }
                        ns = m.getNamespace();
                        rev = m.getRevision();
                    } else {
                        ns = currentModule.getNamespace();
                        rev = currentModule.getRevision();
                    }
                }
                newPath.add(new QName(ns, rev, localPrefix, qn.getLocalName()));
            }
        }
        augment.setTargetNodeSchemaPath(SchemaPath.create(newPath, true));

        for (DataSchemaNodeBuilder childNode : augment.getChildNodeBuilders()) {
            correctPathForAugmentNodes(childNode, augment.getTargetNodeSchemaPath());
        }
    }

    /**
     * Set new schema path to node and all its child nodes based on given parent
     * path. This method do not change the namespace.
     *
     * @param node
     *            node which schema path should be updated
     * @param parentPath
     *            schema path of parent node
     */
    private void correctPathForAugmentNodes(final DataSchemaNodeBuilder node, final SchemaPath parentPath) {
        SchemaPath newPath = parentPath.createChild(node.getQName());
        node.setPath(newPath);
        if (node instanceof DataNodeContainerBuilder) {
            for (DataSchemaNodeBuilder child : ((DataNodeContainerBuilder) node).getChildNodeBuilders()) {
                correctPathForAugmentNodes(child, node.getPath());
            }
        }
        if (node instanceof ChoiceBuilder) {
            for (ChoiceCaseBuilder child : ((ChoiceBuilder) node).getCases()) {
                correctPathForAugmentNodes(child, node.getPath());
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
    private void checkAugmentMandatoryNodes(final Collection<AugmentationSchemaBuilder> augments) {
        for (AugmentationSchemaBuilder augment : augments) {
            String augmentPrefix = augment.getTargetPath().getPathFromRoot().iterator().next().getPrefix();
            ModuleBuilder module = BuilderUtils.getParentModule(augment);
            String modulePrefix = module.getPrefix();

            if (augmentPrefix == null || augmentPrefix.isEmpty() || augmentPrefix.equals(modulePrefix)) {
                continue;
            }

            for (DataSchemaNodeBuilder childNode : augment.getChildNodeBuilders()) {
                if (childNode.getConstraints().isMandatory()) {
                    throw new YangParseException(augment.getModuleName(), augment.getLine(),
                            "Error in augment parsing: cannot augment mandatory node "
                                    + childNode.getQName().getLocalName());
                }
            }
        }
    }

    /**
     * Go through all augment definitions and resolve them.
     *
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveAugments(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final SchemaContext context) {
        List<ModuleBuilder> all = new ArrayList<>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                all.add(inner.getValue());
            }
        }

        List<ModuleBuilder> sorted;
        if (context == null) {
            sorted = ModuleDependencySort.sort(all.toArray(new ModuleBuilder[all.size()]));
        } else {
            sorted = ModuleDependencySort.sortWithContext(context, all.toArray(new ModuleBuilder[all.size()]));
        }

        for (ModuleBuilder mb : sorted) {
            if (mb != null) {
                List<AugmentationSchemaBuilder> augments = mb.getAllAugments();
                checkAugmentMandatoryNodes(augments);
                Collections.sort(augments, Comparators.AUGMENT_COMP);
                for (AugmentationSchemaBuilder augment : augments) {
                    if (!(augment.isResolved())) {
                        boolean resolved = resolveAugment(augment, mb, modules, context);
                        if (!resolved) {
                            throw new YangParseException(augment.getModuleName(), augment.getLine(),
                                    "Error in augment parsing: failed to find augment target: " + augment);
                        }
                    }
                }
            }
        }
    }

    /**
     * Perform augmentation defined under uses statement.
     *
     * @param augment
     *            augment to resolve
     * @param module
     *            current module
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     * @return true if augment process succeed
     */
    private boolean resolveUsesAugment(final AugmentationSchemaBuilder augment, final ModuleBuilder module,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final SchemaContext context) {
        if (augment.isResolved()) {
            return true;
        }

        UsesNodeBuilder usesNode = (UsesNodeBuilder) augment.getParent();
        DataNodeContainerBuilder parentNode = usesNode.getParent();
        Optional<SchemaNodeBuilder> potentialTargetNode;
        SchemaPath resolvedTargetPath = augment.getTargetNodeSchemaPath();
        if (parentNode instanceof ModuleBuilder && resolvedTargetPath.isAbsolute()) {
            // Uses is directly used in module body, we lookup
            // We lookup in data namespace to find correct augmentation target
            potentialTargetNode = findSchemaNodeInModule(resolvedTargetPath, (ModuleBuilder) parentNode);
        } else {
            // Uses is used in local context (be it data namespace or grouping namespace,
            // since all nodes via uses are imported to localName, it is safe to
            // to proceed only with local names.
            //
            // Conflicting elements in other namespaces are still not present
            // since resolveUsesAugment occurs before augmenting from external modules.
            potentialTargetNode = Optional.<SchemaNodeBuilder> fromNullable(findSchemaNode(augment.getTargetPath()
                    .getPath(), (SchemaNodeBuilder) parentNode));
        }

        if (potentialTargetNode.isPresent()) {
            SchemaNodeBuilder targetNode = potentialTargetNode.get();
            if (targetNode instanceof AugmentationTargetBuilder) {
                fillAugmentTarget(augment, targetNode);
                ((AugmentationTargetBuilder) targetNode).addAugmentation(augment);
                augment.setResolved(true);
                return true;
            } else {
                throw new YangParseException(module.getName(), augment.getLine(), String.format(
                        "Failed to resolve augment in uses. Invalid augment target: %s", potentialTargetNode));
            }
        } else {
            throw new YangParseException(module.getName(), augment.getLine(), String.format(
                    "Failed to resolve augment in uses. Invalid augment target path: %s", augment.getTargetPath()));
        }

    }

    /**
     * Find augment target module and perform augmentation.
     *
     * @param augment
     *            augment to resolve
     * @param module
     *            current module
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     * @return true if augment process succeed
     */
    private boolean resolveAugment(final AugmentationSchemaBuilder augment, final ModuleBuilder module,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final SchemaContext context) {
        if (augment.isResolved()) {
            return true;
        }

        QName targetPath = augment.getTargetPath().getPathFromRoot().iterator().next();
        ModuleBuilder targetModule = findTargetModule(targetPath, module, modules, context, augment.getLine());
        if (targetModule == null) {
            throw new YangParseException(module.getModuleName(), augment.getLine(), "Failed to resolve augment "
                    + augment);
        }

        return processAugmentation(augment, targetModule);
    }

    /**
     * Find module from loaded modules or from context based on given qname. If
     * module is found in context, create wrapper over this module and add it to
     * collection of loaded modules.
     *
     * @param qname
     * @param module
     *            current module
     * @param modules
     *            all loaded modules
     * @param context
     *            schema context
     * @param line
     *            current line
     * @return
     */
    private ModuleBuilder findTargetModule(final QName qname, final ModuleBuilder module,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final SchemaContext context, final int line) {
        ModuleBuilder targetModule = null;

        String prefix = qname.getPrefix();
        if (prefix == null || prefix.equals("")) {
            targetModule = module;
        } else {
            targetModule = findModuleFromBuilders(modules, module, qname.getPrefix(), line);
        }

        if (targetModule == null && context != null) {
            Module m = findModuleFromContext(context, module, prefix, line);
            targetModule = new ModuleBuilder(m);
            DataSchemaNode firstNode = m.getDataChildByName(qname.getLocalName());
            DataSchemaNodeBuilder firstNodeWrapped = wrapChildNode(targetModule.getModuleName(), line, firstNode,
                    targetModule.getPath(), firstNode.getQName());
            targetModule.addChildNode(firstNodeWrapped);

            TreeMap<Date, ModuleBuilder> map = new TreeMap<>();
            map.put(targetModule.getRevision(), targetModule);
            modules.put(targetModule.getModuleName(), map);
        }

        return targetModule;
    }

    private ModuleBuilder findTargetModule(final String prefix, final ModuleBuilder module,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final SchemaContext context, final int line) {
        ModuleBuilder targetModule = null;

        if (prefix == null || prefix.equals("")) {
            targetModule = module;
        } else {
            targetModule = findModuleFromBuilders(modules, module, prefix, line);
        }

        if (targetModule == null && context != null) {
            Module m = findModuleFromContext(context, module, prefix, line);
            if (m != null) {
                targetModule = new ModuleBuilder(m);
                TreeMap<Date, ModuleBuilder> map = new TreeMap<>();
                map.put(targetModule.getRevision(), targetModule);
                modules.put(targetModule.getModuleName(), map);
            }
        }

        return targetModule;
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
    private void resolveIdentities(final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                ModuleBuilder module = inner.getValue();
                final Set<IdentitySchemaNodeBuilder> identities = module.getAddedIdentities();
                for (IdentitySchemaNodeBuilder identity : identities) {
                    final String baseIdentityName = identity.getBaseIdentityName();
                    final int line = identity.getLine();
                    if (baseIdentityName != null) {
                        IdentitySchemaNodeBuilder baseIdentity = findBaseIdentity(modules, module, baseIdentityName,
                                line);
                        if (baseIdentity == null) {
                            throw new YangParseException(module.getName(), identity.getLine(),
                                    "Failed to find base identity");
                        } else {
                            identity.setBaseIdentity(baseIdentity);
                        }
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
            final SchemaContext context) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                ModuleBuilder module = inner.getValue();
                final Set<IdentitySchemaNodeBuilder> identities = module.getAddedIdentities();
                for (IdentitySchemaNodeBuilder identity : identities) {
                    final String baseIdentityName = identity.getBaseIdentityName();
                    final int line = identity.getLine();
                    if (baseIdentityName != null) {

                        IdentitySchemaNodeBuilder result = null;
                        if (baseIdentityName.contains(":")) {
                            String[] splittedBase = baseIdentityName.split(":");
                            if (splittedBase.length > 2) {
                                throw new YangParseException(module.getName(), line,
                                        "Failed to parse identityref base: " + baseIdentityName);
                            }
                            String prefix = splittedBase[0];
                            String name = splittedBase[1];
                            ModuleBuilder dependentModule = findTargetModule(prefix, module, modules, context, line);
                            if (dependentModule != null) {
                                result = BuilderUtils.findIdentity(dependentModule.getAddedIdentities(), name);
                            }
                        } else {
                            result = BuilderUtils.findIdentity(module.getAddedIdentities(), baseIdentityName);
                        }
                        identity.setBaseIdentity(result);
                    }
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
            ModuleBuilder module = BuilderUtils.getParentModule(usesNode);
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
     * Resolve uses statements defined in groupings.
     *
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveUsesForGroupings(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        final Set<GroupingBuilder> allGroupings = new HashSet<>();
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                ModuleBuilder module = inner.getValue();
                allGroupings.addAll(module.getAllGroupings());
            }
        }
        final List<GroupingBuilder> sorted = GroupingSort.sort(allGroupings);
        for (GroupingBuilder gb : sorted) {
            List<UsesNodeBuilder> usesNodes = new ArrayList<>(GroupingSort.getAllUsesNodes(gb));
            Collections.sort(usesNodes, new GroupingUtils.UsesComparator());
            for (UsesNodeBuilder usesNode : usesNodes) {
                resolveUses(usesNode, modules, context);
            }
        }
    }

    /**
     * Resolve uses statements.
     *
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveUsesForNodes(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                ModuleBuilder module = inner.getValue();
                List<UsesNodeBuilder> usesNodes = module.getAllUsesNodes();
                Collections.sort(usesNodes, new GroupingUtils.UsesComparator());
                for (UsesNodeBuilder usesNode : usesNodes) {
                    resolveUses(usesNode, modules, context);
                }
            }
        }
    }

    /**
     * Find target grouping and copy its child nodes to current location with
     * new namespace.
     *
     * @param usesNode
     *            uses node to resolve
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveUses(final UsesNodeBuilder usesNode, final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final SchemaContext context) {
        if (!usesNode.isResolved()) {
            DataNodeContainerBuilder parent = usesNode.getParent();
            ModuleBuilder module = BuilderUtils.getParentModule(parent);
            GroupingBuilder target = GroupingUtils.getTargetGroupingFromModules(usesNode, modules, module);
            if (target == null) {
                resolveUsesWithContext(usesNode);
                usesNode.setResolved(true);
                for (AugmentationSchemaBuilder augment : usesNode.getAugmentations()) {
                    resolveUsesAugment(augment, module, modules, context);
                }
            } else {
                parent.getChildNodeBuilders().addAll(target.instantiateChildNodes(parent));
                parent.getTypeDefinitionBuilders().addAll(target.instantiateTypedefs(parent));
                parent.getGroupingBuilders().addAll(target.instantiateGroupings(parent));
                parent.getUnknownNodes().addAll(target.instantiateUnknownNodes(parent));
                usesNode.setResolved(true);
                for (AugmentationSchemaBuilder augment : usesNode.getAugmentations()) {
                    resolveUsesAugment(augment, module, modules, context);
                }
            }
            GroupingUtils.performRefine(usesNode);
        }
    }

    /**
     * Copy target grouping child nodes to current location with new namespace.
     *
     * @param usesNode
     *            uses node to resolve
     * @param modules
     *            all loaded modules
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveUsesWithContext(final UsesNodeBuilder usesNode) {
        final int line = usesNode.getLine();
        DataNodeContainerBuilder parent = usesNode.getParent();
        ModuleBuilder module = BuilderUtils.getParentModule(parent);
        SchemaPath parentPath;
        URI ns = null;
        Date rev = null;
        String pref = null;
        if (parent instanceof AugmentationSchemaBuilder || parent instanceof ModuleBuilder) {
            ns = module.getNamespace();
            rev = module.getRevision();
            pref = module.getPrefix();
            if (parent instanceof AugmentationSchemaBuilder) {
                parentPath = ((AugmentationSchemaBuilder) parent).getTargetNodeSchemaPath();
            } else {
                parentPath = ((ModuleBuilder) parent).getPath();
            }
        } else {
            ns = ((DataSchemaNodeBuilder) parent).getQName().getNamespace();
            rev = ((DataSchemaNodeBuilder) parent).getQName().getRevision();
            pref = ((DataSchemaNodeBuilder) parent).getQName().getPrefix();
            parentPath = ((DataSchemaNodeBuilder) parent).getPath();
        }

        GroupingDefinition gd = usesNode.getGroupingDefinition();

        Set<DataSchemaNodeBuilder> childNodes = wrapChildNodes(module.getModuleName(), line, gd.getChildNodes(),
                parentPath, ns, rev, pref);
        parent.getChildNodeBuilders().addAll(childNodes);
        for (DataSchemaNodeBuilder childNode : childNodes) {
            setNodeAddedByUses(childNode);
        }

        Set<TypeDefinitionBuilder> typedefs = wrapTypedefs(module.getModuleName(), line, gd, parentPath, ns, rev, pref);
        parent.getTypeDefinitionBuilders().addAll(typedefs);
        for (TypeDefinitionBuilder typedef : typedefs) {
            setNodeAddedByUses(typedef);
        }

        Set<GroupingBuilder> groupings = wrapGroupings(module.getModuleName(), line, usesNode.getGroupingDefinition()
                .getGroupings(), parentPath, ns, rev, pref);
        parent.getGroupingBuilders().addAll(groupings);
        for (GroupingBuilder gb : groupings) {
            setNodeAddedByUses(gb);
        }

        List<UnknownSchemaNodeBuilderImpl> unknownNodes = wrapUnknownNodes(module.getModuleName(), line,
                gd.getUnknownSchemaNodes(), parentPath, ns, rev, pref);
        parent.getUnknownNodes().addAll(unknownNodes);
        for (UnknownSchemaNodeBuilder un : unknownNodes) {
            un.setAddedByUses(true);
        }
    }

    /**
     * Try to find extension builder describing this unknown node and assign it
     * to unknown node builder.
     *
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     */
    private void resolveUnknownNodes(final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        for (UnknownSchemaNodeBuilder usnb : module.getAllUnknownNodes()) {
            QName nodeType = usnb.getNodeType();
            try {
                ModuleBuilder dependentModule = findModuleFromBuilders(modules, module, nodeType.getPrefix(),
                        usnb.getLine());
                for (ExtensionBuilder extension : dependentModule.getAddedExtensions()) {
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

    /**
     * Try to find extension builder describing this unknown node and assign it
     * to unknown node builder. If extension is not found in loaded modules, try
     * to find it in context.
     *
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @param context
     *            SchemaContext containing already resolved modules
     */
    private void resolveUnknownNodesWithContext(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final SchemaContext context) {
        for (UnknownSchemaNodeBuilder usnb : module.getAllUnknownNodes()) {
            QName nodeType = usnb.getNodeType();
            try {
                ModuleBuilder dependentModuleBuilder = findModuleFromBuilders(modules, module, nodeType.getPrefix(),
                        usnb.getLine());

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
                    for (ExtensionBuilder extension : dependentModuleBuilder.getAddedExtensions()) {
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
        for (DeviationBuilder dev : module.getDeviationBuilders()) {
            int line = dev.getLine();
            SchemaPath targetPath = dev.getTargetPath();
            Iterable<QName> path = targetPath.getPathFromRoot();
            QName q0 = path.iterator().next();
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
        for (DeviationBuilder dev : module.getDeviationBuilders()) {
            int line = dev.getLine();
            SchemaPath targetPath = dev.getTargetPath();
            Iterable<QName> path = targetPath.getPathFromRoot();
            QName q0 = path.iterator().next();
            String prefix = q0.getPrefix();
            if (prefix == null) {
                prefix = module.getPrefix();
            }

            ModuleBuilder dependentModuleBuilder = findModuleFromBuilders(modules, module, prefix, line);
            if (dependentModuleBuilder == null) {
                Object currentParent = findModuleFromContext(context, module, prefix, line);

                for (QName q : path) {
                    if (currentParent == null) {
                        throw new YangParseException(module.getName(), line, FAIL_DEVIATION_TARGET);
                    }
                    String name = q.getLocalName();
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
            final Iterable<QName> path, final ModuleBuilder module) {
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
