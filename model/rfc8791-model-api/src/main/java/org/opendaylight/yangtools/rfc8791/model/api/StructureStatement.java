/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DefaultStatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared representation of a {@code sx:structure} statement.
 *
 * @since 14.0.21
 */
@NonNullByDefault
public interface StructureStatement extends UnknownStatement<QName> {
    /**
     * The definition of {@code sx:structure} statement.
     */
    StatementDefinition DEFINITION = DefaultStatementDefinition.of(
        QName.create(YangDataStructureConstants.RFC8791_MODULE, "structure").intern(),
        StructureStatement.class,
        StructureEffectiveStatement.class,
        QName.create(YangDataStructureConstants.RFC8791_MODULE, "name").intern(),
        true);

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
