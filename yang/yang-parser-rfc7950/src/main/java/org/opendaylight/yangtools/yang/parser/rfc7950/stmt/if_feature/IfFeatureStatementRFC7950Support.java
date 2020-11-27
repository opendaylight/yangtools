/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class IfFeatureStatementRFC7950Support extends AbstractIfFeatureStatementSupport {
    private static final IfFeatureStatementRFC7950Support INSTANCE = new IfFeatureStatementRFC7950Support();

    private IfFeatureStatementRFC7950Support() {
        // Hidden on purpose
    }

    public static IfFeatureStatementRFC7950Support getInstance() {
        return INSTANCE;
    }

    @Override
    public IfFeatureExpr parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return IfFeaturePredicateVisitor.parseIfFeatureExpression(ctx, value);
    }
}
