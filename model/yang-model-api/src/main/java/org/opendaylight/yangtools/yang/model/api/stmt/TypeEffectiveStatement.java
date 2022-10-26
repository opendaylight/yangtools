/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective view of a {@code type} statement. Its {@link #argument()} points to a {@code typedef} statement in this
 * statement's ancestor hierarchy.
 *
 * @param <T> {@link TypeStatement} specialization
 */
public interface TypeEffectiveStatement<T extends TypeStatement>
        extends EffectiveStatement<QName, T>, TypeDefinitionAware {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.TYPE;
    }
}
