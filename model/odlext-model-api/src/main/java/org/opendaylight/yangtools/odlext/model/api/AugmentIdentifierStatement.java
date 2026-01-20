/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

@NonNullByDefault
public interface AugmentIdentifierStatement extends UnknownStatement<Unqualified> {
    /**
     * The definition of {@code yang-ext:augment-identifier} statement.
     */
    StatementDefinition DEFINITION = StatementDefinition.of(
        AugmentIdentifierStatement.class, AugmentIdentifierEffectiveStatement.class,
        CodegenExtensionsConstants.ORIGINAL_MODULE, "augment-identifier", "identifier");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
