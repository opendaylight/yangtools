/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc8040;

import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Declared statement representation of 'yang-data' extension defined in
 * <a href="https://tools.ietf.org/html/rfc8040#section-8">RFC 8040</a>.
 */
final class YangDataStatementImpl extends AbstractDeclaredStatement<String> implements YangDataStatement {
    YangDataStatementImpl(final StmtContext<String, YangDataStatement, ?> ctx) {
        super(ctx);
    }

    @Override
    public String getArgument() {
        return argument();
    }
}
