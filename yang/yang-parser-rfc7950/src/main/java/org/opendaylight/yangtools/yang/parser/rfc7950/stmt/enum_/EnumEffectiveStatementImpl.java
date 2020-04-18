/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

// FIXME: 6.0.0: hide this class
public final class EnumEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<String, EnumStatement>
        implements EnumEffectiveStatement {
    private final String name;
    private final Integer declaredValue;

    EnumEffectiveStatementImpl(final StmtContext<String, EnumStatement, ?> ctx) {
        super(ctx);

        name = ctx.rawStatementArgument();

        Integer declaredValueInit = null;
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof ValueEffectiveStatement) {
                declaredValueInit = ((ValueEffectiveStatement) effectiveStatement).argument();
            }
        }

        declaredValue = declaredValueInit;
    }

    public String getName() {
        return name;
    }

    public Integer getDeclaredValue() {
        return declaredValue;
    }
}