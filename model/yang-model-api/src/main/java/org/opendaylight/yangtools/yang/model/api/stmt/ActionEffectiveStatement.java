/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code action} statement. The effective view always defines an {@code input} and an
 * {@code output} substatement, both of which are available through {@link #input()} and {@link #output()} methods.
 */
public non-sealed interface ActionEffectiveStatement extends EffectiveOperationStatement<@NonNull ActionStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return ActionStatement.DEFINITION;
    }
}
