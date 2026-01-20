/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

public interface OidStatement extends UnknownStatement<ObjectIdentifier> {
    /**
     * The definition of {@code smiv2:oid} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(OidStatement.class, OidEffectiveStatement.class,
        IetfYangSmiv2Constants.RFC6643_MODULE, "oid", "value");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }

    default ObjectIdentifier getOid() {
        return argument();
    }
}

