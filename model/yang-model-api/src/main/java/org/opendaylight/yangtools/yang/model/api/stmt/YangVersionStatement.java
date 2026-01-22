/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code yang-version} statement.
 */
public interface YangVersionStatement extends DeclaredStatement<YangVersion> {
    /**
     * The definition of {@code yang-version} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(
        YangVersionStatement.class, YangVersionEffectiveStatement.class,
        YangConstants.RFC6020_YIN_MODULE, "yang-version", "value");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}

