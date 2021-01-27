/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
public abstract class WrappedSchemaPath implements Immutable {
    private static final class Absent extends WrappedSchemaPath {
        @Override
        public WrappedSchemaPath createChild(final QName qname) {
            return this;
        }

        @Override
        public SchemaPath unwrap() {
            return null;
        }
    }

    private static final class Present extends WrappedSchemaPath {
        private final SchemaPath path;

        Present(final SchemaPath path) {
            this.path = requireNonNull(path);
        }

        @Override
        public WrappedSchemaPath createChild(final QName qname) {
            return new Present(path.createChild(qname));
        }

        @Override
        public SchemaPath unwrap() {
            return path;
        }
    }

    public abstract @NonNull WrappedSchemaPath createChild(QName qname);

    public abstract @Nullable SchemaPath unwrap();

    @Override
    public final int hashCode() {
        return Objects.hashCode(unwrap());
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj
            || obj instanceof WrappedSchemaPath && Objects.equals(unwrap(), ((WrappedSchemaPath) obj).unwrap());
    }
}
