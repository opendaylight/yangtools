/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.collect.BiMap;
import java.io.DataInput;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * Interface for mapping between {@link String} prefixes and {@link QNameModule} namespaces. The conceptual model
 * matches prefix mapping inside a YANG {@code module} as defined through the use of {@code prefix} and {@code import}
 * statements and detailed in <a href="https://www.rfc-editor.org/rfc/rfc7950#section-7.1.4">RFC7950 Section 7.1.4</a>.
 *
 * <p>Each namespace context has a set of prefix/namespace mappings. A namespace can be bound to multiple prefixes at
 * the same time.
 */
public sealed interface YangNamespaceContext extends Immutable, Serializable, WritableObject
        permits DefaultYangNamespaceContext {
    @NonNullByDefault
    static YangNamespaceContext of(final BiMap<String, QNameModule> mapping) {
        return new DefaultYangNamespaceContext(mapping.inverse(), mapping);
    }

    @NonNullByDefault
    static YangNamespaceContext of(final Map<String, QNameModule> prefixToModule) {
        final var toPrefix = HashMap.<QNameModule, String>newHashMap(prefixToModule.size());
        for (var entry : prefixToModule.entrySet()) {
            final var prefix = entry.getKey();
            final var module = entry.getValue();

            // slightly more complicated: use the lexicographically lowest prefix
            final var prev = toPrefix.putIfAbsent(module, prefix);
            if (prev != null && prev.compareTo(prefix) > 0) {
                toPrefix.replace(module, prev, prefix);
            }
        }

        return new DefaultYangNamespaceContext(toPrefix, prefixToModule);
    }

    @NonNullByDefault
    static YangNamespaceContext ofUnqualified(final Map<Unqualified, QNameModule> prefixToModule) {
        final var toModule = HashMap.<String, QNameModule>newHashMap(prefixToModule.size());
        final var toPrefix = HashMap.<QNameModule, String>newHashMap(prefixToModule.size());
        for (var entry : prefixToModule.entrySet()) {
            final var prefix = entry.getKey().getLocalName();
            final var module = entry.getValue();

            // simple
            toModule.put(prefix, module);

            // slightly more complicated: use the lexicographically lowest prefix
            final var prev = toPrefix.putIfAbsent(module, prefix);
            if (prev != null && prev.compareTo(prefix) > 0) {
                toPrefix.replace(module, prev, prefix);
            }
        }

        return new DefaultYangNamespaceContext(toPrefix, toModule);
    }

    @NonNullByDefault
    static YangNamespaceContext readFrom(final DataInput in) throws IOException {
        final int size = in.readInt();
        final var prefixToModule = HashMap.<String, QNameModule>newHashMap(size);
        for (int i = 0; i < size; ++i) {
            final var prefix = in.readUTF();
            final var namespace = QNameModule.readFrom(in);
            prefixToModule.put(prefix, namespace);
        }

        return of(prefixToModule);
    }

    /**
     * Return QNameModule to which a particular prefix is bound.
     *
     * @param prefix Prefix to look up
     * @return QNameModule bound to specified prefix, or {@code null}
     * @throws NullPointerException if {@code prefix} is {@code null}
     */
    @Nullable QNameModule namespaceForPrefix(@NonNull String prefix);

    /**
     * Return QNameModule to which a particular prefix is bound.
     *
     * @implSpec Default implementation defers to {@link #namespaceForPrefix(String)}
     * @param prefix Prefix to look up
     * @return QNameModule bound to specified prefix
     * @throws NullPointerException if {@code prefix} is {@code null}
     */
    default @NonNull Optional<QNameModule> findNamespaceForPrefix(final @NonNull String prefix) {
        return Optional.ofNullable(namespaceForPrefix(prefix));
    }

    /**
     * Return a prefix to which a particular QNameModule is bound. If a namespace is bound to multiple prefixes, it is
     * left unspecified which of those prefixes is returned.
     *
     * @param namespace QNameModule to look up
     * @return Prefix to which the QNameModule is bound, or {@code null}
     * @throws NullPointerException if {@code module} is {@code null}
     */
    @Nullable String prefixForNamespace(@NonNull QNameModule namespace);

    /**
     * Return a prefix to which a particular QNameModule is bound. If a namespace is bound to multiple prefixes, it is
     * left unspecified which of those prefixes is returned.
     *
     * @implSpec Default implementation defers to {@link #prefixForNamespace(QNameModule)}
     * @param namespace QNameModule to look up
     * @return Prefix to which the QNameModule is bound
     * @throws NullPointerException if {@code module} is {@code null}
     */
    default @NonNull Optional<String> findPrefixForNamespace(final @NonNull QNameModule namespace) {
        return Optional.ofNullable(prefixForNamespace(namespace));
    }

    /**
     * Create a {@link QName} by resolving a prefix against currently-bound prefixes and combining it with specified
     * local name.
     *
     * @implSpec
     *     Default implementation defers to {@link #namespaceForPrefix(String)} and constructs QName based on its
     *     return.
     * @param prefix Namespace prefix
     * @param localName QName local name
     * @return A QName.
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if {@code localName} does not conform to local name requirements or if the
     *                                  prefix is not bound in this context.
     */
    @NonNullByDefault
    default QName createQName(final String prefix, final String localName) {
        final var namespace = namespaceForPrefix(prefix);
        if (namespace == null) {
            throw new IllegalArgumentException("Prefix " + prefix + " is not bound");
        }
        return QName.create(namespace, localName);
    }
}
