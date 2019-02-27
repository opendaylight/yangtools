/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * A BiMap-based implementation of {@link YangNamespaceContext}.
 *
 * @author Robert Varga
 */
@Beta
public final class BiMapYangNamespaceContext implements YangNamespaceContext, WritableObject {
    private static final long serialVersionUID = 1L;

    private final ImmutableBiMap<String, QNameModule> mapping;
    private final QNameModule defaultNamespace;

    public BiMapYangNamespaceContext(final BiMap<String, QNameModule> mapping) {
        this(mapping, null);
    }

    public BiMapYangNamespaceContext(final BiMap<String, QNameModule> mapping,
            final QNameModule defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
        this.mapping = ImmutableBiMap.copyOf(mapping);
        if (defaultNamespace != null) {
            checkArgument(this.mapping.containsValue(defaultNamespace),
                "Mapping %s does not contain default namespace %s", this.mapping, defaultNamespace);
        }
    }

    @Override
    public Optional<QNameModule> getDefaultNamespace() {
        return Optional.ofNullable(defaultNamespace);
    }

    @Override
    public Optional<QNameModule> findNamespaceForPrefix(final String prefix) {
        return Optional.ofNullable(mapping.get(requireNonNull(prefix)));
    }

    @Override
    public Optional<String> findPrefixForNamespace(final QNameModule namespace) {
        return Optional.ofNullable(mapping.inverse().get(requireNonNull(namespace)));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        if (defaultNamespace != null) {
            out.writeBoolean(true);
            defaultNamespace.writeTo(out);
        } else {
            out.writeBoolean(false);
        }

        out.writeInt(mapping.size());
        for (Entry<String, QNameModule> entry : mapping.entrySet()) {
            out.writeUTF(entry.getKey());
            entry.getValue().writeTo(out);
        }
    }

    public static BiMapYangNamespaceContext readFrom(final DataInput in) throws IOException {
        final boolean haveDefault = in.readBoolean();
        final QNameModule defaultNamespace = haveDefault ? QNameModule.readFrom(in) : null;
        final int size = in.readInt();
        final Builder<String, QNameModule> builder = ImmutableBiMap.builder();
        for (int i = 0; i < size; ++i) {
            final String prefix = in.readUTF();
            final QNameModule namespace = QNameModule.readFrom(in);
            builder.put(prefix, namespace);
        }

        return new BiMapYangNamespaceContext(builder.build(), defaultNamespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultNamespace, mapping);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BiMapYangNamespaceContext)) {
            return false;
        }
        final BiMapYangNamespaceContext other = (BiMapYangNamespaceContext) obj;
        return defaultNamespace.equals(other.defaultNamespace) && mapping.equals(other.mapping);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("defaultNamespace", defaultNamespace).add("mapping", mapping)
                .toString();
    }
}
