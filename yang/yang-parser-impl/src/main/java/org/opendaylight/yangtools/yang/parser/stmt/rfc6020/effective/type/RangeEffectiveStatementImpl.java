/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class RangeEffectiveStatementImpl extends
        AbstractListConstraintEffectiveStatement<RangeConstraint, RangeStatement> {
    public RangeEffectiveStatementImpl(final StmtContext<List<RangeConstraint>, RangeStatement, ?> ctx) {
        super(ctx);
    }

    @Override
    final RangeConstraint createCustomizedConstraint(final RangeConstraint rangeConstraint) {
        return new RangeConstraintEffectiveImpl(rangeConstraint.getMin(), rangeConstraint.getMax(),
                getDescription(), getReference(), getErrorAppTag(), getErrorMessage());
    }
}
