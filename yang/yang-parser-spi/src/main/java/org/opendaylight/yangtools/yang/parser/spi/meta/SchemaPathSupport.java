/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
@Deprecated
public abstract class SchemaPathSupport implements Immutable {
    private static final class Enabled extends SchemaPathSupport {
        @Override
        SchemaPath nullableWrap(final SchemaPath path) {
            return path;
        }
    }

    public static final SchemaPathSupport DEFAULT = new Enabled();

    private SchemaPathSupport() {
        // Hidden on purpose
    }

    public static @Nullable SchemaPath wrap(final @Nullable SchemaPath path) {
        return DEFAULT.nullableWrap(path);
    }

    abstract @Nullable SchemaPath nullableWrap(@Nullable SchemaPath path);
}
