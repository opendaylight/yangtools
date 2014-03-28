/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.fillAugmentTarget;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.findBaseIdentity;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.findBaseIdentityFromContext;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.findModuleFromBuilders;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.findModuleFromContext;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.findSchemaNode;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.findSchemaNodeInModule;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.processAugmentation;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.setNodeAddedByUses;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.wrapChildNode;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.wrapChildNodes;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.wrapGroupings;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.wrapTypedefs;
import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.wrapUnknownNodes;
import static org.opendaylight.yangtools.yang.parser.util.TypeUtils.resolveType;
import static org.opendaylight.yangtools.yang.parser.util.TypeUtils.resolveTypeUnion;
import static org.opendaylight.yangtools.yang.parser.util.TypeUtils.resolveTypeUnionWithContext;
import static org.opendaylight.yangtools.yang.parser.util.TypeUtils.resolveTypeWithContext;

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
import org.apache.commons.io.IOUtils;
import org.opendaylight.yangtools.antlrv4.code.gen.YangLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.YangContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.DeviationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ExtensionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder.ModuleImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.GroupingSort;
import org.opendaylight.yangtools.yang.parser.util.GroupingUtils;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;
import org.opendaylight.yangtools.yang.parser.util.NamedByteArrayInputStream;
import org.opendaylight.yangtools.yang.parser.util.NamedInputStream;
import org.opendaylight.yangtools.yang.parser.util.ParserUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.opendaylight.yangtools.yang.validator.YangModelBasicValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public final class YangParserImpl implements YangModelParser {
	private static final Logger LOG = LoggerFactory
			.getLogger(YangParserImpl.class);

	private static final String FAIL_DEVIATION_TARGET = "Failed to find deviation target.";

	@Override
	public Set<Module> parseYangModels(final File yangFile, final File directory) {
		Preconditions.checkState(yangFile.exists(), yangFile
				+ " does not exists");
		Preconditions.checkState(directory.exists(), directory
				+ " does not exists");
		Preconditions.checkState(directory.isDirectory(), directory
				+ " is not a directory");

		final String yangFileName = yangFile.getName();
		final String[] fileList = directory.list();
		checkNotNull(fileList, directory + " not found");

		FileInputStream yangFileStream = null;
		LinkedHashMap<InputStream, File> streamToFileMap = new LinkedHashMap<>();
		try {
			yangFileStream = new FileInputStream(yangFile);
			streamToFileMap.put(yangFileStream, yangFile);
		} catch (FileNotFoundException e) {
			LOG.warn(
					"Exception while reading yang file: " + yangFile.getName(),
					e);
		}

		for (String fileName : fileList) {
			if (fileName.equals(yangFileName)) {
				continue;
			}
			File dependency = new File(directory, fileName);
			try {
				if (dependency.isFile()) {
					streamToFileMap.put(new FileInputStream(dependency),
							dependency);
				}
			} catch (FileNotFoundException e) {
				LOG.warn("Exception while reading yang file: " + fileName, e);
			}
		}

		Map<InputStream, ModuleBuilder> parsedBuilders = parseBuilders(
				new ArrayList<>(streamToFileMap.keySet()),
				new HashMap<ModuleBuilder, InputStream>());
		ModuleBuilder main = parsedBuilders.get(yangFileStream);

		List<ModuleBuilder> moduleBuilders = new ArrayList<>();
		moduleBuilders.add(main);
		filterImports(main, new ArrayList<>(parsedBuilders.values()),
				moduleBuilders);
		Collection<ModuleBuilder> result = resolveSubmodules(moduleBuilders);

		// module builders sorted by dependencies
		ModuleBuilder[] builders = new ModuleBuilder[result.size()];
		result.toArray(builders);
		List<ModuleBuilder> sortedBuilders = ModuleDependencySort
				.sort(builders);
		LinkedHashMap<String, TreeMap<Date, ModuleBuilder>> modules = orderModules(sortedBuilders);
		Collection<Module> unsorted = build(modules).values();
		return new LinkedHashSet<>(ModuleDependencySort.sort(unsorted
				.toArray(new Module[unsorted.size()])));
	}

	@Override
	public Set<Module> parseYangModels(final List<File> yangFiles) {
		Collection<Module> unsorted = parseYangModelsMapped(yangFiles).values();
		return new LinkedHashSet<>(ModuleDependencySort.sort(unsorted
				.toArray(new Module[unsorted.size()])));
	}

	@Override
	public Set<Module> parseYangModels(final List<File> yangFiles,
			final SchemaContext context) {
		if (yangFiles == null) {
			return Collections.emptySet();
		}

		final Map<InputStream, File> inputStreams = new HashMap<>();
		for (final File yangFile : yangFiles) {
			try {
				inputStreams.put(new FileInputStream(yangFile), yangFile);
			} catch (FileNotFoundException e) {
				LOG.warn(
						"Exception while reading yang file: "
								+ yangFile.getName(), e);
			}
		}

		List<InputStream> yangModelStreams = new ArrayList<>(
				inputStreams.keySet());
		Map<ModuleBuilder, InputStream> builderToStreamMap = new HashMap<>();
		Map<String, TreeMap<Date, ModuleBuilder>> modules = resolveModuleBuilders(
				yangModelStreams, builderToStreamMap, null);

		for (InputStream is : inputStreams.keySet()) {
			try {
				is.close();
			} catch (IOException e) {
				LOG.debug("Failed to close stream.");
			}
		}

		final Collection<Module> unsorted = buildWithContext(modules, context)
				.values();
		if (context != null) {
			for (Module m : context.getModules()) {
				if (!unsorted.contains(m)) {
					unsorted.add(m);
				}
			}
		}
		return new LinkedHashSet<>(ModuleDependencySort.sort(unsorted
				.toArray(new Module[unsorted.size()])));
	}

	@Override
	public Set<Module> parseYangModelsFromStreams(
			final List<InputStream> yangModelStreams) {
		Collection<Module> unsorted = parseYangModelsFromStreamsMapped(
				yangModelStreams).values();
		return new LinkedHashSet<>(ModuleDependencySort.sort(unsorted
				.toArray(new Module[unsorted.size()])));
	}

	@Override
	public Set<Module> parseYangModelsFromStreams(
			final List<InputStream> yangModelStreams, SchemaContext context) {
		if (yangModelStreams == null) {
			return Collections.emptySet();
		}

		final Map<ModuleBuilder, InputStream> builderToStreamMap = new HashMap<>();
		final Map<String, TreeMap<Date, ModuleBuilder>> modules = resolveModuleBuilders(
				yangModelStreams, builderToStreamMap, context);
		final Set<Module> unsorted = new LinkedHashSet<>(buildWithContext(
				modules, context).values());
		if (context != null) {
			for (Module m : context.getModules()) {
				if (!unsorted.contains(m)) {
					unsorted.add(m);
				}
			}
		}
		return new LinkedHashSet<>(ModuleDependencySort.sort(unsorted
				.toArray(new Module[unsorted.size()])));
	}

	@Override
	public Map<File, Module> parseYangModelsMapped(List<File> yangFiles) {
		if (yangFiles == null) {
			return Collections.emptyMap();
		}

		final Map<InputStream, File> inputStreams = new HashMap<>();
		for (final File yangFile : yangFiles) {
			try {

				inputStreams.put(new FileInputStream(yangFile), yangFile);
			} catch (FileNotFoundException e) {
				LOG.warn(
						"Exception while reading yang file: "
								+ yangFile.getName(), e);
			}
		}

		List<InputStream> yangModelStreams = new ArrayList<>(
				inputStreams.keySet());
		Map<ModuleBuilder, InputStream> builderToStreamMap = new HashMap<>();
		Map<String, TreeMap<Date, ModuleBuilder>> modules = resolveModuleBuilders(
				yangModelStreams, builderToStreamMap, null);

		for (InputStream is : inputStreams.keySet()) {
			try {
				is.close();
			} catch (IOException e) {
				LOG.debug("Failed to close stream.");
			}
		}

		Map<File, Module> result = new LinkedHashMap<>();
		Map<ModuleBuilder, Module> builderToModuleMap = build(modules);
		Set<ModuleBuilder> keyset = builderToModuleMap.keySet();
		List<ModuleBuilder> sorted = ModuleDependencySort.sort(keyset
				.toArray(new ModuleBuilder[keyset.size()]));
		for (ModuleBuilder key : sorted) {
			result.put(inputStreams.get(builderToStreamMap.get(key)),
					builderToModuleMap.get(key));
		}
		return result;
	}

	// TODO: fix exception handling
	@Override
	public Map<InputStream, Module> parseYangModelsFromStreamsMapped(
			final List<InputStream> yangModelStreams) {
		if (yangModelStreams == null) {
			return Collections.emptyMap();
		}

		// copy input streams so that they can be read more than once
		Map<InputStream/* array backed copy */, InputStream/*
															 * original for
															 * returning
															 */> arrayBackedToOriginalInputStreams = new HashMap<>();
		for (final InputStream originalIS : yangModelStreams) {
			InputStream arrayBackedIs;
			try {
				arrayBackedIs = NamedByteArrayInputStream.create(originalIS);
			} catch (IOException e) {
				// FIXME: throw IOException here
				throw new IllegalStateException(
						"Can not get yang as String from " + originalIS, e);
			}
			arrayBackedToOriginalInputStreams.put(arrayBackedIs, originalIS);
		}

		// it would be better if all code from here used string representation
		// of yang sources instead of input streams
		Map<ModuleBuilder, InputStream> builderToStreamMap = new HashMap<>(); // FIXME:
																				// do
																				// not
																				// modify
																				// input
																				// parameter
		Map<String, TreeMap<Date, ModuleBuilder>> modules = resolveModuleBuilders(
				new ArrayList<>(arrayBackedToOriginalInputStreams.keySet()),
				builderToStreamMap, null);

		// TODO move deeper
		for (TreeMap<Date, ModuleBuilder> value : modules.values()) {
			Collection<ModuleBuilder> values = value.values();
			for (ModuleBuilder builder : values) {
				InputStream is = builderToStreamMap.get(builder);
				try {
					is.reset();
				} catch (IOException e) {
					// this cannot happen because it is ByteArrayInputStream
					throw new IllegalStateException("Possible error in code", e);
				}
				String content;
				try {
					content = IOUtils.toString(is);
				} catch (IOException e) {
					// this cannot happen because it is ByteArrayInputStream
					throw new IllegalStateException("Possible error in code", e);
				}
				builder.setSource(content);
			}
		}

		Map<ModuleBuilder, Module> builderToModuleMap = build(modules);

		Set<ModuleBuilder> keyset = builderToModuleMap.keySet();
		List<ModuleBuilder> sorted = ModuleDependencySort.sort(keyset
				.toArray(new ModuleBuilder[keyset.size()]));
		Map<InputStream, Module> result = new LinkedHashMap<>();
		for (ModuleBuilder key : sorted) {
			Module value = checkNotNull(builderToModuleMap.get(key),
					"Cannot get module for %s", key);
			InputStream arrayBackedIS = checkNotNull(
					builderToStreamMap.get(key), "Cannot get is for %s", key);
			InputStream originalIS = arrayBackedToOriginalInputStreams
					.get(arrayBackedIS);
			result.put(originalIS, value);
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

	// FIXME: why a list is required?
	// FIXME: streamToBuilderMap is output of this method, not input
	private Map<InputStream, ModuleBuilder> parseModuleBuilders(
			List<InputStream> inputStreams,
			Map<ModuleBuilder, InputStream> streamToBuilderMap) {
		Map<InputStream, ModuleBuilder> modules = parseBuilders(inputStreams,
				streamToBuilderMap);
		Map<InputStream, ModuleBuilder> result = resolveSubmodules(modules);
		return result;
	}

	// FIXME: why a list is required?
	// FIXME: streamToBuilderMap is output of this method, not input
	private Map<InputStream, ModuleBuilder> parseBuilders(
			List<InputStream> inputStreams,
			Map<ModuleBuilder, InputStream> streamToBuilderMap) {
		final ParseTreeWalker walker = new ParseTreeWalker();
		final Map<InputStream, ParseTree> trees = parseStreams(inputStreams);
		final Map<InputStream, ModuleBuilder> resBuilders = resolveNamespacesFromYangs(
				trees, streamToBuilderMap, walker);
		final Map<InputStream, ModuleBuilder> builders = new HashMap<>();

		// validate yang
		new YangModelBasicValidator(walker).validate(new ArrayList<>(trees
				.values()));

		YangParserListenerImpl yangModelParser;
		for (Map.Entry<InputStream, ParseTree> entry : trees.entrySet()) {
			InputStream is = entry.getKey();
			String path = null;
			if (is instanceof NamedInputStream) {
				path = is.toString();
			}
			yangModelParser = new YangParserListenerImpl(path);
			yangModelParser.setBuilders(resBuilders.values());
			walker.walk(yangModelParser, entry.getValue());
			ModuleBuilder moduleBuilder = yangModelParser.getModuleBuilder();

			// We expect the order of trees and streams has to be the same
			// FIXME: input parameters should be treated as immutable
			streamToBuilderMap.put(moduleBuilder, entry.getKey());

			builders.put(entry.getKey(), moduleBuilder);
		}

		return builders;
	}

	private Map<InputStream, ModuleBuilder> resolveNamespacesFromYangs(
			Map<InputStream, ParseTree> trees,
			Map<ModuleBuilder, InputStream> streamToBuilderMap,
			ParseTreeWalker walker) {

		YangParserBaseListenerImpl yangListener;
		Map<InputStream, ModuleBuilder> builders = new LinkedHashMap<>();

		for (Map.Entry<InputStream, ParseTree> entry : trees.entrySet()) {
			InputStream is = entry.getKey();
			String path = null;
			if (is instanceof NamedInputStream) {
				path = is.toString();
			}
			yangListener = new YangParserBaseListenerImpl(path);
			walker.walk(yangListener, entry.getValue());
			ModuleBuilder moduleBuilder = yangListener.getModuleBuilder();

			streamToBuilderMap.put(moduleBuilder, entry.getKey());

			builders.put(entry.getKey(), moduleBuilder);
		}

		Set<ModuleBuilder> submodules = new HashSet<>();
		for (ModuleBuilder moduleBuilder : builders.values()) {
			if (moduleBuilder.isSubmodule()) {
				submodules.add(moduleBuilder);
			}
		}

		for (ModuleBuilder submodule : submodules) {
			for (ModuleBuilder module : builders.values()) {
				if (module.getName().equals(submodule.getBelongsTo())) {
					submodule.setNamespace(module.getNamespace());
				}
			}
		}

		return builders;
	}

	private Map<InputStream, ModuleBuilder> resolveSubmodules(
			Map<InputStream, ModuleBuilder> builders) {
		Map<InputStream, ModuleBuilder> modules = new HashMap<>();
		Set<ModuleBuilder> submodules = new HashSet<>();
		for (Map.Entry<InputStream, ModuleBuilder> entry : builders.entrySet()) {
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

	private Collection<ModuleBuilder> resolveSubmodules(
			Collection<ModuleBuilder> builders) {
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

	private void addSubmoduleToModule(ModuleBuilder submodule,
			ModuleBuilder module) {
		submodule.setParent(module);

		module.getDirtyNodes().addAll(submodule.getDirtyNodes());
		module.getModuleImports().addAll(submodule.getModuleImports());
		module.getAugments().addAll(submodule.getAugments());
		module.getAugmentBuilders().addAll(submodule.getAugmentBuilders());
		module.getAllAugments().addAll(submodule.getAllAugments());
		module.getChildNodeBuilders().addAll(submodule.getChildNodeBuilders());
		module.getChildNodes().addAll(submodule.getChildNodes());
		module.getGroupings().addAll(submodule.getGroupings());
		module.getGroupingBuilders().addAll(submodule.getGroupingBuilders());
		module.getTypeDefinitions().addAll(submodule.getTypeDefinitions());
		module.getTypeDefinitionBuilders().addAll(
				submodule.getTypeDefinitionBuilders());
		module.getUsesNodes().addAll(submodule.getUsesNodes());
		module.getUsesNodeBuilders().addAll(submodule.getUsesNodeBuilders());
		module.getAllGroupings().addAll(submodule.getAllGroupings());
		module.getAllUsesNodes().addAll(submodule.getAllUsesNodes());
		module.getRpcs().addAll(submodule.getRpcs());
		module.getAddedRpcs().addAll(submodule.getAddedRpcs());
		module.getNotifications().addAll(submodule.getNotifications());
		module.getAddedNotifications()
				.addAll(submodule.getAddedNotifications());
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

	// FIXME: why a list is required?
	// FIXME: streamToBuilderMap is output of this method, not input
	private Map<String, TreeMap<Date, ModuleBuilder>> resolveModuleBuilders(
			final List<InputStream> yangFileStreams,
			final Map<ModuleBuilder, InputStream> streamToBuilderMap,
			final SchemaContext context) {
		Map<InputStream, ModuleBuilder> parsedBuilders = parseModuleBuilders(
				yangFileStreams, streamToBuilderMap);
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
	private LinkedHashMap<String, TreeMap<Date, ModuleBuilder>> orderModules(
			List<ModuleBuilder> modules) {
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
			TreeMap<Date, ModuleBuilder> builderByRevision = result
					.get(builderName);
			if (builderByRevision == null) {
				builderByRevision = new TreeMap<>();
			}
			builderByRevision.put(builderRevision, builder);
			result.put(builderName, builderByRevision);
		}
		return result;
	}

	private void filterImports(ModuleBuilder main, List<ModuleBuilder> other,
			List<ModuleBuilder> filtered) {
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

	// FIXME: why a list is required?
	private Map<InputStream, ParseTree> parseStreams(
			final List<InputStream> yangStreams) {
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
			YangErrorListener errorListener = new YangErrorListener();
			parser.addErrorListener(errorListener);
			result = parser.yang();
			errorListener.validate();
		} catch (IOException e) {
			// TODO: fix this ASAP
			LOG.warn("Exception while reading yang file: " + yangStream, e);
		}
		return result;
	}

	public static YangContext parseStreamWithoutErrorListeners(
			final InputStream yangStream) {
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
	private Map<ModuleBuilder, Module> build(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
		resolveDirtyNodes(modules);
		resolveAugmentsTargetPath(modules, null);
		resolveUsesTargetGrouping(modules, null);
		resolveUsesForGroupings(modules, null);
		resolveUsesForNodes(modules, null);
		resolveAugments(modules, null);
		resolveDeviations(modules);

		// build
		final Map<ModuleBuilder, Module> result = new LinkedHashMap<>();
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue()
					.entrySet()) {
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
	private Map<ModuleBuilder, Module> buildWithContext(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		resolvedDirtyNodesWithContext(modules, context);
		resolveAugmentsTargetPath(modules, context);
		resolveUsesTargetGrouping(modules, context);
		resolveUsesForGroupings(modules, context);
		resolveUsesForNodes(modules, context);
		resolveAugments(modules, context);
		resolveDeviationsWithContext(modules, context);

		// build
		final Map<ModuleBuilder, Module> result = new LinkedHashMap<>();
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue()
					.entrySet()) {
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
	private void resolveDirtyNodes(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue()
					.entrySet()) {
				final ModuleBuilder module = childEntry.getValue();
				resolveUnknownNodes(modules, module);
				resolveIdentities(modules, module);
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
	private void resolvedDirtyNodesWithContext(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> childEntry : entry.getValue()
					.entrySet()) {
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
	private void resolveDirtyNodes(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final ModuleBuilder module) {
		final Set<TypeAwareBuilder> dirtyNodes = module.getDirtyNodes();
		if (!dirtyNodes.isEmpty()) {
			for (TypeAwareBuilder nodeToResolve : dirtyNodes) {
				if (nodeToResolve instanceof UnionTypeBuilder) {
					// special handling for union types
					resolveTypeUnion((UnionTypeBuilder) nodeToResolve, modules,
							module);
				} else if (nodeToResolve.getTypedef() instanceof IdentityrefTypeBuilder) {
					// special handling for identityref types
					IdentityrefTypeBuilder idref = (IdentityrefTypeBuilder) nodeToResolve
							.getTypedef();
					IdentitySchemaNodeBuilder identity = findBaseIdentity(
							modules, module, idref.getBaseString(),
							idref.getLine());
					if (identity == null) {
						throw new YangParseException(module.getName(),
								idref.getLine(), "Failed to find base identity");
					}
					idref.setBaseIdentity(identity);
					nodeToResolve.setType(idref.build());
				} else {
					resolveType(nodeToResolve, modules, module);
				}
			}
		}
	}

	private void resolveDirtyNodesWithContext(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final ModuleBuilder module, SchemaContext context) {
		final Set<TypeAwareBuilder> dirtyNodes = module.getDirtyNodes();
		if (!dirtyNodes.isEmpty()) {
			for (TypeAwareBuilder nodeToResolve : dirtyNodes) {
				if (nodeToResolve instanceof UnionTypeBuilder) {
					// special handling for union types
					resolveTypeUnionWithContext(
							(UnionTypeBuilder) nodeToResolve, modules, module,
							context);
				} else if (nodeToResolve.getTypedef() instanceof IdentityrefTypeBuilder) {
					// special handling for identityref types
					IdentityrefTypeBuilder idref = (IdentityrefTypeBuilder) nodeToResolve
							.getTypedef();
					IdentitySchemaNodeBuilder identity = findBaseIdentity(
							modules, module, idref.getBaseString(),
							idref.getLine());
					idref.setBaseIdentity(identity);
					nodeToResolve.setType(idref.build());
				} else {
					resolveTypeWithContext(nodeToResolve, modules, module,
							context);
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
	private void resolveAugmentsTargetPath(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			SchemaContext context) {
		// collect augments from all loaded modules
		final List<AugmentationSchemaBuilder> allAugments = new ArrayList<>();
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue()
					.entrySet()) {
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
	private void setCorrectAugmentTargetPath(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final AugmentationSchemaBuilder augment, final SchemaContext context) {
		ModuleBuilder module = ParserUtils.getParentModule(augment);
		SchemaPath oldSchemaPath = augment.getTargetPath();
		List<QName> oldPath = oldSchemaPath.getPath();
		List<QName> newPath = new ArrayList<>();

		Builder parent = augment.getParent();
		if (parent instanceof UsesNodeBuilder) {
			DataNodeContainerBuilder usesParent = ((UsesNodeBuilder) parent)
					.getParent();
			newPath.addAll(usesParent.getPath().getPath());

			URI ns;
			Date revision;
			String prefix;
			QName baseQName = usesParent.getQName();
			if (baseQName == null) {
				ModuleBuilder m = ParserUtils.getParentModule(usesParent);
				ns = m.getNamespace();
				revision = m.getRevision();
				prefix = m.getPrefix();
			} else {
				ns = baseQName.getNamespace();
				revision = baseQName.getRevision();
				prefix = baseQName.getPrefix();
			}

			for (QName qn : oldSchemaPath.getPath()) {
				newPath.add(new QName(ns, revision, prefix, qn.getLocalName()));
			}
		} else {
			for (QName qn : oldPath) {
				URI ns = module.getNamespace();
				Date rev = module.getRevision();
				String localPrefix = qn.getPrefix();
				if (localPrefix != null && !("".equals(localPrefix))) {
					ModuleBuilder currentModule = ParserUtils
							.findModuleFromBuilders(modules, module,
									localPrefix, augment.getLine());
					if (currentModule == null) {
						Module m = ParserUtils.findModuleFromContext(context,
								module, localPrefix, augment.getLine());
						if (m == null) {
							throw new YangParseException(module.getName(),
									augment.getLine(), "Module with prefix "
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
		augment.setTargetNodeSchemaPath(new SchemaPath(newPath, augment
				.getTargetPath().isAbsolute()));

		for (DataSchemaNodeBuilder childNode : augment.getChildNodeBuilders()) {
			correctPathForAugmentNodes(childNode,
					augment.getTargetNodeSchemaPath());
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
	private void correctPathForAugmentNodes(DataSchemaNodeBuilder node,
			SchemaPath parentPath) {
		SchemaPath newPath = ParserUtils.createSchemaPath(parentPath,
				node.getQName());
		node.setPath(newPath);
		if (node instanceof DataNodeContainerBuilder) {
			for (DataSchemaNodeBuilder child : ((DataNodeContainerBuilder) node)
					.getChildNodeBuilders()) {
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
	private void checkAugmentMandatoryNodes(
			Collection<AugmentationSchemaBuilder> augments) {
		for (AugmentationSchemaBuilder augment : augments) {
			String augmentPrefix = augment.getTargetPath().getPath().get(0)
					.getPrefix();
			ModuleBuilder module = ParserUtils.getParentModule(augment);
			String modulePrefix = module.getPrefix();

			if (augmentPrefix == null || augmentPrefix.isEmpty()
					|| augmentPrefix.equals(modulePrefix)) {
				continue;
			}

			for (DataSchemaNodeBuilder childNode : augment
					.getChildNodeBuilders()) {
				if (childNode.getConstraints().isMandatory()) {
					throw new YangParseException(augment.getModuleName(),
							augment.getLine(),
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
	private void resolveAugments(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		List<ModuleBuilder> all = new ArrayList<>();
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue()
					.entrySet()) {
				all.add(inner.getValue());
			}
		}

		List<ModuleBuilder> sorted;
		if (context == null) {
			sorted = ModuleDependencySort.sort(all
					.toArray(new ModuleBuilder[all.size()]));
		} else {
			sorted = ModuleDependencySort.sortWithContext(context,
					all.toArray(new ModuleBuilder[all.size()]));
		}

		for (ModuleBuilder mb : sorted) {
			if (mb != null) {
				List<AugmentationSchemaBuilder> augments = mb.getAllAugments();
				checkAugmentMandatoryNodes(augments);
				Collections.sort(augments, Comparators.AUGMENT_COMP);
				for (AugmentationSchemaBuilder augment : augments) {
					if (!(augment.isResolved())) {
						boolean resolved = resolveAugment(augment, mb, modules,
								context);
						if (!resolved) {
							throw new YangParseException(
									augment.getModuleName(), augment.getLine(),
									"Error in augment parsing: failed to find augment target: "
											+ augment);
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
	private boolean resolveUsesAugment(final AugmentationSchemaBuilder augment,
			final ModuleBuilder module,
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		if (augment.isResolved()) {
			return true;
		}

		UsesNodeBuilder usesNode = (UsesNodeBuilder) augment.getParent();
		DataNodeContainerBuilder parentNode = usesNode.getParent();
		SchemaNodeBuilder targetNode;
		if (parentNode instanceof ModuleBuilder) {
			targetNode = findSchemaNodeInModule(augment.getTargetPath()
					.getPath(), (ModuleBuilder) parentNode);
		} else {
			targetNode = findSchemaNode(augment.getTargetPath().getPath(),
					(SchemaNodeBuilder) parentNode);
		}

		if (targetNode instanceof AugmentationTargetBuilder) {
			fillAugmentTarget(augment, targetNode);
			((AugmentationTargetBuilder) targetNode).addAugmentation(augment);
			augment.setResolved(true);
			return true;
		} else {
			throw new YangParseException(module.getName(), augment.getLine(),
					"Failed to resolve augment in uses. Invalid augment target: "
							+ targetNode);
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
	private boolean resolveAugment(final AugmentationSchemaBuilder augment,
			final ModuleBuilder module,
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		if (augment.isResolved()) {
			return true;
		}

		List<QName> targetPath = augment.getTargetPath().getPath();
		ModuleBuilder targetModule = findTargetModule(targetPath.get(0),
				module, modules, context, augment.getLine());
		if (targetModule == null) {
			throw new YangParseException(module.getModuleName(),
					augment.getLine(), "Failed to resolve augment " + augment);
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
	private ModuleBuilder findTargetModule(final QName qname,
			final ModuleBuilder module,
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context, final int line) {
		ModuleBuilder targetModule = null;

		String prefix = qname.getPrefix();
		if (prefix == null || prefix.equals("")) {
			targetModule = module;
		} else {
			targetModule = findModuleFromBuilders(modules, module,
					qname.getPrefix(), line);
		}

		if (targetModule == null && context != null) {
			Module m = findModuleFromContext(context, module, prefix, line);
			targetModule = new ModuleBuilder(m);
			DataSchemaNode firstNode = m.getDataChildByName(qname
					.getLocalName());
			DataSchemaNodeBuilder firstNodeWrapped = wrapChildNode(
					targetModule.getModuleName(), line, firstNode,
					targetModule.getPath(), firstNode.getQName());
			targetModule.addChildNode(firstNodeWrapped);

			TreeMap<Date, ModuleBuilder> map = new TreeMap<>();
			map.put(targetModule.getRevision(), targetModule);
			modules.put(targetModule.getModuleName(), map);
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
	private void resolveIdentities(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final ModuleBuilder module) {
		final Set<IdentitySchemaNodeBuilder> identities = module
				.getAddedIdentities();
		for (IdentitySchemaNodeBuilder identity : identities) {
			final String baseIdentityName = identity.getBaseIdentityName();
			final int line = identity.getLine();
			if (baseIdentityName != null) {
				IdentitySchemaNodeBuilder baseIdentity = findBaseIdentity(
						modules, module, baseIdentityName, line);
				if (baseIdentity == null) {
					throw new YangParseException(module.getName(),
							identity.getLine(), "Failed to find base identity");
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
	private void resolveIdentitiesWithContext(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final ModuleBuilder module, final SchemaContext context) {
		final Set<IdentitySchemaNodeBuilder> identities = module
				.getAddedIdentities();
		for (IdentitySchemaNodeBuilder identity : identities) {
			final String baseIdentityName = identity.getBaseIdentityName();
			final int line = identity.getLine();
			if (baseIdentityName != null) {
				IdentitySchemaNodeBuilder baseIdentity = findBaseIdentity(
						modules, module, baseIdentityName, line);
				if (baseIdentity == null) {
					IdentitySchemaNode baseId = findBaseIdentityFromContext(
							modules, module, baseIdentityName, line, context);
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
	private void resolveUsesTargetGrouping(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		final List<UsesNodeBuilder> allUses = new ArrayList<>();
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue()
					.entrySet()) {
				allUses.addAll(inner.getValue().getAllUsesNodes());
			}
		}
		for (UsesNodeBuilder usesNode : allUses) {
			ModuleBuilder module = ParserUtils.getParentModule(usesNode);
			final GroupingBuilder targetGroupingBuilder = GroupingUtils
					.getTargetGroupingFromModules(usesNode, modules, module);
			if (targetGroupingBuilder == null) {
				if (context == null) {
					throw new YangParseException(module.getName(),
							usesNode.getLine(), "Referenced grouping '"
									+ usesNode.getGroupingPathAsString()
									+ "' not found.");
				} else {
					GroupingDefinition targetGroupingDefinition = GroupingUtils
							.getTargetGroupingFromContext(usesNode, module,
									context);
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
	private void resolveUsesForGroupings(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		final Set<GroupingBuilder> allGroupings = new HashSet<>();
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue()
					.entrySet()) {
				ModuleBuilder module = inner.getValue();
				allGroupings.addAll(module.getAllGroupings());
			}
		}
		final List<GroupingBuilder> sorted = GroupingSort.sort(allGroupings);
		for (GroupingBuilder gb : sorted) {
			List<UsesNodeBuilder> usesNodes = new ArrayList<>(
					GroupingSort.getAllUsesNodes(gb));
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
	private void resolveUsesForNodes(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue()
					.entrySet()) {
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
	private void resolveUses(UsesNodeBuilder usesNode,
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		if (!usesNode.isResolved()) {
			DataNodeContainerBuilder parent = usesNode.getParent();
			ModuleBuilder module = ParserUtils.getParentModule(parent);
			GroupingBuilder target = GroupingUtils
					.getTargetGroupingFromModules(usesNode, modules, module);
			if (target == null) {
				resolveUsesWithContext(usesNode);
				usesNode.setResolved(true);
				for (AugmentationSchemaBuilder augment : usesNode
						.getAugmentations()) {
					resolveUsesAugment(augment, module, modules, context);
				}
			} else {
				parent.getChildNodeBuilders().addAll(
						target.instantiateChildNodes(parent));
				parent.getTypeDefinitionBuilders().addAll(
						target.instantiateTypedefs(parent));
				parent.getGroupingBuilders().addAll(
						target.instantiateGroupings(parent));
				parent.getUnknownNodes().addAll(
						target.instantiateUnknownNodes(parent));
				usesNode.setResolved(true);
				for (AugmentationSchemaBuilder augment : usesNode
						.getAugmentations()) {
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
	private void resolveUsesWithContext(UsesNodeBuilder usesNode) {
		final int line = usesNode.getLine();
		DataNodeContainerBuilder parent = usesNode.getParent();
		ModuleBuilder module = ParserUtils.getParentModule(parent);
		SchemaPath parentPath;
		URI ns = null;
		Date rev = null;
		String pref = null;
		if (parent instanceof AugmentationSchemaBuilder
				|| parent instanceof ModuleBuilder) {
			ns = module.getNamespace();
			rev = module.getRevision();
			pref = module.getPrefix();
			if (parent instanceof AugmentationSchemaBuilder) {
				parentPath = ((AugmentationSchemaBuilder) parent)
						.getTargetNodeSchemaPath();
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

		Set<DataSchemaNodeBuilder> childNodes = wrapChildNodes(
				module.getModuleName(), line, gd.getChildNodes(), parentPath,
				ns, rev, pref);
		parent.getChildNodeBuilders().addAll(childNodes);
		for (DataSchemaNodeBuilder childNode : childNodes) {
			setNodeAddedByUses(childNode);
		}

		Set<TypeDefinitionBuilder> typedefs = wrapTypedefs(
				module.getModuleName(), line, gd, parentPath, ns, rev, pref);
		parent.getTypeDefinitionBuilders().addAll(typedefs);
		for (TypeDefinitionBuilder typedef : typedefs) {
			setNodeAddedByUses(typedef);
		}

		Set<GroupingBuilder> groupings = wrapGroupings(module.getModuleName(),
				line, usesNode.getGroupingDefinition().getGroupings(),
				parentPath, ns, rev, pref);
		parent.getGroupingBuilders().addAll(groupings);
		for (GroupingBuilder gb : groupings) {
			setNodeAddedByUses(gb);
		}

		List<UnknownSchemaNodeBuilder> unknownNodes = wrapUnknownNodes(
				module.getModuleName(), line, gd.getUnknownSchemaNodes(),
				parentPath, ns, rev, pref);
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
	private void resolveUnknownNodes(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final ModuleBuilder module) {
		for (UnknownSchemaNodeBuilder usnb : module.getAllUnknownNodes()) {
			QName nodeType = usnb.getNodeType();
			try {
				ModuleBuilder dependentModule = findModuleFromBuilders(modules,
						module, nodeType.getPrefix(), usnb.getLine());
				for (ExtensionBuilder extension : dependentModule
						.getAddedExtensions()) {
					if (extension.getQName().getLocalName()
							.equals(nodeType.getLocalName())) {
						usnb.setNodeType(extension.getQName());
						usnb.setExtensionBuilder(extension);
						break;
					}
				}
			} catch (YangParseException e) {
				throw new YangParseException(module.getName(), usnb.getLine(),
						"Failed to resolve node " + usnb
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
	private void resolveUnknownNodesWithContext(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final ModuleBuilder module, final SchemaContext context) {
		for (UnknownSchemaNodeBuilder usnb : module.getAllUnknownNodes()) {
			QName nodeType = usnb.getNodeType();
			try {
				ModuleBuilder dependentModuleBuilder = findModuleFromBuilders(
						modules, module, nodeType.getPrefix(), usnb.getLine());

				if (dependentModuleBuilder == null) {
					Module dependentModule = findModuleFromContext(context,
							module, nodeType.getPrefix(), usnb.getLine());
					for (ExtensionDefinition e : dependentModule
							.getExtensionSchemaNodes()) {
						if (e.getQName().getLocalName()
								.equals(nodeType.getLocalName())) {
							usnb.setNodeType(new QName(e.getQName()
									.getNamespace(),
									e.getQName().getRevision(), nodeType
											.getPrefix(), e.getQName()
											.getLocalName()));
							usnb.setExtensionDefinition(e);
							break;
						}
					}
				} else {
					for (ExtensionBuilder extension : dependentModuleBuilder
							.getAddedExtensions()) {
						if (extension.getQName().getLocalName()
								.equals(nodeType.getLocalName())) {
							usnb.setExtensionBuilder(extension);
							break;
						}
					}
				}

			} catch (YangParseException e) {
				throw new YangParseException(module.getName(), usnb.getLine(),
						"Failed to resolve node " + usnb
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
	private void resolveDeviations(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules) {
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue()
					.entrySet()) {
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
	private void resolveDeviation(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final ModuleBuilder module) {
		for (DeviationBuilder dev : module.getDeviationBuilders()) {
			int line = dev.getLine();
			SchemaPath targetPath = dev.getTargetPath();
			List<QName> path = targetPath.getPath();
			QName q0 = path.get(0);
			String prefix = q0.getPrefix();
			if (prefix == null) {
				prefix = module.getPrefix();
			}

			ModuleBuilder dependentModuleBuilder = findModuleFromBuilders(
					modules, module, prefix, line);
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
	private void resolveDeviationsWithContext(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final SchemaContext context) {
		for (Map.Entry<String, TreeMap<Date, ModuleBuilder>> entry : modules
				.entrySet()) {
			for (Map.Entry<Date, ModuleBuilder> inner : entry.getValue()
					.entrySet()) {
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
	private void resolveDeviationWithContext(
			final Map<String, TreeMap<Date, ModuleBuilder>> modules,
			final ModuleBuilder module, final SchemaContext context) {
		for (DeviationBuilder dev : module.getDeviationBuilders()) {
			int line = dev.getLine();
			SchemaPath targetPath = dev.getTargetPath();
			List<QName> path = targetPath.getPath();
			QName q0 = path.get(0);
			String prefix = q0.getPrefix();
			if (prefix == null) {
				prefix = module.getPrefix();
			}

			ModuleBuilder dependentModuleBuilder = findModuleFromBuilders(
					modules, module, prefix, line);
			if (dependentModuleBuilder == null) {
				Object currentParent = findModuleFromContext(context, module,
						prefix, line);

				for (QName q : path) {
					if (currentParent == null) {
						throw new YangParseException(module.getName(), line,
								FAIL_DEVIATION_TARGET);
					}
					String name = q.getLocalName();
					if (currentParent instanceof DataNodeContainer) {
						currentParent = ((DataNodeContainer) currentParent)
								.getDataChildByName(name);
					}
				}

				if (currentParent == null) {
					throw new YangParseException(module.getName(), line,
							FAIL_DEVIATION_TARGET);
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
	private void processDeviation(final DeviationBuilder dev,
			final ModuleBuilder dependentModuleBuilder, final List<QName> path,
			final ModuleBuilder module) {
		final int line = dev.getLine();
		Builder currentParent = dependentModuleBuilder;

		for (QName q : path) {
			if (currentParent == null) {
				throw new YangParseException(module.getName(), line,
						FAIL_DEVIATION_TARGET);
			}
			String name = q.getLocalName();
			if (currentParent instanceof DataNodeContainerBuilder) {
				currentParent = ((DataNodeContainerBuilder) currentParent)
						.getDataChildByName(name);
			}
		}

		if (!(currentParent instanceof SchemaNodeBuilder)) {
			throw new YangParseException(module.getName(), line,
					FAIL_DEVIATION_TARGET);
		}
		dev.setTargetPath(((SchemaNodeBuilder) currentParent).getPath());
	}

}
