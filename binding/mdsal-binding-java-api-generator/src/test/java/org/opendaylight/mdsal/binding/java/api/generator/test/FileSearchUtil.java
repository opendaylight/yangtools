/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

final class FileSearchUtil {
    private FileSearchUtil() {
        // Hidden on purpose
    }

    static void assertFileContains(final File file, final String searchText) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().contains(searchText)) {
                    return;
                }
            }
        }
        throw new AssertionError("File " + file + " does not contain '" + searchText + "'");
    }

    static Map<String, File> getFiles(final File path) {
        final Map<String, File> ret = new HashMap<>();
        getFiles(path, ret);
        return ret;
    }

    private static void getFiles(final File path, final Map<String, File> files) {
        final File [] dirFiles = path.listFiles();
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                getFiles(file, files);
            }

            files.put(file.getName(), file);
        }
    }
}
