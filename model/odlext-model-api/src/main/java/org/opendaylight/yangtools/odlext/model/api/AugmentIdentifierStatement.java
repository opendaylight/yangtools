/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

public interface AugmentIdentifierStatement extends UnknownStatement<Unqualified> {
    /**
     * The definition of {@code yang-ext:augment-identifier} statement.
     */
    @NonNull StatementDefinition<Unqualified, @NonNull AugmentIdentifierStatement,
        @NonNull AugmentIdentifierEffectiveStatement> DEF = StatementDefinition.of(
            AugmentIdentifierStatement.class, AugmentIdentifierEffectiveStatement.class,
            CodegenExtensionsConstants.ORIGINAL_MODULE, "augment-identifier",
            ArgumentDefinition.of(Unqualified.class, CodegenExtensionsConstants.ORIGINAL_MODULE, "identifier"));

    @Override
    default StatementDefinition<Unqualified, ?, ?> statementDefinition() {
        return DEF;
    }
}
