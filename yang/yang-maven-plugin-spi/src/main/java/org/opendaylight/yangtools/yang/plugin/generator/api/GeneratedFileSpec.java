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

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;

public final class GeneratedFileSpec {
    private final @NonNull GeneratedFileType type;
    private final @NonNull String path;

    public GeneratedFileSpec(final GeneratedFileType type, final String path) {
        this.type = requireNonNull(type);
        this.path = requireNonNull(path);
        checkArgument(!path.isEmpty());
    }

    public @NonNull GeneratedFileType getType() {
        return type;
    }

    public @NonNull String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GeneratedFileSpec)) {
            return false;
        }
        final GeneratedFileSpec other = (GeneratedFileSpec) obj;
        return type.equals(other.type) && path.equals(other.path);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).add("path", path).toString();
    }
}
