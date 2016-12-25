/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.test.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.IfFeaturePredicates;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;

/**
 * Utility class which provides convenience methods for producing effective schema context based on the supplied
 * yang/yin sources or paths to these sources
 */
@Beta
public final class YangParserTestUtils {

    private static final FileFilter YANG_FILE_FILTER = file -> {
        final String name = file.getName().toLowerCase();
        return name.endsWith(".yang") && file.isFile();
    };

    private YangParserTestUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param sources YANG sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     *
     * @deprecated Migration method only, do not use.
     */
    @Deprecated
    public static SchemaContext parseYangSources(final YangStatementSourceImpl... sources) throws ReactorException {
        return parseYangSources(IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE, sources);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param sources YANG sources to be parsed
     *
     * @return effective schema context
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     *
     * @deprecated Migration method only, do not use.
     */
    @Deprecated
    public static SchemaContext parseYangSources(final Predicate<QName> isFeatureSupported,
            final YangStatementSourceImpl... sources) throws ReactorException {
        return parseYangSources(isFeatureSupported, StatementParserMode.DEFAULT_MODE, sources);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param statementParserMode mode of statement parser
     * @param sources YANG sources to be parsed
     *
     * @return effective schema context
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     *
     * @deprecated Migration method only, do not use.
     */
    @Deprecated
    public static SchemaContext parseYangSources(final StatementParserMode statementParserMode,
            final YangStatementSourceImpl... sources) throws ReactorException {
        return parseYangSources(IfFeaturePredicates.ALL_FEATURES, statementParserMode, sources);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param statementParserMode mode of statement parser
     * @param sources YANG sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangSources(final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode, final YangStatementSourceImpl... sources)
                    throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(
                statementParserMode, isFeatureSupported);
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param files YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified files does not exist
     */
    public static SchemaContext parseYangSources(final File... files) throws ReactorException, FileNotFoundException {
        return parseYangSources(IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE, files);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param files YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified files does not exist
     */
    public static SchemaContext parseYangSources(final Predicate<QName> isFeatureSupported, final File... files)
            throws ReactorException, FileNotFoundException {
        return parseYangSources(isFeatureSupported, StatementParserMode.DEFAULT_MODE, files);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param statementParserMode mode of statement parser
     * @param files YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified files does not exist
     */
    public static SchemaContext parseYangSources(final StatementParserMode statementParserMode, final File... files)
            throws ReactorException, FileNotFoundException {
        return parseYangSources(IfFeaturePredicates.ALL_FEATURES, statementParserMode, files);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param statementParserMode mode of statement parser
     * @param files YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified files does not exist
     */
    public static SchemaContext parseYangSources(final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode, final File... files) throws ReactorException,
            FileNotFoundException {
        final YangStatementSourceImpl[] sources = new YangStatementSourceImpl[files.length];
        for (int i = 0; i < files.length; i++) {
            sources[i] = new YangStatementSourceImpl(new NamedFileInputStream(files[i], files[i].getPath()));
        }

        return parseYangSources(isFeatureSupported, statementParserMode, sources);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param files collection of YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified files does not exist
     */
    public static SchemaContext parseYangSources(final Collection<File> files) throws ReactorException,
            FileNotFoundException {
        return parseYangSources(files, IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param files collection of YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified files does not exist
     */
    public static SchemaContext parseYangSources(final Collection<File> files, final Predicate<QName> isFeatureSupported)
            throws ReactorException, FileNotFoundException {
        return parseYangSources(files, isFeatureSupported, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param statementParserMode mode of statement parser
     * @param files collection of YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified files does not exist
     */
    public static SchemaContext parseYangSources(final Collection<File> files, final StatementParserMode statementParserMode)
            throws ReactorException, FileNotFoundException {
        return parseYangSources(files, IfFeaturePredicates.ALL_FEATURES, statementParserMode);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param statementParserMode mode of statement parser
     * @param files collection of YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified files does not exist
     */
    public static SchemaContext parseYangSources(final Collection<File> files, final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode) throws ReactorException, FileNotFoundException {
        return parseYangSources(isFeatureSupported, statementParserMode, files.toArray(new File[files.size()]));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param yangSourcesDirectoryPath relative path to the directory with YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if the specified directory does not exist
     * @throws URISyntaxException if the specified directory does not exist
     */
    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath) throws ReactorException,
            FileNotFoundException, URISyntaxException {
        return parseYangSources(yangSourcesDirectoryPath, IfFeaturePredicates.ALL_FEATURES,
            StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param yangSourcesDirectoryPath relative path to the directory with YANG files to be parsed
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if the specified directory does not exist
     * @throws URISyntaxException if the specified directory does not exist
     */
    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath,
            final Predicate<QName> isFeatureSupported) throws ReactorException, FileNotFoundException,
            URISyntaxException {
        return parseYangSources(yangSourcesDirectoryPath, isFeatureSupported, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param yangSourcesDirectoryPath relative path to the directory with YANG files to be parsed
     * @param statementParserMode mode of statement parser
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if the specified directory does not exist
     * @throws URISyntaxException if the specified directory does not exist
     */
    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath,
            final StatementParserMode statementParserMode) throws ReactorException, FileNotFoundException,
            URISyntaxException {
        return parseYangSources(yangSourcesDirectoryPath, IfFeaturePredicates.ALL_FEATURES, statementParserMode);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param yangSourcesDirectoryPath relative path to the directory with YANG files to be parsed
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param statementParserMode mode of statement parser
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if the specified directory does not exist
     * @throws URISyntaxException if the specified directory does not exist
     */
    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath,
            final Predicate<QName> isFeatureSupported, final StatementParserMode statementParserMode)
                    throws ReactorException, FileNotFoundException, URISyntaxException {
        final URI directoryPath = YangParserTestUtils.class.getResource(yangSourcesDirectoryPath).toURI();
        final File dir = new File(directoryPath);

        return parseYangSources(isFeatureSupported, statementParserMode, dir.listFiles(YANG_FILE_FILTER));
    }

    /**
     * Creates a new effective schema context containing the specified YANG source. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param yangSourcePath relative path to the YANG file to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in the parsed YANG source
     * @throws FileNotFoundException if the specified file does not exist
     * @throws URISyntaxException if the specified file does not exist
     */
    public static SchemaContext parseYangSource(final String yangSourcePath) throws ReactorException,
            FileNotFoundException, URISyntaxException {
        return parseYangSource(yangSourcePath, IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG source. Statement parser mode is set to
     * default mode.
     *
     * @param yangSourcePath relative path to the YANG file to be parsed
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG model are resolved
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in the parsed YANG source
     * @throws FileNotFoundException if the specified file does not exist
     * @throws URISyntaxException if the specified file does not exist
     */
    public static SchemaContext parseYangSource(final String yangSourcePath, final Predicate<QName> isFeatureSupported)
            throws ReactorException, FileNotFoundException, URISyntaxException {
        return parseYangSource(yangSourcePath, isFeatureSupported, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG source. All YANG features are supported.
     *
     * @param yangSourcePath relative path to the YANG file to be parsed
     * @param statementParserMode mode of statement parser
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in the parsed YANG source
     * @throws FileNotFoundException if the specified file does not exist
     * @throws URISyntaxException if the specified file does not exist
     */
    public static SchemaContext parseYangSource(final String yangSourcePath, final StatementParserMode statementParserMode)
            throws ReactorException, FileNotFoundException, URISyntaxException {
        return parseYangSource(yangSourcePath, IfFeaturePredicates.ALL_FEATURES, statementParserMode);
    }

    /**
     * Creates a new effective schema context containing the specified YANG source.
     *
     * @param yangSourcePath relative path to the YANG file to be parsed
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG model are resolved
     * @param statementParserMode mode of statement parser
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in the parsed YANG source
     * @throws FileNotFoundException if the specified file does not exist
     * @throws URISyntaxException if the specified file does not exist
     */
    public static SchemaContext parseYangSource(final String yangSourcePath, final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode) throws ReactorException, FileNotFoundException,
            URISyntaxException {
        final URI sourcePath = YangParserTestUtils.class.getResource(yangSourcePath).toURI();
        final File sourceFile = new File(sourcePath);
        return parseYangSources(isFeatureSupported, statementParserMode, sourceFile);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param yangDirs relative paths to the directories containing YANG files to be parsed
     * @param yangFiles relative paths to the YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified directories or files does not exist
     * @throws URISyntaxException if one of the specified directories or files does not exist
     */
    public static SchemaContext parseYangSources(final List<String> yangDirs, final List<String> yangFiles)
            throws FileNotFoundException, ReactorException, URISyntaxException {
        return parseYangSources(yangDirs, yangFiles, IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param yangDirs relative paths to the directories containing YANG files to be parsed
     * @param yangFiles relative paths to the YANG files to be parsed
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified directories or files does not exist
     * @throws URISyntaxException if one of the specified directories or files does not exist
     */
    public static SchemaContext parseYangSources(final List<String> yangDirs, final List<String> yangFiles,
            final Predicate<QName> isFeatureSupported) throws FileNotFoundException, ReactorException,
            URISyntaxException {
        return parseYangSources(yangDirs, yangFiles, isFeatureSupported, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param yangDirs relative paths to the directories containing YANG files to be parsed
     * @param yangFiles relative paths to the YANG files to be parsed
     * @param statementParserMode mode of statement parser
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified directories or files does not exist
     * @throws URISyntaxException if one of the specified directories or files does not exist
     */
    public static SchemaContext parseYangSources(final List<String> yangDirs, final List<String> yangFiles,
            final StatementParserMode statementParserMode) throws FileNotFoundException, ReactorException,
            URISyntaxException {
        return parseYangSources(yangDirs, yangFiles, IfFeaturePredicates.ALL_FEATURES, statementParserMode);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param yangDirs relative paths to the directories containing YANG files to be parsed
     * @param yangFiles relative paths to the YANG files to be parsed
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param statementParserMode mode of statement parser
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     * @throws FileNotFoundException if one of the specified directories or files does not exist
     * @throws URISyntaxException if one of the specified directories or files does not exist
     */
    public static SchemaContext parseYangSources(final List<String> yangDirs, final List<String> yangFiles,
            final Predicate<QName> isFeatureSupported, final StatementParserMode statementParserMode)
            throws FileNotFoundException, ReactorException, URISyntaxException {
        final List<File> allYangFiles = new ArrayList<>();
        for (final String yangDir : yangDirs) {
            allYangFiles.addAll(getYangFiles(yangDir));
        }

        for (final String yangFile : yangFiles) {
            final URI filePath = YangParserTestUtils.class.getResource(yangFile).toURI();
            allYangFiles.add(new File(filePath));
        }

        return parseYangSources(allYangFiles, isFeatureSupported, statementParserMode);
    }

    private static Collection<File> getYangFiles(final String yangSourcesDirectoryPath) throws URISyntaxException {
        final URI directoryPath = YangParserTestUtils.class.getResource(yangSourcesDirectoryPath).toURI();
        final File dir = new File(directoryPath);

        return Arrays.asList(dir.listFiles(YANG_FILE_FILTER));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param filePaths relative paths to the YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangSources(final List<String> filePaths) throws ReactorException {
        return parseYangSources(filePaths, IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param filePaths relative paths to the YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangSources(final List<String> filePaths, final Predicate<QName> isFeatureSupported)
            throws ReactorException {
        return parseYangSources(filePaths, isFeatureSupported, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param statementParserMode mode of statement parser
     * @param filePaths relative paths to the YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangSources(final List<String> filePaths,final StatementParserMode statementParserMode)
            throws ReactorException {
        return parseYangSources(filePaths, IfFeaturePredicates.ALL_FEATURES, statementParserMode);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param statementParserMode mode of statement parser
     * @param filePaths relative paths to the YANG files to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangSources(final List<String> filePaths,
            final Predicate<QName> isFeatureSupported, final StatementParserMode statementParserMode)
                    throws ReactorException {
        final YangStatementSourceImpl[] sources = new YangStatementSourceImpl[filePaths.size()];

        for (int i = 0; i < filePaths.size(); i++) {
            sources[i] = new YangStatementSourceImpl(YangParserTestUtils.class.getResourceAsStream(filePaths.get(i)));
        }

        return parseYangSources(isFeatureSupported, statementParserMode, sources);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param streams input streams containing YANG sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangStreams(final List<InputStream> streams) throws ReactorException {
        return parseYangStreams(streams, IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param streams input streams containing YANG sources to be parsed
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangStreams(final List<InputStream> streams, final Predicate<QName> isFeatureSupported)
            throws ReactorException {
        return parseYangStreams(streams, isFeatureSupported, StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param streams input streams containing YANG sources to be parsed
     * @param statementParserMode mode of statement parser
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangStreams(final List<InputStream> streams, final StatementParserMode statementParserMode)
            throws ReactorException {
        return parseYangStreams(streams, IfFeaturePredicates.ALL_FEATURES, statementParserMode);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param statementParserMode mode of statement parser
     * @param streams input streams containing YANG sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangStreams(final List<InputStream> streams, final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode) throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(
                statementParserMode, isFeatureSupported);
        return reactor.buildEffective(streams);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param clazz Resource lookup base
     * @param resources Resource names to be looked up
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangResources(final Class<?> clazz, final String... resources)
            throws ReactorException {
        final List<InputStream> streams = new ArrayList<>(resources.length);
        for (String r : resources) {
            final InputStream is = clazz.getResourceAsStream(r);
            Preconditions.checkArgument(is != null, "Resource %s not found", r);
            streams.add(is);
        }

        return parseYangStreams(streams);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param streams input streams containing YANG sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangStreams(final InputStream... streams) throws ReactorException {
        return parseYangStreams(Arrays.asList(streams));
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. Statement parser mode is set to
     * default mode.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param streams input streams containing YANG sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangStreams(final Predicate<QName> isFeatureSupported,
            final InputStream... streams) throws ReactorException {
        return parseYangStreams(Arrays.asList(streams), isFeatureSupported);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources. All YANG features are supported.
     *
     * @param statementParserMode mode of statement parser
     * @param streams input streams containing YANG sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangStreams(final StatementParserMode statementParserMode,
            final InputStream... streams) throws ReactorException {
        return parseYangStreams(Arrays.asList(streams), statementParserMode);
    }

    /**
     * Creates a new effective schema context containing the specified YANG sources.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YANG models are resolved
     * @param statementParserMode mode of statement parser
     * @param streams input streams containing YANG sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YANG sources
     */
    public static SchemaContext parseYangStreams(final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode, final InputStream... streams) throws ReactorException {
        return parseYangStreams(Arrays.asList(streams), isFeatureSupported, statementParserMode);
    }

    /**
     * Creates a new effective schema context containing the specified YIN sources. Statement parser mode is set to
     * default mode and all YANG features are supported.
     *
     * @param sources YIN sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YIN sources
     */
    public static SchemaContext parseYinSources(final YinStatementStreamSource... sources) throws ReactorException {
        return parseYinSources(IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE, sources);
    }

    /**
     * Creates a new effective schema context containing the specified YIN sources. Statement parser mode is set to
     * default mode.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YIN models are resolved
     * @param sources YIN sources to be parsed
     *
     * @return effective schema context
     * @throws ReactorException if there is an error in one of the parsed YIN sources
     */
    public static SchemaContext parseYinSources(final Predicate<QName> isFeatureSupported,
            final YinStatementStreamSource... sources) throws ReactorException {
        return parseYinSources(isFeatureSupported, StatementParserMode.DEFAULT_MODE, sources);
    }

    /**
     * Creates a new effective schema context containing the specified YIN sources. All YANG features are supported.
     *
     * @param statementParserMode mode of statement parser
     * @param sources YIN sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YIN sources
     */
    public static SchemaContext parseYinSources(final StatementParserMode statementParserMode,
            final YinStatementStreamSource... sources) throws ReactorException {
        return parseYinSources(IfFeaturePredicates.ALL_FEATURES, statementParserMode, sources);
    }

    /**
     * Creates a new effective schema context containing the specified YIN sources.
     *
     * @param isFeatureSupported predicate based on which all if-feature statements in the parsed YIN models are resolved
     * @param statementParserMode mode of statement parser
     * @param sources YIN sources to be parsed
     *
     * @return effective schema context
     *
     * @throws ReactorException if there is an error in one of the parsed YIN sources
     */
    public static SchemaContext parseYinSources(final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode, final YinStatementStreamSource... sources)
                    throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(
                statementParserMode, isFeatureSupported);
        reactor.addSources(sources);

        return reactor.buildEffective();
    }
}
