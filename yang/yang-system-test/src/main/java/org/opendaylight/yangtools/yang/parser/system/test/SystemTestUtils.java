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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;

class SystemTestUtils {

    static final FileFilter YANG_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File file) {
            final String name = file.getName().toLowerCase();
            return name.endsWith(".yang") && file.isFile();
        }
    };

    static SchemaContext parseYangSources(final Collection<File> files, final Predicate<QName> isFeatureSupported)
            throws ReactorException, FileNotFoundException {
        return parseYangSources(files, StatementParserMode.DEFAULT_MODE, isFeatureSupported);
    }

    static SchemaContext parseYangSources(final Collection<File> files,
            final StatementParserMode statementParserMode, final Predicate<QName> isFeatureSupported)
            throws ReactorException, FileNotFoundException {
        return parseYangSources(isFeatureSupported, statementParserMode, files.toArray(new File[files.size()]));
    }

    static SchemaContext parseYangSources(final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode, final File... files) throws ReactorException,
            FileNotFoundException {
        final YangStatementSourceImpl [] sources = new YangStatementSourceImpl[files.length];

        for (int i = 0; i < files.length; i++) {
            sources[i] = new YangStatementSourceImpl(new NamedFileInputStream(files[i], files[i].getPath()));
        }

        return parseYangSources(isFeatureSupported, statementParserMode, sources);
    }

    static SchemaContext parseYangSources(final Predicate<QName> isFeatureSupported,
            final StatementParserMode statementParserMode, final YangStatementSourceImpl... sources)
            throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild(
                statementParserMode, isFeatureSupported);
        reactor.addSources(sources);

        return reactor.buildEffective();
    }

    static SchemaContext parseYangSources(final List<String> yangDirs, final List<String> yangFiles,
            final Predicate<QName> isFeatureSupported) throws FileNotFoundException, ReactorException {
        final List<File> allYangFiles = new ArrayList<>();
        for (final String yangDir : yangDirs) {
            allYangFiles.addAll(getYangFiles(yangDir));
        }

        for (final String yangFile : yangFiles) {
            allYangFiles.add(new File(yangFile));
        }

        return parseYangSources(allYangFiles, isFeatureSupported);
    }

    private static Collection<File> getYangFiles(final String yangSourcesDirectoryPath) {
        final File testSourcesDir = new File(yangSourcesDirectoryPath);
        return Arrays.asList(testSourcesDir.listFiles(YANG_FILE_FILTER));
    }
}
