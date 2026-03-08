/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * {@link ParserNamespace} serves as common superclass for namespaces used during parser operation. Each such namespace,
 * is a type-captured subclass. This type capture of namespace allows for handy type-safe reading methods such as
 * {@link NamespaceStmtCtx#namespaceItem(ParserNamespace, Object)} and still allows introduction of new namespaces
 * without need to change APIs.
 *
 * <p>Each namespace is either {@link ReadOnly} or {@link Writable}.
 *
 * @param <K> key type
 * @param <V> value type
 */
@NonNullByDefault
public sealed interface ParserNamespace<K, V> extends Immutable {
    /**
     * A read-only {@link ParserNamespace}, which has fixed contents over the course of an execution.
     *
     * @param <K> key type
     * @param <V> value type
     */
    sealed interface ReadOnly<K, V> extends ParserNamespace<K, V> permits ReadOnlyParserNamespace {
        // Nothing else
    }

    /**
     * A writable {@link ParserNamespace}, which can be modified over the course of an execution.
     *
     * @param <K> key type
     * @param <V> value type
     */
    sealed interface Writable<K, V> extends ParserNamespace<K, V> permits WritableParserNamespace {
        // Nothing else
    }

    /**
     * {@return a new {@link ReadOnly} namespace with specified name}
     * @param <K> key type
     * @param <V> value type
     * @param name the name
     */
    static <K, V> ReadOnly<K, V> readOnly(final String name) {
        return new ReadOnlyParserNamespace<>(name);
    }

    /**
     * {@return a new {@link Writable} namespace with specified name}
     * @param <K> key type
     * @param <V> value type
     * @param name the name
     */
    static <K, V> Writable<K, V> writable(final String name) {
        return new WritableParserNamespace<>(name);
    }
}
