/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * {@link ParserNamespace} serves as common superclass for namespaces used during parser operation. Each such namespace,
 * is a type-captured subclass. This type capture of namespace allows for handy type-safe reading methods such as
 * {@link NamespaceStmtCtx#getFromNamespace(Class, Object)} and still allows introduction of new namespaces without need
 * to change APIs.
 *
 * @param <K> Identifier type
 * @param <V> Value type
 */
@Beta
public interface ParserNamespace<K, V> {
    /**
     * Return the {@link NamespaceBehaviour} governing this namespace. This method is typically not used by users, but
     * rather serves the implementation to maintain the namespace's organization.
     *
     * @return A {@link NamespaceBehaviour}.
     */
    @NonNull NamespaceBehaviour<K, V, ?> behaviour();

    /**
     * Return the {@link ModelProcessingPhase} in which this namespace becomes available. Attempts to access this
     * namespace in previous phases should be rejected.
     *
     * @return A {@link ModelProcessingPhase}.
     */
    @NonNull ModelProcessingPhase phase();
}
