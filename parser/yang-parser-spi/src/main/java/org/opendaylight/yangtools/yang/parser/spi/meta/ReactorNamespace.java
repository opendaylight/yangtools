/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A read-only {@link ParserNamespace}, which has fixed contents over the course of an execution.
 *
 * @param <K> key type
 * @param <V> value type
 */
@NonNullByDefault
public sealed interface ReactorNamespace<K, V> extends ParserNamespace<K, V> permits ReactorNamespaceImpl {
    /**
     * {@return a new {@link ReactorNamespace} namespace with specified name}
     *
     * @param <K> key type
     * @param <V> value type
     * @param name the name
     */
    static <K, V> ReactorNamespace<K, V> of(final String name) {
        return new ReactorNamespaceImpl<>(name);
    }
}
