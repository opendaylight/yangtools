/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Support work with namespace content.
 */
@Beta
public interface NamespaceStmtCtx extends CommonStmtCtx {
    /**
     * Return the selected namespace.
     *
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param namespace namespace
     * @return Namespace contents, if available
     */
    <K, V> @Nullable Map<K, V> namespace(@NonNull ParserNamespace<K, V> namespace);

    /**
     * Return a value associated with specified key within a namespace.
     *
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param namespace Namespace
     * @param key Key
     * @return Value, or {@code null} if there is no element
     * @throws NamespaceNotAvailableException when the namespace is not available.
     */
    <K, V> @Nullable V namespaceItem(@NonNull ParserNamespace<K, V> namespace, K key);

    /**
     * Return the portion of specified namespace stored in this node. Depending on namespace behaviour this may or may
     * not represent the complete contents of the namespace as available via {@link #namespace(ParserNamespace)}.
     *
     * <p>This partial view is useful when the need is not to perform a proper namespace lookup, but rather act on
     * current statement's contribution to the namespace.
     *
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param namespace namespace type class
     * @return Namespace portion stored in this node, if available
     */
    <K, V> @Nullable Map<K, V> localNamespacePortion(@NonNull ParserNamespace<K, V> namespace);
}
