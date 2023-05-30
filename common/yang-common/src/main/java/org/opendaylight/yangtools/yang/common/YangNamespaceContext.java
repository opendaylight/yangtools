/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.Serializable;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Interface for mapping between {@link String} prefixes and {@link QNameModule} namespaces. The conceptual model
 * matches prefix mapping inside a YANG {@code module} as defined through the use of {@code prefix} and {@code import}
 * statements and detailed in <a href="https://www.rfc-editor.org/rfc/rfc7950#section-7.1.4">RFC7950 Section 7.1.4</a>.
 *
 * <p>
 * Each namespace context has a set of prefix/namespace mappings. A namespace can be bound to multiple prefixes at the
 * same time.
 */
public interface YangNamespaceContext extends Immutable, Serializable {
    /**
     * Return QNameModule to which a particular prefix is bound.
     *
     * @param prefix Prefix to look up
     * @return QNameModule bound to specified prefix, or {@code null}
     * @throws NullPointerException if {@code prefix} is {@code null}
     */
    @Nullable QNameModule namespaceForPrefix(String prefix);

    /**
     * Return QNameModule to which a particular prefix is bound.
     *
     * @implSpec Default implementation defers to {@link #namespaceForPrefix(String)}
     * @param prefix Prefix to look up
     * @return QNameModule bound to specified prefix
     * @throws NullPointerException if {@code prefix} is {@code null}
     */
    default @NonNull Optional<QNameModule> findNamespaceForPrefix(final String prefix) {
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
    @Nullable String prefixForNamespace(QNameModule namespace);

    /**
     * Return a prefix to which a particular QNameModule is bound. If a namespace is bound to multiple prefixes, it is
     * left unspecified which of those prefixes is returned.
     *
     * @implSpec Default implementation defers to {@link #prefixForNamespace(QNameModule)}
     * @param namespace QNameModule to look up
     * @return Prefix to which the QNameModule is bound
     * @throws NullPointerException if {@code module} is {@code null}
     */
    default @NonNull Optional<String> findPrefixForNamespace(final QNameModule namespace) {
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
    default @NonNull QName createQName(final String prefix, final String localName) {
        final var namespace = namespaceForPrefix(prefix);
        if (namespace == null) {
            throw new IllegalArgumentException("Prefix " + prefix + " is not bound");
        }
        return QName.create(namespace, localName);
    }
}
