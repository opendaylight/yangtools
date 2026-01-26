/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code uses} statement.
 */
public non-sealed interface UsesStatement extends DataDefinitionStatement, AugmentStatement.MultipleIn<QName> {
    /**
     * The definition of {@code uses} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull UsesStatement, @NonNull UsesEffectiveStatement> DEF =
        StatementDefinition.of(UsesStatement.class, UsesEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "uses", YangArgumentDefinitions.NAME_AS_QNAME);

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }

    // FIXME: document/rename
    default @NonNull Collection<? extends @NonNull RefineStatement> getRefines() {
        return declaredSubstatements(RefineStatement.class);
    }
}
