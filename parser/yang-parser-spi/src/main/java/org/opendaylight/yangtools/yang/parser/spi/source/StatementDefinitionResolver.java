/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * An entity capable of resolving the combination of a namespace and a statement to the corresponding
 * {@link StatementDefinition}.
 */
public interface StatementDefinitionResolver {
    /**
     * {@return the {@link StatementDefinition} with specified namespace and local name}
     * @param namespace namespace of requested statement
     * @param localName localName of requested statement
     */
    @Nullable StatementDefinition<?, ?, ?> lookupDef(@NonNull String namespace, @NonNull String localName);

    /**
     * {@return the {@link StatementDefinition} with specified namespace and local name}
     * @param namespace namespace of requested statement
     * @param localName localName of requested statement
     */
    @Nullable StatementDefinition<?, ?, ?> lookupDef(@NonNull QNameModule namespace, @NonNull String localName);
}
