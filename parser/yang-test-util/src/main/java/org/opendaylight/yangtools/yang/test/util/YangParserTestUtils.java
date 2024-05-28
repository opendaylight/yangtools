/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.test.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.StringYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;

/**
 * Utility class which provides convenience methods for producing effective schema context based on the supplied
 * YANG/YIN sources or paths to these sources.
 */
public final class YangParserTestUtils {
    private static final FileFilter YANG_FILE_FILTER = file -> {
        // Locale keeps SpotBugs happy. It should not matter that much anyway.
        final String name = file.getName().toLowerCase(Locale.ENGLISH);
        return name.endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION) && file.isFile();
    };

    private static final @NonNull YangParserFactory PARSER_FACTORY;

    static {
        final var it = ServiceLoader.load(YangParserFactory.class).iterator();
        if (!it.hasNext()) {
            throw new IllegalStateException("No YangParserFactory found");
        }
        PARSER_FACTORY = it.next();
    }

    private YangParserTestUtils() {
        // Hidden on purpose
    }

    /**
     * Creates a new effective schema context containing the specified YANG source. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param resource relative path to the YANG file to be parsed
     *
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResource(final String resource) {
        return parseYangResource(resource, YangParserConfiguration.DEFAULT);
    }

    /**
     * Creates a new effective schema context containing the specified YANG source. All YANG features are supported.
     *
     * @param resource relative path to the YANG file to be parsed
     * @param config parser configuration
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResource(final String resource,
            final YangParserConfiguration config) {
        return parseYangResource(resource, config, null);
    }

    /**
     * Creates a new effective schema context containing the specified YANG source. Statement parser mode is set to
     * default mode.
     *
     * @param resource relative path to the YANG file to be parsed
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          model are resolved
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResource(final String resource,
            final Set<QName> supportedFeatures) {
        return parseYangResource(resource, YangParserConfiguration.DEFAULT, supportedFeatures);
    }

    /**
     * Creates a new effective schema context containing the specified YANG source.
     *
     * @param resource relative path to the YANG file to be parsed
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          model are resolved
     * @param config parser configuration
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResource(final String resource,
            final YangParserConfiguration config, final Set<QName> supportedFeatures) {
        return parseYangSources(config, supportedFeatures,
            new URLYangTextSource(YangParserTestUtils.class.getResource(resource)));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param files YANG files to be parsed
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangFiles(final File... files) {
        return parseYangFiles(Arrays.asList(files));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param files collection of YANG files to be parsed
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangFiles(final Collection<File> files) {
        return parseYangFiles(YangParserConfiguration.DEFAULT, files);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @param files YANG files to be parsed
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangFiles(final Set<QName> supportedFeatures,
            final File... files) {
        return parseYangFiles(supportedFeatures, Arrays.asList(files));
    }

    public static @NonNull EffectiveModelContext parseYangFiles(final Set<QName> supportedFeatures,
            final Collection<File> files) {
        return parseYangFiles(supportedFeatures, YangParserConfiguration.DEFAULT, files);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param config parser configuration
     * @param files YANG files to be parsed
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangFiles(final YangParserConfiguration config,
            final File... files) {
        return parseYangFiles(config, Arrays.asList(files));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param config parser configuration
     * @param files collection of YANG files to be parsed
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangFiles(final YangParserConfiguration config,
            final Collection<File> files) {
        return parseYangFiles(null, config, files);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @param config parser configuration
     * @param files YANG files to be parsed
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangFiles(final Set<QName> supportedFeatures,
            final YangParserConfiguration config, final File... files) {
        return parseYangFiles(supportedFeatures, config, Arrays.asList(files));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @param config parser configuration
     * @param files YANG files to be parsed
     * @return effective schema context
     */
    //  FIXME: use Java.nio.file.Path
    public static @NonNull EffectiveModelContext parseYangFiles(final Set<QName> supportedFeatures,
            final YangParserConfiguration config, final Collection<File> files) {
        return parseSources(config, supportedFeatures,
            files.stream().map(file -> new FileYangTextSource(file.toPath())).toList());
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param resourcePath relative path to the directory with YANG files to be parsed
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResourceDirectory(final String resourcePath) {
        return parseYangResourceDirectory(resourcePath, YangParserConfiguration.DEFAULT);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param resourcePath relative path to the directory with YANG files to be parsed
     * @param config parser configuration
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResourceDirectory(final String resourcePath,
            final YangParserConfiguration config) {
        return parseYangResourceDirectory(resourcePath, null, config);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param resourcePath relative path to the directory with YANG files to be parsed
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResourceDirectory(final String resourcePath,
            final Set<QName> supportedFeatures) {
        return parseYangResourceDirectory(resourcePath, supportedFeatures, YangParserConfiguration.DEFAULT);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param resourcePath relative path to the directory with YANG files to be parsed
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @param config parser configuration
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResourceDirectory(final String resourcePath,
            final Set<QName> supportedFeatures, final YangParserConfiguration config) {
        final URI directoryPath;
        try {
            directoryPath = YangParserTestUtils.class.getResource(resourcePath).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to open resource " + resourcePath, e);
        }
        return parseYangFiles(supportedFeatures, config, new File(directoryPath).listFiles(YANG_FILE_FILTER));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param clazz Resource lookup base
     * @param resources Resource names to be looked up
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResources(final Class<?> clazz, final String... resources) {
        return parseYangResources(clazz, Arrays.asList(resources));
    }

    public static @NonNull EffectiveModelContext parseYangResources(final Class<?> clazz,
            final Collection<String> resources) {
        return parseSources(YangParserConfiguration.DEFAULT, null, resources.stream()
            .map(resource -> new URLYangTextSource(clazz.getResource(resource)))
            .toList());
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param yangDirs relative paths to the directories containing YANG files to be parsed
     * @param yangFiles relative paths to the YANG files to be parsed
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResources(final List<String> yangDirs,
            final List<String> yangFiles, final Set<QName> supportedFeatures) {
        return parseYangResources(yangDirs, yangFiles, supportedFeatures, YangParserConfiguration.DEFAULT);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param yangResourceDirs relative paths to the directories containing YANG files to be parsed
     * @param yangResources relative paths to the YANG files to be parsed
     * @param config parser configuration
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResources(final List<String> yangResourceDirs,
            final List<String> yangResources, final YangParserConfiguration config) {
        return parseYangResources(yangResourceDirs, yangResources, null, config);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param yangResourceDirs relative paths to the directories containing YANG files to be parsed
     * @param yangResources relative paths to the YANG files to be parsed
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @param config parser configuration
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYangResources(final List<String> yangResourceDirs,
            final List<String> yangResources, final Set<QName> supportedFeatures,
            final YangParserConfiguration config) {
        final List<File> allYangFiles = new ArrayList<>();
        for (final String yangDir : yangResourceDirs) {
            allYangFiles.addAll(getYangFiles(yangDir));
        }

        for (final String yangFile : yangResources) {
            try {
                allYangFiles.add(new File(YangParserTestUtils.class.getResource(yangFile).toURI()));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid resource " + yangFile, e);
            }
        }

        return parseYangFiles(supportedFeatures, config, allYangFiles);
    }

    public static @NonNull EffectiveModelContext parseYangSources(final YangParserConfiguration config,
            final Set<QName> supportedFeatures, final YangTextSource... sources) {
        return parseSources(config, supportedFeatures, Arrays.asList(sources));
    }

    public static @NonNull EffectiveModelContext parseSources(final YangParserConfiguration config,
            final Set<QName> supportedFeatures, final Collection<? extends SourceRepresentation> sources) {
        final YangParser parser = PARSER_FACTORY.createParser(config);
        if (supportedFeatures != null) {
            parser.setSupportedFeatures(FeatureSet.of(supportedFeatures));
        }

        try {
            parser.addSources(sources);
        } catch (YangSyntaxErrorException e) {
            throw new IllegalArgumentException("Malformed source", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read a source", e);
        }

        try {
            return parser.buildEffectiveModel();
        } catch (YangParserException e) {
            throw new IllegalStateException("Failed to assemble SchemaContext", e);
        }
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param sources list of yang sources in plain string
     * @return effective schema context
     */
    public static @NonNull EffectiveModelContext parseYang(final String... sources) {
        return parseSources(YangParserConfiguration.DEFAULT, null,
            Arrays.stream(sources).map(YangParserTestUtils::createYangTextSource).toList());
    }

    private static Collection<File> getYangFiles(final String resourcePath) {
        final URI directoryPath;
        try {
            directoryPath = YangParserTestUtils.class.getResource(resourcePath).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to open resource directory " + resourcePath, e);
        }
        return Arrays.asList(new File(directoryPath).listFiles(YANG_FILE_FILTER));
    }

    /**
     * Create a new {@link YangTextSource} backed by a String input.
     *
     * @param sourceString YANG file as a String
     * @return A new instance.
     * @throws NullPointerException if {@code sourceString} is {@code null}
     * @throws IllegalArgumentException if {@code sourceString} does not a valid YANG body, given a rather restrictive
     *         view of what is valid.
     */
    private static @NonNull StringYangTextSource createYangTextSource(final String sourceString) {
        // First line of a YANG file looks as follows:
        //   `module module-name {`
        // therefore in order to extract the name of the module from a plain string, we are interested in the second
        // word of the first line
        final var firstLine = sourceString.substring(0, sourceString.indexOf("{")).strip().split(" ");
        final var moduleOrSubmoduleString = firstLine[0].strip();
        return switch (moduleOrSubmoduleString) {
            case "module", "submodule" -> {
                final String arg = firstLine[1].strip();
                yield new StringYangTextSource(new SourceIdentifier(Unqualified.of(arg)), sourceString, arg);
            }
            default -> throw new IllegalArgumentException("Unknown statement " + moduleOrSubmoduleString);
        };
    }
}
