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
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Support work with namespace content
 */
@Beta
public interface NamespaceStmtCtx {

    /**
     * Return a value associated with specified key within a namespace.
     *
     * @param type Namespace type
     * @param key Key
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param <N> namespace type
     * @param <T> key type
     * @return Value, or null if there is no element
     * @throws NamespaceNotAvailableException when the namespace is not available.
     */
    <K, V, T extends K, N extends IdentifierNamespace<K, V>> @Nullable V getFromNamespace(Class<@NonNull N> type,
                                                                                          T key);

    <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(Class<N> type);
}
