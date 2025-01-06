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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
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

    static final FileFilter YANG_FILE_FILTER = file ->
        file.getName().toLowerCase(Locale.ROOT).endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION) && file.isFile();

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

        final var libFiles = new ArrayList<Path>();
        for (var yangLibDir : yangLibDirs) {
            libFiles.addAll(getYangFiles(yangLibDir, recursiveSearch));
        }

        final var testFiles = new ArrayList<Path>();
        for (var yangTestFile : yangTestFiles) {
            if (!yangTestFile.endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION)) {
                testFiles.add(findInFiles(libFiles, yangTestFile));
            } else {
                testFiles.add(Path.of(yangTestFile));
            }
        }

        return parseYangSources(supportedFeatures, testFiles, libFiles, warnForUnkeyedLists);
    }

    static EffectiveModelContext parseYangSources(final Set<QName> supportedFeatures, final List<Path> testFiles,
            final List<Path> libFiles,  final boolean warnForUnkeyedLists) throws IOException, YangParserException {
        checkArgument(!testFiles.isEmpty(), "No yang sources");

        final var configuration = YangParserConfiguration.builder().warnForUnkeyedLists(warnForUnkeyedLists).build();
        final var parser = PARSER_FACTORY.createParser(configuration);
        if (supportedFeatures != null) {
            parser.setSupportedFeatures(FeatureSet.of(supportedFeatures));
        }

        for (var file : testFiles) {
            parser.addSource(new FileYangTextSource(file));
        }
        for (var file : libFiles) {
            parser.addLibSource(new FileYangTextSource(file));
        }

        return parser.buildEffectiveModel();
    }

    private static Path findInFiles(final List<Path> libFiles, final String yangTestFile) throws IOException {
        for (var file : libFiles) {
            if (WHITESPACES.matcher(getModelNameFromFile(file)).replaceAll("").equals(yangTestFile)) {
                return file;
            }
        }
        throw new FileNotFoundException("Model with specific module-name does not exist : " + yangTestFile);
    }

    private static String getModelNameFromFile(final Path file) throws IOException {
        final var matcher = MODULE_PATTERN.matcher(Files.readString(file));
        return matcher.find() ? matcher.group(1) : "";
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static List<Path> getYangFiles(final String yangSourcesDirectoryPath, final boolean recursiveSearch)
            throws FileNotFoundException {
        final var testSourcesDir = Path.of(yangSourcesDirectoryPath);
        if (!Files.isDirectory(testSourcesDir)) {
            throw new FileNotFoundException(yangSourcesDirectoryPath + " no such directory");
        }

        return recursiveSearch ? searchYangFiles(testSourcesDir)
            : Arrays.stream(testSourcesDir.toFile().listFiles(YANG_FILE_FILTER)).map(File::toPath).toList();
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static List<Path> searchYangFiles(final Path dir) {
        requireNonNull(dir);
        checkArgument(Files.isDirectory(dir), "File %s is not a directory", dir);

        final var yangFiles = new ArrayList<Path>();
        for (var file : dir.toFile().listFiles()) {
            final var path = file.toPath();
            if (file.isDirectory()) {
                yangFiles.addAll(searchYangFiles(path));
            } else if (file.isFile()
                    && file.getName().toLowerCase(Locale.ROOT).endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION)) {
                yangFiles.add(path);
            }
        }

        return yangFiles;
    }
}
