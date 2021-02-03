/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

@Beta
public interface AnyxmlSchemaLocationEffectiveStatement
        extends UnknownEffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return OpenDaylightExtensionsStatements.ANYXML_SCHEMA_LOCATION;
    }
}
