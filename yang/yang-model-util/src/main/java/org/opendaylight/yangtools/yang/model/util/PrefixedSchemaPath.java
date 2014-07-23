/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class PrefixedSchemaPath implements Immutable {
    private SchemaPath schemaPath;

    private final List<PrefixedQName> path;
    private final boolean isAbsolute;

    private PrefixedSchemaPath(PrefixedSchemaPathBuilder builder) {
        if (builder.pathBuilder.isEmpty()) {
            this.path = Collections.emptyList();
        } else {
            this.path = ImmutableList.copyOf(builder.pathBuilder);
        }
        this.isAbsolute = builder.absolute;
    }

    /**
     * Returns the complete path to schema node.
     *
     * @return list of <code>PrefixedQName</code> instances which represents
     *         complete path to schema node
     *
     */
    public List<PrefixedQName> getPathFromRoot() {
        return path;
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public SchemaPath toSchemaPath() {
        if (schemaPath != null) {
            return schemaPath;
        }
        List<QName> iterable = new ArrayList<>();
        for (PrefixedQName name : path) {
            iterable.add(name.createQName());
        }
        schemaPath = SchemaPath.create(iterable, isAbsolute);
        return schemaPath;
    }

    public static PrefixedSchemaPathBuilder builder() {
        return new PrefixedSchemaPathBuilder();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(PrefixedSchemaPath.class.getSimpleName());
        sb.append('(');
        sb.append(path);
        sb.append(',');
        sb.append("absolute = ");
        sb.append(isAbsolute);
        sb.append(')');
        return sb.toString();
    }


    public static final class PrefixedSchemaPathBuilder {
        private final List<PrefixedQName> pathBuilder = new ArrayList<>();
        private boolean absolute;

        private PrefixedSchemaPathBuilder() {
        }

        public PrefixedSchemaPathBuilder add(PrefixedQName name) {
            pathBuilder.add(name);
            return this;
        }

        public PrefixedSchemaPathBuilder setAbsolute(final boolean absolute) {
            this.absolute = absolute;
            return this;
        }

        public PrefixedSchemaPathBuilder wrapPath(final SchemaPath path) {
            this.absolute = path.isAbsolute();
            for (QName qname : path.getPathFromRoot()) {
                add(new PrefixedQName(qname));
            }
            return this;
        }

        public PrefixedSchemaPath build() {
            return new PrefixedSchemaPath(this);
        }

    }

}
