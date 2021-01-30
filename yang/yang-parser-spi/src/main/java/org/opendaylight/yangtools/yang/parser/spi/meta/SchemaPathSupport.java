/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
// FIXME: remove this class once we ditch SchemaPath.getPath()
public abstract class SchemaPathSupport implements Immutable {
    private static final class Enabled extends SchemaPathSupport {
        @Override
        Object effectivePath(final SchemaPath path) {
            return path;
        }

        @Override
        SchemaPath optionalPath(final SchemaPath path) {
            return path;
        }
    }

    private static final SchemaPathSupport DEFAULT = new Enabled();

    private SchemaPathSupport() {
        // Hidden on purpose
    }

    public static @NonNull Object toEffectivePath(final @NonNull SchemaPath path) {
        return DEFAULT.effectivePath(path);
    }

    public static @Nullable SchemaPath toOptionalPath(final @Nullable SchemaPath path) {
        return DEFAULT.optionalPath(path);
    }

    abstract @NonNull Object effectivePath(@NonNull SchemaPath path);

    abstract @Nullable SchemaPath optionalPath(@Nullable SchemaPath path);
}
