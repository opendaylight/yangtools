/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;

sealed interface CaseEffectiveStatementMixin
        extends CaseEffectiveStatement, CaseSchemaNode, DataSchemaNodeMixin<@NonNull CaseStatement>
        permits DeclaredCaseEffectiveStatement, UndeclaredCaseEffectiveStatement {
    @Override
    default QName getQName() {
        return argument();
    }

    @Override
    default CaseEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    default CaseSchemaNode toDataNodeContainer() {
        return this;
    }

    @Override
    default CaseSchemaNode toDataSchemaNode() {
        return this;
    }
}
