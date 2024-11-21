/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

final class FileSearchUtil {
    private static final String LS = System.lineSeparator();
    static final String TAB = "    ";
    static final String DOUBLE_TAB = TAB.repeat(2);
    static final String TRIPLE_TAB = TAB.repeat(3);

    private FileSearchUtil() {
        // Hidden on purpose
    }

    static void assertFileContains(final File file, final String searchText) throws IOException {
        assertFileContains(Files.readString(file.toPath()), searchText);
    }

    static void assertFileContains(final String fileContent, final String searchText) {
        assertThat(fileContent).contains(searchText);
    }

    static void assertFileContainsConsecutiveLines(final File file, final String fileContent, final String ... lines) {
        for (var line : lines) {
            assertFileContains(fileContent, line);
        }
        assertFileContains(fileContent, String.join(LS, lines));
    }

    static Map<String, File> getFiles(final File path) {
        final var ret = new HashMap<String, File>();
        getFiles(path, ret);
        return ret;
    }

    private static void getFiles(final File path, final Map<String, File> files) {
        for (var file : path.listFiles()) {
            if (file.isDirectory()) {
                getFiles(file, files);
            }

            files.put(file.getName(), file);
        }
    }

    static String tab(final String line) {
        return TAB + line;
    }

    static String doubleTab(final String line) {
        return DOUBLE_TAB + line;
    }

    static String tripleTab(final String line) {
        return TRIPLE_TAB + line;
    }
}
