/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.test.util;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

/**
 * Utility class which provides convenience methods for producing effective schema context based on the supplied
 * yang/yin sources or paths to these sources.
 */
@Beta
public final class YangParserTestUtils {

    private static final FileFilter YANG_FILE_FILTER = file -> {
        // Locale keeps SpotBugs happy. It should not matter that much anyway.
        final String name = file.getName().toLowerCase(Locale.ENGLISH);
        return name.endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION) && file.isFile();
    };

    private static final @NonNull YangParserFactory PARSER_FACTORY;

    static {
        final Iterator<@NonNull YangParserFactory> it = ServiceLoader.load(YangParserFactory.class).iterator();
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
    public static EffectiveModelContext parseYangResource(final String resource) {
        return parseYangResource(resource, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG source. All YANG features are supported.
     *
     * @param resource relative path to the YANG file to be parsed
     * @param parserMode mode of statement parser
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangResource(final String resource, final StatementParserMode parserMode) {
        return parseYangResource(resource, parserMode, null);
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
    public static EffectiveModelContext parseYangResource(final String resource, final Set<QName> supportedFeatures) {
        return parseYangResource(resource, StatementParserMode.DEFAULT_MODE, supportedFeatures);
    }

    /**
     * Creates a new effective schema context containing the specified YANG source.
     *
     * @param resource relative path to the YANG file to be parsed
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          model are resolved
     * @param parserMode mode of statement parser
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangResource(final String resource, final StatementParserMode parserMode,
            final Set<QName> supportedFeatures) {
        final YangTextSchemaSource source = YangTextSchemaSource.forResource(YangParserTestUtils.class, resource);
        return parseYangSources(parserMode, supportedFeatures, source);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param files YANG files to be parsed
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangFiles(final File... files) {
        return parseYangFiles(Arrays.asList(files));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param files collection of YANG files to be parsed
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangFiles(final Collection<File> files) {
        return parseYangFiles(StatementParserMode.DEFAULT_MODE, files);
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
    public static EffectiveModelContext parseYangFiles(final Set<QName> supportedFeatures, final File... files) {
        return parseYangFiles(supportedFeatures, Arrays.asList(files));
    }

    public static EffectiveModelContext parseYangFiles(final Set<QName> supportedFeatures,
            final Collection<File> files) {
        return parseYangFiles(supportedFeatures, StatementParserMode.DEFAULT_MODE, files);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param parserMode mode of statement parser
     * @param files YANG files to be parsed
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangFiles(final StatementParserMode parserMode, final File... files) {
        return parseYangFiles(parserMode, Arrays.asList(files));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param parserMode mode of statement parser
     * @param files collection of YANG files to be parsed
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangFiles(final StatementParserMode parserMode,
            final Collection<File> files) {
        return parseYangFiles(null, parserMode, files);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @param parserMode mode of statement parser
     * @param files YANG files to be parsed
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangFiles(final Set<QName> supportedFeatures,
            final StatementParserMode parserMode, final File... files) {
        return parseYangFiles(supportedFeatures, parserMode, Arrays.asList(files));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @param parserMode mode of statement parser
     * @param files YANG files to be parsed
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangFiles(final Set<QName> supportedFeatures,
            final StatementParserMode parserMode, final Collection<File> files) {
        return parseSources(parserMode, supportedFeatures,
            files.stream().map(YangTextSchemaSource::forFile).collect(Collectors.toList()));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param resourcePath relative path to the directory with YANG files to be parsed
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangResourceDirectory(final String resourcePath) {
        return parseYangResourceDirectory(resourcePath, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param resourcePath relative path to the directory with YANG files to be parsed
     * @param parserMode mode of statement parser
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangResourceDirectory(final String resourcePath,
            final StatementParserMode parserMode) {
        return parseYangResourceDirectory(resourcePath, null, parserMode);
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
    public static EffectiveModelContext parseYangResourceDirectory(final String resourcePath,
            final Set<QName> supportedFeatures) {
        return parseYangResourceDirectory(resourcePath, supportedFeatures, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param resourcePath relative path to the directory with YANG files to be parsed
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @param parserMode mode of statement parser
     * @return effective schema context
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Wrong inferent on listFiles")
    public static EffectiveModelContext parseYangResourceDirectory(final String resourcePath,
            final Set<QName> supportedFeatures, final StatementParserMode parserMode) {
        final URI directoryPath;
        try {
            directoryPath = YangParserTestUtils.class.getResource(resourcePath).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to open resource " + resourcePath, e);
        }
        return parseYangFiles(supportedFeatures, parserMode, new File(directoryPath).listFiles(YANG_FILE_FILTER));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param clazz Resource lookup base
     * @param resources Resource names to be looked up
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangResources(final Class<?> clazz, final String... resources) {
        return parseYangResources(clazz, Arrays.asList(resources));
    }

    public static EffectiveModelContext parseYangResources(final Class<?> clazz, final Collection<String> resources) {
        final List<YangTextSchemaSource> sources = new ArrayList<>(resources.size());
        for (final String r : resources) {
            sources.add(YangTextSchemaSource.forResource(clazz, r));
        }
        return parseSources(StatementParserMode.DEFAULT_MODE, null, sources);
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
    public static EffectiveModelContext parseYangResources(final List<String> yangDirs, final List<String> yangFiles,
            final Set<QName> supportedFeatures) {
        return parseYangResources(yangDirs, yangFiles, supportedFeatures, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param yangResourceDirs relative paths to the directories containing YANG files to be parsed
     * @param yangResources relative paths to the YANG files to be parsed
     * @param statementParserMode mode of statement parser
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangResources(final List<String> yangResourceDirs,
            final List<String> yangResources, final StatementParserMode statementParserMode) {
        return parseYangResources(yangResourceDirs, yangResources, null, statementParserMode);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param yangResourceDirs relative paths to the directories containing YANG files to be parsed
     * @param yangResources relative paths to the YANG files to be parsed
     * @param supportedFeatures set of supported features based on which all if-feature statements in the parsed YANG
     *                          models are resolved
     * @param statementParserMode mode of statement parser
     * @return effective schema context
     */
    public static EffectiveModelContext parseYangResources(final List<String> yangResourceDirs,
            final List<String> yangResources, final Set<QName> supportedFeatures,
            final StatementParserMode statementParserMode) {
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

        return parseYangFiles(supportedFeatures, statementParserMode, allYangFiles);
    }

    public static EffectiveModelContext parseYangSources(final StatementParserMode parserMode,
            final Set<QName> supportedFeatures, final YangTextSchemaSource... sources) {
        return parseSources(parserMode, supportedFeatures, Arrays.asList(sources));
    }

    public static EffectiveModelContext parseSources(final StatementParserMode parserMode,
            final Set<QName> supportedFeatures, final Collection<? extends SchemaSourceRepresentation> sources) {
        final YangParser parser = PARSER_FACTORY.createParser(parserMode);
        if (supportedFeatures != null) {
            parser.setSupportedFeatures(supportedFeatures);
        }

        try {
            parser.addSources(sources);
        } catch (YangSyntaxErrorException e) {
            throw new IllegalArgumentException("Malformed source", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read a source", e);
        }

        final EffectiveModelContext result;

        try {
            result = parser.buildEffectiveModel();
        } catch (YangParserException e) {
            throw new IllegalStateException("Failed to assemble SchemaContext", e);
        }

        System.out.println(parser);
        return result;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Wrong inferent on listFiles")
    private static Collection<File> getYangFiles(final String resourcePath) {
        final URI directoryPath;
        try {
            directoryPath = YangParserTestUtils.class.getResource(resourcePath).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to open resource directory " + resourcePath, e);
        }
        return Arrays.asList(new File(directoryPath).listFiles(YANG_FILE_FILTER));
    }
}
