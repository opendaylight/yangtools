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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

class SystemTestUtils {

    static final FileFilter YANG_FILE_FILTER = file -> {
        final String name = file.getName().toLowerCase();
        return name.endsWith(".yang") && file.isFile();
    };

    static SchemaContext parseYangSources(final List<String> yangLibDirs, final List<String> yangTestFiles,
            final Set<QName> supportedFeatures, final boolean recursiveSearch) throws ReactorException, IOException,
            YangSyntaxErrorException {
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
            final List<File> libFiles) throws ReactorException, IOException, YangSyntaxErrorException {
        final List<StatementStreamSource> testSources = getYangStatementSources(testFiles);
        final List<StatementStreamSource> libSources = getYangStatementSources(libFiles);
        return parseYangSources(testSources, libSources, supportedFeatures);
    }

    static SchemaContext parseYangSources(final List<StatementStreamSource> testSources,
            final List<StatementStreamSource> libSources, final Set<QName> supportedFeatures) throws ReactorException {
        Preconditions.checkArgument(testSources != null && !testSources.isEmpty(), "No yang sources");

        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(testSources);
        reactor.addLibSources(libSources);

        if (supportedFeatures != null) {
            reactor.setSupportedFeatures(supportedFeatures);
        }

        return reactor.buildEffective();
    }

    private static List<StatementStreamSource> getYangStatementSources(final List<File> yangFiles)
            throws IOException, YangSyntaxErrorException {
        final List<StatementStreamSource> yangSources = new ArrayList<>(yangFiles.size());
        for (File file : yangFiles) {
            yangSources.add(YangStatementStreamSource.create(YangTextSchemaSource.forFile(file)));
        }
        return yangSources;
    }

    private static Collection<File> getYangFiles(final String yangSourcesDirectoryPath, final boolean recursiveSearch)
            throws FileNotFoundException {
        final File testSourcesDir = new File(yangSourcesDirectoryPath);
        if (!testSourcesDir.isDirectory()) {
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
