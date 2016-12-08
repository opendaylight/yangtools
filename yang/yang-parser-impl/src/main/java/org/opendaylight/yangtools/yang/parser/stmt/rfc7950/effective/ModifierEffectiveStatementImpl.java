/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective;

import com.google.common.base.MoreObjects;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;

public final class ModifierEffectiveStatementImpl extends
        DeclaredEffectiveStatementBase<ModifierKind, ModifierStatement> {
    public ModifierEffectiveStatementImpl(final StmtContext<ModifierKind, ModifierStatement, ?> ctx) {
        super(ctx);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("argument", argument()).toString();
    }
}