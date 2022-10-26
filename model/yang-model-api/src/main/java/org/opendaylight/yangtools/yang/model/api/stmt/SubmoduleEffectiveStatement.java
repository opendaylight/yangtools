/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Representation of {@code submodule} statement. Note that implementations of this interface are required to provide
 * {@link ModuleEffectiveStatement.PrefixToEffectiveModuleNamespace} and
 * {@link ModuleEffectiveStatement.QNameModuleToPrefixNamespace} namespaces.
 */
public interface SubmoduleEffectiveStatement
        extends DataTreeAwareEffectiveStatement<Unqualified, SubmoduleStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.SUBMODULE;
    }
}
