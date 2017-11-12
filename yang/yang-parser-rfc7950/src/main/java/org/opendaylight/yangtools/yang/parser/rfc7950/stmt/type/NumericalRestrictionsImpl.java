/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.NumericalRestrictions;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

// FIXME: this class is not used anywhere, decide its future
final class NumericalRestrictionsImpl extends AbstractDeclaredStatement<String> implements NumericalRestrictions {
    NumericalRestrictionsImpl(final StmtContext<String, NumericalRestrictions, ?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }

    @Nonnull
    @Override
    public RangeStatement getRange() {
        return firstDeclared(RangeStatement.class);
    }
}
