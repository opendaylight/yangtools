/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

public final class IfFeatureStatementRFC6020Support extends AbstractIfFeatureStatementSupport  {
    public IfFeatureStatementRFC6020Support(final YangParserConfiguration config) {
        super(config);
    }

    @Override
    public IfFeatureExpr parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return IfFeatureExpr.isPresent(StmtContextUtils.parseNodeIdentifier(ctx, value));
    }
}
