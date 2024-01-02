/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.validator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;

final class SystemTestUtils {

    private static final Pattern MODULE_PATTERN = Pattern.compile("module(.*?)\\{");
    private static final Pattern WHITESPACES = Pattern.compile("\\s+");
    private static final @NonNull YangParserFactory PARSER_FACTORY;

    static {
        final Iterator<@NonNull YangParserFactory> it = ServiceLoader.load(YangParserFactory.class).iterator();
        if (!it.hasNext()) {
            throw new IllegalStateException("No YangParserFactory found");
        }
        PARSER_FACTORY = it.next();
    }

    private SystemTestUtils() {
        // Hidden on purpose
    }

    static final FileFilter YANG_FILE_FILTER = file -> {
        final String name = file.getName().toLowerCase(Locale.ENGLISH);
        return name.endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION) && file.isFile();
    };

    static EffectiveModelContext parseYangSources(final List<String> yangLibDirs, final List<String> yangTestFiles,
            final Set<QName> supportedFeatures, final boolean recursiveSearch,
            final boolean warnForUnkeyedLists) throws IOException, YangParserException {
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
            if (!yangTestFile.endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION)) {
                testFiles.add(findInFiles(libFiles, yangTestFile));
            } else {
                testFiles.add(new File(yangTestFile));
            }
        }

        return parseYangSources(supportedFeatures, testFiles, libFiles, warnForUnkeyedLists);
    }

    static EffectiveModelContext parseYangSources(final Set<QName> supportedFeatures, final List<File> testFiles,
            final List<File> libFiles,  final boolean warnForUnkeyedLists) throws IOException, YangParserException {
        checkArgument(!testFiles.isEmpty(), "No yang sources");

        final YangParserConfiguration configuration = YangParserConfiguration.builder()
                .warnForUnkeyedLists(warnForUnkeyedLists).build();
        final YangParser parser = PARSER_FACTORY.createParser(configuration);
        if (supportedFeatures != null) {
            parser.setSupportedFeatures(FeatureSet.of(supportedFeatures));
        }

        for (File file : testFiles) {
            parser.addSource(YangTextSchemaSource.forPath(file.toPath()));
        }
        for (File file : libFiles) {
            parser.addLibSource(YangTextSchemaSource.forPath(file.toPath()));
        }

        return parser.buildEffectiveModel();
    }

    private static File findInFiles(final List<File> libFiles, final String yangTestFile) throws IOException {
        for (final File file : libFiles) {
            if (WHITESPACES.matcher(getModelNameFromFile(file)).replaceAll("").equals(yangTestFile)) {
                return file;
            }
        }
        throw new FileNotFoundException("Model with specific module-name does not exist : " + yangTestFile);
    }

    private static String getModelNameFromFile(final File file) throws IOException {
        final String fileAsString = readFile(file.getAbsolutePath());
        final Matcher matcher = MODULE_PATTERN.matcher(fileAsString);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String readFile(final String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static Collection<File> getYangFiles(final String yangSourcesDirectoryPath, final boolean recursiveSearch)
            throws FileNotFoundException {
        final File testSourcesDir = new File(yangSourcesDirectoryPath);
        if (!testSourcesDir.isDirectory()) {
            throw new FileNotFoundException(String.format("%s no such directory", yangSourcesDirectoryPath));
        }

        return recursiveSearch ? searchYangFiles(testSourcesDir)
            : Arrays.asList(testSourcesDir.listFiles(YANG_FILE_FILTER));
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static List<File> searchYangFiles(final File dir) {
        requireNonNull(dir);
        checkArgument(dir.isDirectory(), "File %s is not a directory", dir.getPath());

        final List<File> yangFiles = new ArrayList<>();
        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                yangFiles.addAll(searchYangFiles(file));
            } else if (file.isFile()
                    && file.getName().toLowerCase(Locale.ENGLISH).endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION)) {
                yangFiles.add(file);
            }
        }

        return yangFiles;
    }
}
