/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Map of fully qualified statement name to statement definition.
 */
public interface QNameToStatementDefinition {
    /**
     * Returns StatementDefinition with specified QName.
     *
     * @param identifier QName of requested statement
     * @return StatementDefinition
     */
    @Nullable StatementDefinition get(QName identifier);

    /**
     * Returns StatementDefinition with specified namespace and localName.
     *
     * @param namespace namespace of requested statement
     * @param localName localName of requested statement
     * @return StatementDefinition
     */
    @Nullable StatementDefinition getByNamespaceAndLocalName(@NonNull XMLNamespace namespace,
        @NonNull String localName);
}
