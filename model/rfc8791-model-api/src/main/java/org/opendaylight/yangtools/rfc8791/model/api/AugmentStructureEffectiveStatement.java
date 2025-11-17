/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

/**
 * Effective representation of a {@code sx:augment-structure} statement.
 *
 * @since 14.0.21
 */
@NonNullByDefault
public interface AugmentStructureEffectiveStatement
        extends DataTreeAwareEffectiveStatement<AugmentStructureArgument, AugmentStructureStatement>,
                UnknownEffectiveStatement<AugmentStructureArgument, AugmentStructureStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return AugmentStructureStatement.DEFINITION;
    }
}
