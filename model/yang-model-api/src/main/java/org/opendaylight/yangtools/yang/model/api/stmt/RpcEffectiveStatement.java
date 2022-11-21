/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code rpc} statement. The effective view always defines an {@code input} and an
 * {@code output} substatement, both of which are available through {@link #input()} and {@link #output()} methods.
 */
public interface RpcEffectiveStatement extends SchemaTreeEffectiveStatement<RpcStatement>,
        DataTreeAwareEffectiveStatement<QName, RpcStatement>, TypedefAwareEffectiveStatement<QName, RpcStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.RPC;
    }

    /**
     * Return this statement's {@code input} substatement.
     *
     * @implSpec
     *      Default implementation uses {@link #findFirstEffectiveSubstatement(Class)} and throws a
     *      {@link VerifyException} if a matching substatement is not found.
     * @return An {@link InputEffectiveStatement}
     */
    default @NonNull InputEffectiveStatement input() {
        return DefaultMethodHelpers.verifyInputSubstatement(this);
    }

    /**
     * Return this statement's {@code output} substatement.
     *
     * @implSpec
     *      Default implementation uses {@link #findFirstEffectiveSubstatement(Class)} and throws a
     *      {@link VerifyException} if a matching substatement is not found.
     * @return An {@link OutputEffectiveStatement}
     */
    default @NonNull OutputEffectiveStatement output() {
        return DefaultMethodHelpers.verifyOutputSubstatement(this);
    }
}
