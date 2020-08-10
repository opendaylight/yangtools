/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Source-specific mapping of prefixes to namespaces.
 */
public interface AnyxmlSchemaLocationNamespace extends IdentifierNamespace<StatementDefinition,
        Mutable<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement, AnyxmlSchemaLocationEffectiveStatement>> {
    NamespaceBehaviour<StatementDefinition, Mutable<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
        AnyxmlSchemaLocationEffectiveStatement>,
        @NonNull AnyxmlSchemaLocationNamespace> BEHAVIOUR =
            NamespaceBehaviour.treeScoped(AnyxmlSchemaLocationNamespace.class);
}
