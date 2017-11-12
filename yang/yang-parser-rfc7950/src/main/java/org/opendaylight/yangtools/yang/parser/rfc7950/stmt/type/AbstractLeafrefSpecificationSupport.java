/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractLeafrefSpecificationSupport
        extends
        AbstractStatementSupport<String, LeafrefSpecification, EffectiveStatement<String, LeafrefSpecification>> {
    AbstractLeafrefSpecificationSupport() {
        super(YangStmtMapping.TYPE);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public final LeafrefSpecification createDeclared(final StmtContext<String, LeafrefSpecification, ?> ctx) {
        return new LeafrefSpecificationImpl(ctx);
    }

    @Override
    public final EffectiveStatement<String, LeafrefSpecification> createEffective(
            final StmtContext<String, LeafrefSpecification, EffectiveStatement<String, LeafrefSpecification>> ctx) {
        return new LeafrefSpecificationEffectiveStatement(ctx);
    }
}