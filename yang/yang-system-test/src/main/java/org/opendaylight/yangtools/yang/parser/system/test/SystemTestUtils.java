/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.system.test;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
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

    static SchemaContext parseYangSources(final List<String> yangLibDirs, final List<String> yangTestFiles,
            final Set<QName> supportedFeatures, final boolean recursiveSearch) throws FileNotFoundException, ReactorException {
        /*
         * Current dir "." should be always present implicitly in the list of
         * directories where dependencies are searched for
         */
        if (!yangLibDirs.contains(".")) {
            yangLibDirs.add(".");
        }

        final List<File> libFiles = new ArrayList<>();
        for (final String yangLibDir : yangLibDirs) {
            libFiles.addAll(getYangFiles(yangLibDir, recursiveSearch));
        }

        final List<File> testFiles = new ArrayList<>();
        for (final String yangTestFile : yangTestFiles) {
            testFiles.add(new File(yangTestFile));
        }

        return parseYangSources(supportedFeatures, testFiles, libFiles);
    }

    static SchemaContext parseYangSources(final Set<QName> supportedFeatures, final List<File> testFiles,
            final List<File> libFiles) throws FileNotFoundException, ReactorException {
        final StatementStreamSource[] testSources = getYangStatementSources(testFiles);
        final StatementStreamSource[] libSources = getYangStatementSources(libFiles);
        return parseYangSources(testSources, libSources, supportedFeatures);
    }

    static SchemaContext parseYangSources(final StatementStreamSource[] testSources,
            final StatementStreamSource[] libSources, final Set<QName> supportedFeatures) throws ReactorException {

        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild(supportedFeatures);
        reactor.addSources(testSources);
        reactor.addLibSources(libSources);

        return reactor.buildEffective();
    }

    private static StatementStreamSource[] getYangStatementSources(final List<File> yangFiles)
            throws FileNotFoundException {
        final StatementStreamSource[] yangSources = new StatementStreamSource[yangFiles.size()];
        for (int i = 0; i < yangFiles.size(); i++) {
            yangSources[i] = new YangStatementSourceImpl(new NamedFileInputStream(yangFiles.get(i), yangFiles.get(i)
                    .getPath()));
        }
        return yangSources;
    }

    private static Collection<File> getYangFiles(final String yangSourcesDirectoryPath, final boolean recursiveSearch)
            throws FileNotFoundException {
        final File testSourcesDir = new File(yangSourcesDirectoryPath);
        if (testSourcesDir == null || !testSourcesDir.isDirectory()) {
            throw new FileNotFoundException(String.format("%s no such directory", yangSourcesDirectoryPath));
        }

        return recursiveSearch ? searchYangFiles(testSourcesDir) : Arrays.asList(testSourcesDir.listFiles(YANG_FILE_FILTER));
    }

    private static List<File> searchYangFiles(final File dir) {
        Preconditions.checkNotNull(dir);
        Preconditions.checkArgument(dir.isDirectory(), "File %s is not a directory", dir.getPath());

        final List<File> yangFiles = new ArrayList<>();
        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                yangFiles.addAll(searchYangFiles(file));
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".yang")) {
                yangFiles.add(file);
            }
        }

        return yangFiles;
    }
}
