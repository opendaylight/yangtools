/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.system.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YinStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;

public class YangParserUtils {

    final public static FileFilter YANG_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File file) {
            final String name = file.getName().toLowerCase();
            return name.endsWith(".yang") && file.isFile();
        }
    };

    final public static FileFilter YIN_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File file) {
            final String name = file.getName().toLowerCase();
            return name.endsWith(".xml") && file.isFile();
        }
    };

    private YangParserUtils() {
        new UnsupportedOperationException("Utility class");
    }

    public static void addSources(final CrossSourceStatementReactor.BuildAction reactor,
            final YangStatementSourceImpl... sources) {
        for (final YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }

    public static SchemaContext parseYangSources(final StatementStreamSource... sources) throws SourceException,
            ReactorException {
        return parseYangSources(IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE, sources);
    }

    public static SchemaContext parseYangSources(final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode, final StatementStreamSource... sources)
            throws SourceException, ReactorException {

        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(
                statementParserMode, isFeatureSupported);
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static SchemaContext parseYangSources(final File... files) throws SourceException, ReactorException,
            FileNotFoundException {
        return parseYangSources(IfFeaturePredicates.ALL_FEATURES, StatementParserMode.DEFAULT_MODE, files);
    }

    public static SchemaContext parseYangSources(final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode, final File... files) throws SourceException,
            ReactorException, FileNotFoundException {

        final StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = new YangStatementSourceImpl(new NamedFileInputStream(files[i], files[i].getPath()));
        }

        return parseYangSources(isFeatureSupported, statementParserMode, sources);
    }

    public static SchemaContext parseYangSources(final Collection<File> files, final Predicate<QName> isFeatureSupported)
            throws SourceException, ReactorException, FileNotFoundException {
        return parseYangSources(files, StatementParserMode.DEFAULT_MODE, isFeatureSupported);
    }

    public static SchemaContext parseYangSources(final Collection<File> files,
            final StatementParserMode statementParserMode, final Predicate<QName> isFeatureSupported)
            throws SourceException, ReactorException, FileNotFoundException {
        return parseYangSources(isFeatureSupported, statementParserMode, files.toArray(new File[files.size()]));
    }

    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath) throws SourceException,
            ReactorException, FileNotFoundException, URISyntaxException {
        return parseYangSources(yangSourcesDirectoryPath, StatementParserMode.DEFAULT_MODE);
    }

    public static SchemaContext parseYangSources(final String yangSourcesDirectoryPath,
            final StatementParserMode statementParserMode) throws SourceException, ReactorException,
            FileNotFoundException, URISyntaxException {
        final File testSourcesDir = new File(yangSourcesDirectoryPath);
        return parseYangSources(IfFeaturePredicates.ALL_FEATURES, statementParserMode,
                testSourcesDir.listFiles(YANG_FILE_FILTER));
    }

    public static SchemaContext parseYinSources(final String yinSourcesDirectoryPath,
            final StatementParserMode statementParserMode) throws SourceException, ReactorException,
            FileNotFoundException, URISyntaxException {

        final File testSourcesDir = new File(yinSourcesDirectoryPath);
        return parseYinSources(statementParserMode, testSourcesDir.listFiles(YIN_FILE_FILTER));
    }

    public static SchemaContext parseYinSources(final StatementParserMode statementParserMode, final File... files)
            throws SourceException, ReactorException, FileNotFoundException {

        final StatementStreamSource[] sources = new StatementStreamSource[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = new YinStatementSourceImpl(new NamedFileInputStream(files[i], files[i].getPath()));
        }

        return parseYinSources(statementParserMode, sources);
    }

    public static SchemaContext parseYinSources(final StatementParserMode statementParserMode,
            final StatementStreamSource... sources) throws SourceException, ReactorException {

        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild(statementParserMode);
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    public static SchemaContext parseYangSources(final List<String> yangDirs, final List<String> yangFiles,
            final Predicate<QName> isFeatureSupported) throws SourceException, FileNotFoundException, ReactorException {
        final List<File> allYangFiles = new ArrayList<>();
        for (final String yangDir : yangDirs) {
            allYangFiles.addAll(getYangFiles(yangDir));
        }

        for (final String yangFile : yangFiles) {
            allYangFiles.add(new File(yangFile));
        }

        return parseYangSources(allYangFiles, isFeatureSupported);
    }

    public static Collection<File> getYangFiles(final String yangSourcesDirectoryPath) {
        final File testSourcesDir = new File(yangSourcesDirectoryPath);
        return Arrays.asList(testSourcesDir.listFiles(YANG_FILE_FILTER));
    }
}
