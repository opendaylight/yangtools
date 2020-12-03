/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
// FIXME: remove this class once we ditch SchemaPath.getPath()
public abstract class SchemaPathSupport implements Immutable {
    private static final class Disabled extends SchemaPathSupport {
        @Override
        QName effectivePath(final SchemaPath path) {
            return verifyNotNull(path.getLastComponent());
        }

        @Override
        SchemaPath optionalPath(final SchemaPath path) {
            return null;
        }

        @Override
        boolean equalPaths(final SchemaPath first, final SchemaPath second) {
            return true;
        }
    }

    private static final class Enabled extends SchemaPathSupport {
        @Override
        SchemaPath effectivePath(final SchemaPath path) {
            return path;
        }

        @Override
        SchemaPath optionalPath(final SchemaPath path) {
            return path;
        }

        @Override
        boolean equalPaths(final SchemaPath first, final SchemaPath second) {
            return Objects.equals(first, second);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SchemaPathSupport.class);
    private static final String ENABLE_PROPERTY = "org.opendaylight.yangtools.schemanode.getpath";

    private static final SchemaPathSupport DEFAULT;

    static {
        // Forbid creating the SchemaPath in SchemaNode if property "schemanode.getpath.forbid" is set to "enabled"
        // schemanode.getpath.forbid=enabled
        if (System.getProperty(ENABLE_PROPERTY, "enabled").equals("disabled")) {
            LOG.info("SchemaNode.getPath() support disabled");
            DEFAULT = new Disabled();
        } else {
            DEFAULT = new Enabled();
        }
    }

    private SchemaPathSupport() {
        // Hidden on purpose
    }

    public static @NonNull Object toEffectivePath(final @NonNull SchemaPath path) {
        return DEFAULT.effectivePath(path);
    }

    public static @Nullable SchemaPath toOptionalPath(final @Nullable SchemaPath path) {
        return DEFAULT.optionalPath(path);
    }

    public static boolean effectivelyEqual(@Nullable final SchemaPath first, @Nullable final SchemaPath second) {
        return DEFAULT.equalPaths(first, second);
    }

    public static @NonNull QName extractQName(final @NonNull Object path) {
        return path instanceof QName ? (QName) path : verifyNotNull(((SchemaPath) path).getLastComponent());
    }

    public static @NonNull SchemaPath extractPath(final @NonNull Object impl, final @NonNull Object path) {
        return path instanceof SchemaPath ? (SchemaPath) path : SchemaNodeDefaults.throwUnsupported(impl);
    }

    abstract boolean equalPaths(@Nullable SchemaPath first, @Nullable SchemaPath second);

    abstract @NonNull Object effectivePath(@NonNull SchemaPath path);

    abstract @Nullable SchemaPath optionalPath(@Nullable SchemaPath path);
}
