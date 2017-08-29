/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class PatternEffectiveStatementImpl extends
        AbstractConstraintEffectiveStatement<PatternConstraint, PatternStatement> {
    public PatternEffectiveStatementImpl(final StmtContext<PatternConstraint, PatternStatement, ?> ctx) {
        super(ctx);
    }

    @Override
    protected PatternConstraint createConstraints(final PatternConstraint argument) {
        if (!isCustomizedStatement()) {
            return argument;
        }

        return new PatternConstraintEffectiveImpl(argument.getRegularExpression(), argument.getRawRegularExpression(),
            getDescription(), getReference(), getErrorAppTag(), getErrorMessage(), getModifier());
    }
}
