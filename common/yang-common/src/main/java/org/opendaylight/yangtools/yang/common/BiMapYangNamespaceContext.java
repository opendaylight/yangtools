/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serial;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * A BiMap-based implementation of {@link YangNamespaceContext}.
 */
public final class BiMapYangNamespaceContext implements YangNamespaceContext, WritableObject {
    @Serial
    private static final long serialVersionUID = 1L;

    private final ImmutableBiMap<String, QNameModule> mapping;

    public BiMapYangNamespaceContext(final BiMap<String, QNameModule> mapping) {
        this.mapping = ImmutableBiMap.copyOf(mapping);
    }

    @Override
    public QNameModule namespaceForPrefix(final String prefix) {
        return mapping.get(requireNonNull(prefix));
    }

    @Override
    public String prefixForNamespace(final QNameModule namespace) {
        return mapping.inverse().get(requireNonNull(namespace));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeInt(mapping.size());
        for (var entry : mapping.entrySet()) {
            out.writeUTF(entry.getKey());
            entry.getValue().writeTo(out);
        }
    }

    public static BiMapYangNamespaceContext readFrom(final DataInput in) throws IOException {
        final int size = in.readInt();
        final var builder = ImmutableBiMap.<String, QNameModule>builder();
        for (int i = 0; i < size; ++i) {
            final var prefix = in.readUTF();
            final var namespace = QNameModule.readFrom(in);
            builder.put(prefix, namespace);
        }

        return new BiMapYangNamespaceContext(builder.build());
    }

    @Override
    public int hashCode() {
        return mapping.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof BiMapYangNamespaceContext other && mapping.equals(other.mapping);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("mapping", mapping).toString();
    }
}
