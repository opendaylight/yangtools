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
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code belongs-to} statement.
 */
public interface BelongsToEffectiveStatement extends EffectiveStatement<Unqualified, BelongsToStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.BELONGS_TO;
    }

    /**
     * Return this statement's {@code prefix} substatement.
     *
     * @implSpec
     *      Default implementation uses {@link #findFirstEffectiveSubstatement(Class)} and throws a
     *      {@link VerifyException} if a matching substatement is not found.
     * @return A {@link PrefixEffectiveStatement}
     */
    default @NonNull PrefixEffectiveStatement prefix() {
        return DefaultMethodHelpers.verifyPrefixSubstatement(this);
    }
}
