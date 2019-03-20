/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Interface for mapping between {@link String} prefixes and {@link QNameModule} namespaces. The conceptual model
 * matches prefix mapping inside a YANG {@code module} as defined through the use of {@code prefix} and {@code import}
 * statements and detailed in <a href="https://tools.ietf.org/html/rfc7950#section-7.1.4">RFC7950 Section 7.1.4</a>.
 *
 * <p>
 * Each namespace context can have a default namespace and a set of prefix/namespace mappings. A namespace can be bound
 * to multiple prefixes at the same time. The default namespace must also have a prefix assigned.
 *
 * @author Robert Varga
 */
@Beta
public interface YangNamespaceContext extends Immutable, Serializable {



    /**
     * Return the default namespace in this context.
     *
     * @return Default namespace, if supported.
     */
    @NonNull Optional<QNameModule> getDefaultNamespace();

    /**
     * Return QNameModule to which a particular prefix is bound.
     *
     * @param prefix Prefix to look up
     * @return QNameModule bound to specified prefix
     * @throws NullPointerException if {@code prefix} is null
     */
    @NonNull Optional<QNameModule> findNamespaceForPrefix(String prefix);

    /**
     * Return a prefix to which a particular QNameModule is bound. If a namespace is bound to multiple prefixes, it is
     * left unspecified which of those prefixes is returned.
     *
     * @param namespace QNameModule to look up
     * @return Prefix to which the QNameModule is bound
     * @throws NullPointerException if {@code module} is null
     */
    @NonNull Optional<String> findPrefixForNamespace(QNameModule namespace);

    /**
     * Create a {@link QName} in the default namespace.
     *
     * @param localName QName local name
     * @return A QName.
     * @throws NullPointerException if {@code localName} is null
     * @throws IllegalArgumentException if {@code localName} does not conform to local name requirements
     * @throws IllegalStateException if this context does not have default namespace
     */
    default @NonNull QName createQName(final String localName) {
        final Optional<QNameModule> namespace = getDefaultNamespace();
        checkState(namespace.isPresent(), "%s does not have a default namespace", this);
        return QName.create(namespace.get(), requireNonNull(localName));
    }

    /**
     * Create a {@link QName} by resolving a prefix against currently-bound prefixes and combining it with specified
     * local name.
     *
     * @param prefix Namespace prefix
     * @param localName QName local name
     * @return A QName.
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if {@code localName} does not conform to local name requirements or if the
     *                                  prefix is not bound in this context.
     */
    default @NonNull QName createQName(final String prefix, final String localName) {
        final Optional<QNameModule> namespace = findNamespaceForPrefix(prefix);
        checkArgument(namespace.isPresent(), "Prefix %s is not bound", prefix);
        return QName.create(namespace.get(), localName);
    }
}
