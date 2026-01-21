/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code input} statement.
 */
public interface InputEffectiveStatement extends DataTreeEffectiveStatement<@NonNull InputStatement>,
        DataTreeAwareEffectiveStatement<QName, @NonNull InputStatement>,
        TypedefAwareEffectiveStatement<QName, @NonNull InputStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return InputStatement.DEFINITION;
    }
}
