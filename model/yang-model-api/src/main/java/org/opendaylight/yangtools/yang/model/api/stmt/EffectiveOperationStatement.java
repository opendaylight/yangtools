/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Common capture of effective traits shared by {@code action} and {@code rpc} statements. The effective view always
 * defines an {@code input} and an {@code output} substatement, both of which are available through {@link #input()}
 * and {@link #output()} methods respectively.
 */
public sealed interface EffectiveOperationStatement<D extends DeclaredOperationStatement>
    extends SchemaTreeEffectiveStatement<D>, DataTreeAwareEffectiveStatement<QName, D>,
            TypedefAwareEffectiveStatement<QName, D>
    permits ActionEffectiveStatement, RpcEffectiveStatement {
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
