/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import java.net.URI;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 *
 * Map of fully qualified statement name to statement definition.
 *
 */
public interface QNameToStatementDefinition extends IdentifierNamespace<QName, StatementDefinition> {

    /**
     * Returns StatementDefinition with specified QName.
     *
     * @param identifier
     *            QName of requested statement
     * @return StatementDefinition
     */
    @Nullable
    StatementDefinition get(@Nonnull QName identifier);

    /**
     * Returns StatementDefinition with specified namespace and localName.
     *
     * @param namespace
     *            namespace of requested statement
     * @param localName
     *            localName of requested statement
     * @return StatementDefinition
     */
    @Nullable
    StatementDefinition getByNamespaceAndLocalName(@Nonnull final URI namespace, @Nonnull final String localName);
}
