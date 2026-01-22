/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code bit} statement.
 */
public interface BitStatement extends DocumentedDeclaredStatement.WithStatus<String>,
        IfFeatureAwareDeclaredStatement<String> {
    /**
     * The definition of {@code bin} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(BitStatement.class, BitEffectiveStatement.class,
        YangConstants.RFC6020_YIN_MODULE, "bit", "name");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }

    default @Nullable PositionStatement position() {
        final var opt = findFirstDeclaredSubstatement(PositionStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }
}
