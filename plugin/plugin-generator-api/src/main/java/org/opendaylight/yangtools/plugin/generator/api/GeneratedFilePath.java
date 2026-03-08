/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.file.Path;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A relative path to a generated file.
 *
 * @param path the path, guaranteed to be non-empty
 */
@NonNullByDefault
public record GeneratedFilePath(String path) {
    /**
     * The separator used in {@code path()} as a character.
     */
    public static final char SEPARATOR = '/';
    /**
     * The separator used in {@code path()} as a string.
     */
    public static final String SEPARATOR_STR = "/";

    /**
     * Default constructor.
     *
     * @param path the path, must not be empty
     */
    public GeneratedFilePath {
        checkArgument(!path.isEmpty());
    }

    public static GeneratedFilePath ofDirectoryFile(final String directory, final String fileName) {
        checkArgument(!directory.isEmpty());
        checkArgument(!fileName.isEmpty());
        return new GeneratedFilePath(directory + SEPARATOR + fileName);
    }

    public static GeneratedFilePath ofPath(final Path path) {
        return new GeneratedFilePath(path.toString().replace(path.getFileSystem().getSeparator(), SEPARATOR_STR));
    }

    @Deprecated(since = "15.0.0", forRemoval = true)
    public String getPath() {
        return path;
    }
}
