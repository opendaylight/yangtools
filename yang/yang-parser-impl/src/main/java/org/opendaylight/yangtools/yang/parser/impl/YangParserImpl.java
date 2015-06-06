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
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findModuleFromContext;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findSchemaNode;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findSchemaNodeInModule;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.processAugmentation;
import static org.opendaylight.yangtools.yang.parser.builder.impl.TypeUtils.resolveType;
import static org.opendaylight.yangtools.yang.parser.builder.impl.TypeUtils.resolveTypeUnion;
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
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.concurrent.Immutable;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.YangContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
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
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.GroupingUtils;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;
import org.opendaylight.yangtools.yang.parser.util.NamedByteArrayInputStream;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.opendaylight.yangtools.yang.parser.util.NamedInputStream;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Immutable
public final class YangParserImpl implements YangContextParser {
    private static final Logger LOG = LoggerFactory.getLogger(YangParserImpl.class);
    private static final YangParserImpl INSTANCE = new YangParserImpl();

    public static YangParserImpl getInstance() {
        return INSTANCE;
    }

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
    public SchemaContext parseFile(final File yangFile, final File directory) throws IOException, YangSyntaxErrorException {
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

        Map<ByteSource, ModuleBuilder> sourceToBuilder = parseSourcesToBuilders(sourceToFile.keySet(), null);
        ModuleBuilder main = sourceToBuilder.get(mainFileSource);

        List<ModuleBuilder> moduleBuilders = new ArrayList<>();
        moduleBuilders.add(main);
        filterImports(main, new ArrayList<>(sourceToBuilder.values()), moduleBuilders);
        Collection<ModuleBuilder> resolved = resolveSubmodules(moduleBuilders);

        // module builders sorted by dependencies
        List<ModuleBuilder> sortedBuilders = ModuleDependencySort.sort(resolved);
        Map<URI, NavigableMap<Date, ModuleBuilder>> modules = resolveModulesWithImports(sortedBuilders, null);
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
        return parseSources(sources, context);
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
    public SchemaContext parseSources(final Collection<ByteSource> sources) throws IOException,YangSyntaxErrorException {
        return assembleContext(parseYangModelSources(sources, null).values());
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

        final List<ModuleBuilder> sorted = resolveModuleBuilders(sources, context);
        final Map<URI, NavigableMap<Date, ModuleBuilder>> modules = resolveModulesWithImports(sorted, context);

        final Set<Module> unsorted = new LinkedHashSet<>(build(modules).values());
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

    private static Map<URI, NavigableMap<Date, ModuleBuilder>> resolveModulesWithImports(final List<ModuleBuilder> sorted,
            final SchemaContext context) {
        final Map<URI, NavigableMap<Date, ModuleBuilder>> modules = orderModules(sorted);
        for (ModuleBuilder module : sorted) {
            if (module != null) {
                for (ModuleImport imp : module.getImports().values()) {
                    String prefix = imp.getPrefix();
                    ModuleBuilder targetModule = BuilderUtils.findModuleFromBuilders(imp, sorted);
                    if (targetModule == null) {
                        Module result = findModuleFromContext(context, module, prefix, 0);
                        targetModule = new ModuleBuilder(result);
                        NavigableMap<Date, ModuleBuilder> map = modules.get(targetModule.getNamespace());
                        if (map == null) {
                            map = new TreeMap<>();
                            map.put(targetModule.getRevision(), targetModule);
                            modules.put(targetModule.getNamespace(), map);
                        } else {
                            map.put(targetModule.getRevision(), targetModule);
                        }
                    }
                    module.addImportedModule(prefix, targetModule);
                }
            }
        }
        return modules;
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
            byteSourceToModule = parseYangModelSources(byteSourceToFile.keySet(), null);
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
            sourceToModule = parseYangModelSources(sourceToStream.keySet(), null);
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

    public Collection<Module> buildModules(final Collection<ModuleBuilder> builders) {
        Collection<ModuleBuilder> unsorted = resolveSubmodules(builders);
        List<ModuleBuilder> sorted = ModuleDependencySort.sort(unsorted);
        Map<URI, NavigableMap<Date, ModuleBuilder>> modules = resolveModulesWithImports(sorted, null);
        Map<ModuleBuilder, Module> builderToModule = build(modules);
        return builderToModule.values();
    }

    public SchemaContext assembleContext(final Collection<Module> modules) {
        final Set<Module> sorted = new LinkedHashSet<>(
                ModuleDependencySort.sort(modules.toArray(new Module[modules.size()])));
        return resolveSchemaContext(sorted);
    }

    private Map<ByteSource, Module> parseYangModelSources(final Collection<ByteSource> sources, final SchemaContext context) throws IOException, YangSyntaxErrorException {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<ByteSource, ModuleBuilder> sourceToBuilder = resolveSources(sources, context);
        // sort and check for duplicates
        List<ModuleBuilder> sorted = ModuleDependencySort.sort(sourceToBuilder.values());
        Map<URI, NavigableMap<Date, ModuleBuilder>> modules = resolveModulesWithImports(sorted, null);
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
    // TODO: remove ByteSource result after removing YangModelParser
    private Map<ByteSource, ModuleBuilder> resolveSources(final Collection<ByteSource> streams, final SchemaContext context) throws IOException, YangSyntaxErrorException {
        Map<ByteSource, ModuleBuilder> builders = parseSourcesToBuilders(streams, context);
        return resolveSubmodules(builders);
    }

    private static Map<ByteSource, ModuleBuilder> parseSourcesToBuilders(final Collection<ByteSource> sources,
            final SchemaContext context) throws IOException, YangSyntaxErrorException {
        final ParseTreeWalker walker = new ParseTreeWalker();
        final Map<ByteSource, ParseTree> sourceToTree = parseYangSources(sources);
        final Map<ByteSource, ModuleBuilder> sourceToBuilder = new LinkedHashMap<>();

        // validate yang
        new YangModelBasicValidator(walker).validate(sourceToTree.values());

        Map<String, NavigableMap<Date, URI>> namespaceContext = BuilderUtils.createYangNamespaceContext(
                sourceToTree.values(), Optional.fromNullable(context));
        YangParserListenerImpl yangModelParser;
        for (Map.Entry<ByteSource, ParseTree> entry : sourceToTree.entrySet()) {
            ByteSource source = entry.getKey();
            String path = null; // TODO refactor to Optional
            // TODO refactor so that path can be retrieved without opening
            // stream: NamedInputStream -> NamedByteSource ?
            try (InputStream stream = source.openStream()) {
                if (stream instanceof NamedInputStream) {
                    path = stream.toString();
                }
            }
            yangModelParser = new YangParserListenerImpl(namespaceContext, path);
            walker.walk(yangModelParser, entry.getValue());
            ModuleBuilder moduleBuilder = yangModelParser.getModuleBuilder();
            moduleBuilder.setSource(source);
            sourceToBuilder.put(source, moduleBuilder);
        }
        return sourceToBuilder;
    }

    private Map<ByteSource, ModuleBuilder> resolveSubmodules(final Map<ByteSource, ModuleBuilder> builders) {
        Map<ByteSource, ModuleBuilder> modules = new HashMap<>();
        Map<String, NavigableMap<Date, ModuleBuilder>> submodules = new HashMap<>();
        for (Map.Entry<ByteSource, ModuleBuilder> entry : builders.entrySet()) {
            ModuleBuilder builder = entry.getValue();
            if (builder.isSubmodule()) {
                String submoduleName = builder.getName();
                NavigableMap<Date, ModuleBuilder> map = submodules.get(submoduleName);
                if (map == null) {
                    map = new TreeMap<>();
                    map.put(builder.getRevision(), builder);
                    submodules.put(submoduleName, map);
                } else {
                    map.put(builder.getRevision(), builder);
                }
            } else {
                modules.put(entry.getKey(), builder);
            }
        }

        for (ModuleBuilder module : modules.values()) {
            resolveSubmodules(module, submodules);
        }

        return modules;
    }

    private Collection<ModuleBuilder> resolveSubmodules(final Collection<ModuleBuilder> builders) {
        Collection<ModuleBuilder> modules = new HashSet<>();
        Map<String, NavigableMap<Date, ModuleBuilder>> submodules = new HashMap<>();
        for (ModuleBuilder builder : builders) {
            if (builder.isSubmodule()) {
                String submoduleName = builder.getName();
                NavigableMap<Date, ModuleBuilder> map = submodules.get(submoduleName);
                if (map == null) {
                    map = new TreeMap<>();
                    map.put(builder.getRevision(), builder);
                    submodules.put(submoduleName, map);
                } else {
                    map.put(builder.getRevision(), builder);
                }
            } else {
                modules.add(builder);
            }
        }

        for (ModuleBuilder module : modules) {
            resolveSubmodules(module, submodules);
        }

        return modules;
    }

    /**
     * Traverse collection of builders, find builders representing submodule and
     * add this submodule to its parent module.
     *
     * @param module
     *            current module
     * @param submodules
     *            collection all loaded submodules
     * @return collection of module builders with resolved submodules
     */
    private void resolveSubmodules(final ModuleBuilder module,
            final Map<String, NavigableMap<Date, ModuleBuilder>> submodules) {
        Map<String, Date> includes = module.getIncludedModules();
        for (Map.Entry<String, Date> entry : includes.entrySet()) {
            NavigableMap<Date, ModuleBuilder> subs = submodules.get(entry.getKey());
            if (subs == null) {
                throw new YangParseException("Failed to find references submodule " + entry.getKey() + " in module "
                        + module.getName());
            }
            Date rev = entry.getValue();
            ModuleBuilder submodule;
            if (rev == null) {
                submodule = subs.lastEntry().getValue();
            } else {
                submodule = subs.get(rev);
                // FIXME an exception should be thrown after issue with
                // submodule's revisions and namespaces will be resolved
                if (submodule == null) {
                    submodule = subs.lastEntry().getValue();
                }
            }

            if (!submodule.getIncludedModules().isEmpty()) {
                resolveSubmodules(submodule, submodules);
            }
            addSubmoduleToModule(submodule, module);
        }
    }

    private static void addSubmoduleToModule(final ModuleBuilder submodule, final ModuleBuilder module) {
        module.addSubmodule(submodule);
        submodule.setParent(module);
        module.getDirtyNodes().addAll(submodule.getDirtyNodes());
        module.getImports().putAll(submodule.getImports());
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

    private List<ModuleBuilder> resolveModuleBuilders(final Collection<ByteSource> yangFileStreams,
            final SchemaContext context) throws IOException, YangSyntaxErrorException {
        Map<ByteSource, ModuleBuilder> parsedBuilders = resolveSources(yangFileStreams, context);
        ModuleBuilder[] builders = new ModuleBuilder[parsedBuilders.size()];
        parsedBuilders.values().toArray(builders);

        // module dependency graph sorted
        List<ModuleBuilder> sorted;
        if (context == null) {
            sorted = ModuleDependencySort.sort(builders);
        } else {
            sorted = ModuleDependencySort.sortWithContext(context, builders);
        }
        return sorted;
    }

    /**
     * Order modules by namespace and revision.
     *
     * @param modules
     *            topologically sorted modules
     * @return modules ordered by namespace and revision
     */
    private static Map<URI, NavigableMap<Date, ModuleBuilder>> orderModules(final List<ModuleBuilder> modules) {
        final Map<URI, NavigableMap<Date, ModuleBuilder>> result = new LinkedHashMap<>();
        for (final ModuleBuilder builder : modules) {
            if (builder == null) {
                continue;
            }

            URI ns = builder.getNamespace();
            Date rev = builder.getRevision();
            if (rev == null) {
                rev = new Date(0);
            }

            NavigableMap<Date, ModuleBuilder> builderByRevision = result.get(ns);
            if (builderByRevision == null) {
                builderByRevision = new TreeMap<>();
                builderByRevision.put(rev, builder);
                result.put(ns, builderByRevision);
            } else {
                builderByRevision.put(rev, builder);
            }

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
        Map<String, ModuleImport> imports = main.getImports();

        // if this is submodule, add parent to filtered and pick its imports
        if (main.isSubmodule()) {
            NavigableMap<Date, ModuleBuilder> dependencies = new TreeMap<>();
            for (ModuleBuilder mb : other) {
                if (mb.getName().equals(main.getBelongsTo())) {
                    dependencies.put(mb.getRevision(), mb);
                }
            }
            ModuleBuilder parent = dependencies.get(dependencies.firstKey());
            filtered.add(parent);
            imports.putAll(parent.getImports());
        }

        for (ModuleImport mi : imports.values()) {
            for (ModuleBuilder builder : other) {
                if (mi.getModuleName().equals(builder.getModuleName())) {
                    if (mi.getRevision() == null) {
                        if (!filtered.contains(builder)) {
                            filtered.add(builder);
                            filterImports(builder, other, filtered);
                        }
                    } else {
                        if (!filtered.contains(builder) && mi.getRevision().equals(builder.getRevision())) {
                            filtered.add(builder);
                            filterImports(builder, other, filtered);
                        }
                    }
                }
            }
        }
    }

    private static Map<ByteSource, ParseTree> parseYangSources(final Collection<ByteSource> sources) throws IOException, YangSyntaxErrorException {
        final Map<ByteSource, ParseTree> trees = new HashMap<>();
        for (ByteSource source : sources) {
            try (InputStream stream = source.openStream()) {
                trees.put(source, parseYangSource(stream));
            }
        }
        return trees;
    }

    public static YangContext parseYangSource(final InputStream stream) throws IOException, YangSyntaxErrorException {
        final YangLexer lexer = new YangLexer(new ANTLRInputStream(stream));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final YangParser parser = new YangParser(tokens);
        parser.removeErrorListeners();

        final YangErrorListener errorListener = new YangErrorListener();
        parser.addErrorListener(errorListener);

        final YangContext result = parser.yang();
        errorListener.validate();

        return result;
    }

    /**
     * Mini parser: This parsing context does not validate full YANG module,
     * only parses header up to the revisions and imports.
     *
     * @see org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo
     */
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
    private Map<ModuleBuilder, Module> build(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        resolveDirtyNodes(modules);
        resolveAugmentsTargetPath(modules);
        resolveUsesTargetGrouping(modules);
        resolveUsesForGroupings(modules);
        resolveUsesForNodes(modules);
        resolveAugments(modules);
        resolveIdentities(modules);
        checkChoiceCasesForDuplicityQNames(modules);

        // build
        final Map<ModuleBuilder, Module> result = new LinkedHashMap<>();
        for (Map.Entry<URI, NavigableMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
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
    private static void resolveDirtyNodes(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        for (Map.Entry<URI, NavigableMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder module = childEntry.getValue();
                resolveUnknownNodes(modules, module);
                resolveDirtyNodes(modules, module);
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
    private static void resolveDirtyNodes(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final Set<TypeAwareBuilder> dirtyNodes = module.getDirtyNodes();
        if (!dirtyNodes.isEmpty()) {
            for (TypeAwareBuilder nodeToResolve : dirtyNodes) {
                if (nodeToResolve instanceof UnionTypeBuilder) {
                    // special handling for union types
                    resolveTypeUnion((UnionTypeBuilder) nodeToResolve, modules, module);
                } else if (nodeToResolve.getTypedef() instanceof IdentityrefTypeBuilder) {
                    // special handling for identityref types
                    IdentityrefTypeBuilder idref = (IdentityrefTypeBuilder) nodeToResolve.getTypedef();
                    IdentitySchemaNodeBuilder identity = findBaseIdentity(module, idref.getBaseString(),
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

    /**
     * Traverse through augmentations of modules and fix their child nodes
     * schema path.
     *
     * @param modules
     *            all loaded modules
     */
    private void resolveAugmentsTargetPath(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        // collect augments from all loaded modules
        final List<AugmentationSchemaBuilder> allAugments = new ArrayList<>();
        for (Map.Entry<URI, NavigableMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                allAugments.addAll(inner.getValue().getAllAugments());
            }
        }

        for (AugmentationSchemaBuilder augment : allAugments) {
            setCorrectAugmentTargetPath(augment);
        }
    }

    /**
     * Find augment target and set correct schema path for all its child nodes.
     *
     * @param augment
     *            augment to resolve
     */
    private void setCorrectAugmentTargetPath(final AugmentationSchemaBuilder augment) {
        Builder parent = augment.getParent();
        final SchemaPath targetNodeSchemaPath;

        if (parent instanceof UsesNodeBuilder) {
            targetNodeSchemaPath = findUsesAugmentTargetNodePath(((UsesNodeBuilder) parent).getParent(), augment);
        } else {
            targetNodeSchemaPath = augment.getTargetPath();
        }

        for (DataSchemaNodeBuilder childNode : augment.getChildNodeBuilders()) {
            correctPathForAugmentNodes(childNode, targetNodeSchemaPath);
        }
    }

    private static SchemaPath findUsesAugmentTargetNodePath(final DataNodeContainerBuilder usesParent,
            final AugmentationSchemaBuilder augment) {
        QName parentQName = usesParent.getQName();
        final QNameModule qnm;
        if (parentQName == null) {
            ModuleBuilder m = BuilderUtils.getParentModule(usesParent);
            qnm = m.getQNameModule();
        } else {
            qnm = parentQName.getModule();
        }

        SchemaPath path = usesParent.getPath();
        for (QName qname : augment.getTargetPath().getPathFromRoot()) {
            path = path.createChild(QName.create(qnm, qname.getLocalName()));
        }

        return path;
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
    private static void checkAugmentMandatoryNodes(final Collection<AugmentationSchemaBuilder> augments) {
        for (AugmentationSchemaBuilder augment : augments) {
            URI augmentTargetNs = augment.getTargetPath().getPathFromRoot().iterator().next().getNamespace();
            Date augmentTargetRev = augment.getTargetPath().getPathFromRoot().iterator().next().getRevision();
            ModuleBuilder module = BuilderUtils.getParentModule(augment);

            if (augmentTargetNs.equals(module.getNamespace()) && augmentTargetRev.equals(module.getRevision())) {
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
     *            all loaded modules topologically sorted (based on dependencies
     *            between each other)
     */
    private static void resolveAugments(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        List<ModuleBuilder> all = new ArrayList<>();
        for (Map.Entry<URI, NavigableMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                all.add(inner.getValue());
            }
        }

        for (ModuleBuilder mb : all) {
            if (mb != null) {
                List<AugmentationSchemaBuilder> augments = mb.getAllAugments();
                checkAugmentMandatoryNodes(augments);
                Collections.sort(augments, Comparators.AUGMENT_BUILDER_COMP);
                for (AugmentationSchemaBuilder augment : augments) {
                    if (!(augment.isResolved())) {
                        boolean resolved = resolveAugment(augment, mb, modules);
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
     * @return true if augment process succeed
     */
    private static boolean resolveUsesAugment(final AugmentationSchemaBuilder augment, final ModuleBuilder module,
            final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        if (augment.isResolved()) {
            return true;
        }

        UsesNodeBuilder usesNode = (UsesNodeBuilder) augment.getParent();
        DataNodeContainerBuilder parentNode = usesNode.getParent();
        Optional<SchemaNodeBuilder> potentialTargetNode;
        SchemaPath resolvedTargetPath = findUsesAugmentTargetNodePath(parentNode, augment);
        if (parentNode instanceof ModuleBuilder && resolvedTargetPath.isAbsolute()) {
            // Uses is directly used in module body, we lookup
            // We lookup in data namespace to find correct augmentation target
            potentialTargetNode = findSchemaNodeInModule(resolvedTargetPath, (ModuleBuilder) parentNode);
        } else {
            // Uses is used in local context (be it data namespace or grouping
            // namespace,
            // since all nodes via uses are imported to localName, it is safe to
            // to proceed only with local names.
            //
            // Conflicting elements in other namespaces are still not present
            // since resolveUsesAugment occurs before augmenting from external
            // modules.
            potentialTargetNode = Optional.<SchemaNodeBuilder> fromNullable(findSchemaNode(augment.getTargetPath()
                    .getPathFromRoot(), (SchemaNodeBuilder) parentNode));
        }

        if (potentialTargetNode.isPresent()) {
            SchemaNodeBuilder targetNode = potentialTargetNode.get();
            if (targetNode instanceof AugmentationTargetBuilder) {
                fillAugmentTarget(augment, targetNode);
                ((AugmentationTargetBuilder) targetNode).addAugmentation(augment);
                augment.setResolved(true);
                return true;
            } else {
                LOG.warn(
                        "Error in module {} at line {}: Unsupported augment target: {}. Augmentation process skipped.",
                        module.getName(), augment.getLine(), potentialTargetNode);
                augment.setResolved(true);
                augment.setUnsupportedTarget(true);
                return true;
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
     * @return true if augment process succeed
     */
    private static boolean resolveAugment(final AugmentationSchemaBuilder augment, final ModuleBuilder module,
            final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        if (augment.isResolved()) {
            return true;
        }

        QName targetModuleName = augment.getTargetPath().getPathFromRoot().iterator().next();
        ModuleBuilder targetModule = BuilderUtils.findModule(targetModuleName, modules);
        if (targetModule == null) {
            throw new YangParseException(module.getModuleName(), augment.getLine(), "Failed to resolve augment "
                    + augment);
        }

        return processAugmentation(augment, targetModule);
    }

    /**
     * Go through identity statements defined in current module and resolve
     * their 'base' statement.
     *
     * @param modules
     *            all loaded modules
     */
    private static void resolveIdentities(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        for (Map.Entry<URI, NavigableMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                ModuleBuilder module = inner.getValue();
                final Set<IdentitySchemaNodeBuilder> identities = module.getAddedIdentities();
                for (IdentitySchemaNodeBuilder identity : identities) {
                    resolveIdentity(module, identity);
                }
            }
        }
    }

    private static void resolveIdentity(final ModuleBuilder module,
            final IdentitySchemaNodeBuilder identity) {
        final String baseIdentityName = identity.getBaseIdentityName();
        if (baseIdentityName != null) {
            IdentitySchemaNodeBuilder result = null;
            if (baseIdentityName.contains(":")) {
                final int line = identity.getLine();
                String[] splittedBase = baseIdentityName.split(":");
                if (splittedBase.length > 2) {
                    throw new YangParseException(module.getName(), line,
                            "Failed to parse identityref base: "
                                    + baseIdentityName);
                }
                String prefix = splittedBase[0];
                String name = splittedBase[1];

                if (prefix.equals(module.getPrefix())
                        && name.equals(identity.getQName().getLocalName())) {
                    throw new YangParseException(module.getName(),
                            identity.getLine(),
                            "Failed to parse base, identity name equals base identity name: "
                                    + baseIdentityName);
                }

                ModuleBuilder dependentModule = BuilderUtils.getModuleByPrefix(
                        module, prefix);
                result = BuilderUtils.findIdentity(
                        dependentModule.getAddedIdentities(), name);
            } else {
                if (baseIdentityName.equals(identity.getQName().getLocalName())) {
                    throw new YangParseException(module.getName(),
                            identity.getLine(),
                            "Failed to parse base, identity name equals base identity name: "
                                    + baseIdentityName);
                }
                result = BuilderUtils.findIdentity(module.getAddedIdentities(),
                        baseIdentityName);
            }
            identity.setBaseIdentity(result);
        }
    }

    /**
     * Find and add reference of uses target grouping.
     *
     * @param modules
     *            all loaded modules
     */
    private static void resolveUsesTargetGrouping(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        final List<UsesNodeBuilder> allUses = new ArrayList<>();
        for (Map.Entry<URI, NavigableMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                allUses.addAll(inner.getValue().getAllUsesNodes());
            }
        }
        for (UsesNodeBuilder usesNode : allUses) {
            ModuleBuilder module = BuilderUtils.getParentModule(usesNode);
            final GroupingBuilder targetGroupingBuilder = GroupingUtils.getTargetGroupingFromModules(usesNode, modules,
                    module);
            usesNode.setGrouping(targetGroupingBuilder);
        }
    }

    /**
     * Resolve uses statements defined in groupings.
     *
     * @param modules
     *            all loaded modules
     */
    private static void resolveUsesForGroupings(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        final Set<GroupingBuilder> allGroupings = new HashSet<>();
        for (Map.Entry<URI, NavigableMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
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
                resolveUses(usesNode, modules);
            }
        }
    }

    /**
     * Resolve uses statements.
     *
     * @param modules
     *            all loaded modules
     */
    private static void resolveUsesForNodes(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        for (Map.Entry<URI, NavigableMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue().entrySet()) {
                ModuleBuilder module = inner.getValue();
                List<UsesNodeBuilder> usesNodes = module.getAllUsesNodes();
                Collections.sort(usesNodes, new GroupingUtils.UsesComparator());
                for (UsesNodeBuilder usesNode : usesNodes) {
                    resolveUses(usesNode, modules);
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
     */
    private static void resolveUses(final UsesNodeBuilder usesNode, final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        if (!usesNode.isResolved()) {
            DataNodeContainerBuilder parent = usesNode.getParent();
            ModuleBuilder module = BuilderUtils.getParentModule(parent);
            GroupingBuilder target = GroupingUtils.getTargetGroupingFromModules(usesNode, modules, module);

            int index = nodeAfterUsesIndex(usesNode);
            List<DataSchemaNodeBuilder> targetNodes = target.instantiateChildNodes(parent);
            for (DataSchemaNodeBuilder targetNode : targetNodes) {
                parent.addChildNode(index++, targetNode);
            }
            parent.getTypeDefinitionBuilders().addAll(target.instantiateTypedefs(parent));
            parent.getGroupingBuilders().addAll(target.instantiateGroupings(parent));
            parent.getUnknownNodes().addAll(target.instantiateUnknownNodes(parent));
            usesNode.setResolved(true);
            for (AugmentationSchemaBuilder augment : usesNode.getAugmentations()) {
                resolveUsesAugment(augment, module, modules);
            }

            GroupingUtils.performRefine(usesNode);
        }
    }

    private static int nodeAfterUsesIndex(final UsesNodeBuilder usesNode) {
        DataNodeContainerBuilder parent = usesNode.getParent();
        int usesLine = usesNode.getLine();

        List<DataSchemaNodeBuilder> childNodes = parent.getChildNodeBuilders();
        if (childNodes.isEmpty()) {
            return 0;
        }

        DataSchemaNodeBuilder nextNodeAfterUses = null;
        for (DataSchemaNodeBuilder childNode : childNodes) {
            if (!childNode.isAddedByUses() && !childNode.isAugmenting() && childNode.getLine() > usesLine) {
                nextNodeAfterUses = childNode;
                break;
            }
        }

        // uses is declared after child nodes
        if (nextNodeAfterUses == null) {
            return childNodes.size();
        }

        return parent.getChildNodeBuilders().indexOf(nextNodeAfterUses);
    }

    /**
     * Try to find extension describing this unknown node and assign it to
     * unknown node builder.
     *
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     */
    private static void resolveUnknownNodes(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        for (UnknownSchemaNodeBuilder usnb : module.getAllUnknownNodes()) {
            QName nodeType = usnb.getNodeType();
            String localName = usnb.getNodeType().getLocalName();
            ModuleBuilder dependentModule = BuilderUtils.findModule(nodeType, modules);

            if (dependentModule == null) {
                LOG.warn(
                        "Error in module {} at line {}: Failed to resolve node {}: no such extension definition found.",
                        module.getName(), usnb.getLine(), usnb);
                continue;
            }

            ExtensionBuilder extBuilder = findExtBuilder(localName, dependentModule.getAddedExtensions());
            if (extBuilder == null) {
                ExtensionDefinition extDef = findExtDef(localName, dependentModule.getExtensions());
                if (extDef == null) {
                    LOG.warn(
                            "Error in module {} at line {}: Failed to resolve node {}: no such extension definition found.",
                            module.getName(), usnb.getLine(), usnb);
                } else {
                    usnb.setExtensionDefinition(extDef);
                }
            } else {
                usnb.setExtensionBuilder(extBuilder);
            }
        }
    }

    private static ExtensionBuilder findExtBuilder(final String name, final Collection<ExtensionBuilder> extensions) {
        for (ExtensionBuilder extension : extensions) {
            if (extension.getQName().getLocalName().equals(name)) {
                return extension;
            }
        }
        return null;
    }

    private static ExtensionDefinition findExtDef(final String name, final Collection<ExtensionDefinition> extensions) {
        for (ExtensionDefinition extension : extensions) {
            if (extension.getQName().getLocalName().equals(name)) {
                return extension;
            }
        }
        return null;
    }


    /**
     * Traverse through modules and check if choice has choice cases with the
     * same qname.
     *
     * @param modules
     *            all loaded modules
     */
    private void checkChoiceCasesForDuplicityQNames(final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        for (Map.Entry<URI, NavigableMap<Date, ModuleBuilder>> entry : modules.entrySet()) {
            for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue().entrySet()) {
                final ModuleBuilder moduleBuilder = childEntry.getValue();
                final Module module = moduleBuilder.build();
                final List<ChoiceSchemaNode> allChoicesFromModule = getChoicesFrom(module);

                for (ChoiceSchemaNode choiceNode : allChoicesFromModule) {
                    findDuplicityNodesIn(choiceNode, module, moduleBuilder, modules);
                }
            }
        }
    }

    private static void findDuplicityNodesIn(final ChoiceSchemaNode choiceNode, final Module module, final ModuleBuilder moduleBuilder,
            final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        final Set<QName> duplicityTestSet = new HashSet<>();

        for (ChoiceCaseNode choiceCaseNode : choiceNode.getCases()) {

            for (DataSchemaNode childSchemaNode : choiceCaseNode.getChildNodes()) {
                if (!duplicityTestSet.add(childSchemaNode.getQName())) {
                    final Optional<SchemaNodeBuilder> schemaNodeBuilder = BuilderUtils.findSchemaNodeInModule(childSchemaNode.getPath(), moduleBuilder);
                    final String nameOfSchemaNode = childSchemaNode.getQName().getLocalName();
                    int lineOfSchemaNode = 0;

                    if (schemaNodeBuilder.isPresent()) {
                        lineOfSchemaNode = schemaNodeBuilder.get().getLine();
                    }
                    throw new YangParseException(module.getName(), lineOfSchemaNode,
                            String.format("Choice has two nodes case with same qnames - %s", nameOfSchemaNode));
                }
            }
        }
    }

    private List<ChoiceSchemaNode> getChoicesFrom(final Module module) {
        final List<ChoiceSchemaNode> allChoices = new ArrayList<>();

        for (DataSchemaNode dataSchemaNode : module.getChildNodes()) {
            findChoicesIn(dataSchemaNode, allChoices);
        }
        return allChoices;
    }

    private void findChoicesIn(final SchemaNode schemaNode, final Collection<ChoiceSchemaNode> choiceNodes) {
        if (schemaNode instanceof ContainerSchemaNode) {
            final ContainerSchemaNode contSchemaNode = (ContainerSchemaNode) schemaNode;
            for (DataSchemaNode dataSchemaNode : contSchemaNode.getChildNodes()) {
                findChoicesIn(dataSchemaNode, choiceNodes);
            }
        } else if (schemaNode instanceof ListSchemaNode) {
            final ListSchemaNode listSchemaNode = (ListSchemaNode) schemaNode;
            for (DataSchemaNode dataSchemaNode : listSchemaNode.getChildNodes()) {
                findChoicesIn(dataSchemaNode, choiceNodes);
            }
        } else if (schemaNode instanceof ChoiceSchemaNode) {
            choiceNodes.add((ChoiceSchemaNode) schemaNode);
        }
    }

}
