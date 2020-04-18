/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.length;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractListConstraintEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class LengthEffectiveStatementImpl extends AbstractListConstraintEffectiveStatement<ValueRange, LengthStatement>
        implements LengthEffectiveStatement {
    LengthEffectiveStatementImpl(final StmtContext<List<ValueRange>, LengthStatement, ?> ctx) {
        super(ctx);
    }
}