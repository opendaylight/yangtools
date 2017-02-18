/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.plugin.generator.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import java.io.File;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Beta
@NonNullByDefault
public final class GeneratedFilePath {
    public static final char SEPARATOR = '/';
    public static final String SEPARATOR_STR = "/";

    private static final CharMatcher FS_MATCHER = CharMatcher.is(File.separatorChar);

    private final String path;

    private GeneratedFilePath(final String path) {
        this.path = requireNonNull(path);
        checkArgument(!path.isEmpty());
    }

    public static GeneratedFilePath ofPath(final String path) {
        return new GeneratedFilePath(path);
    }

    public static GeneratedFilePath ofFile(final File file) {
        return ofFilePath(file.getPath());
    }

    public static GeneratedFilePath ofFilePath(final String filePath) {
        return ofPath(FS_MATCHER.replaceFrom(filePath, SEPARATOR));
    }

    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof GeneratedFilePath && path.equals(((GeneratedFilePath) obj).path);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("path", path).toString();
    }
}
