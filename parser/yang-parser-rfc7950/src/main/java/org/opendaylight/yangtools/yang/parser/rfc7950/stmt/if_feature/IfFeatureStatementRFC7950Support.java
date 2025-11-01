/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.parser.antlr.IfFeatureArgumentParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class IfFeatureStatementRFC7950Support extends AbstractIfFeatureStatementSupport {
    public IfFeatureStatementRFC7950Support(final YangParserConfiguration config) {
        super(config);
    }

    @Override
    public IfFeatureExpr parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return IfFeatureArgumentParser.RFC7950.parseArgument(ctx, value);
    }
}
