/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

/**
 * An entity able to look up a {@link NamespaceBehaviour} corresponding to a {@link ParserNamespace}.
 */
sealed interface NamespaceBehaviourRegistry permits BuildGlobalContext, SourceSpecificContext {
    /**
     * Get a namespace behavior.
     *
     * @param <K> key type
     * @param <V> value type
     * @param namespace Namespace type
     * @return Namespace behaviour
     * @throws NamespaceNotAvailableException when the namespace is not available
     * @throws NullPointerException if {@code namespace} is {@code null}
     */
    <K, V> @NonNull NamespaceBehaviour<K, V> getNamespaceBehaviour(ParserNamespace<K, V> namespace);
}
