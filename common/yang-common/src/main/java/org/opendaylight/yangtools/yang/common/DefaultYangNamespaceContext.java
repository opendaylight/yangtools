/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
record DefaultYangNamespaceContext(
        Map<QNameModule, String> moduleToPrefix,
        Map<String, QNameModule> prefixToModule) implements YangNamespaceContext {
    DefaultYangNamespaceContext {
        moduleToPrefix = Map.copyOf(moduleToPrefix);
        prefixToModule = Map.copyOf(prefixToModule);
    }

    @Override
    public @Nullable String prefixForNamespace(final QNameModule namespace) {
        return moduleToPrefix.get(requireNonNull(namespace));
    }

    @Override
    public @Nullable QNameModule namespaceForPrefix(final String prefix) {
        return prefixToModule.get(requireNonNull(prefix));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeInt(prefixToModule.size());
        for (var entry : prefixToModule.entrySet()) {
            out.writeUTF(entry.getKey());
            entry.getValue().writeTo(out);
        }
    }

    @Override
    public int hashCode() {
        return prefixToModule.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof DefaultYangNamespaceContext other
            && prefixToModule.equals(other.prefixToModule);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(YangNamespaceContext.class).add("mapping", prefixToModule).toString();
    }

    @java.io.Serial
    private Object writeReplace() {
        return new YNSv1(prefixToModule);
    }
}
