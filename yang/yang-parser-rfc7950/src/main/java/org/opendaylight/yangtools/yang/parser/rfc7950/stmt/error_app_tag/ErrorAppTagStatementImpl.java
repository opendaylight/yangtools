/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_app_tag;

import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement.WithArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class ErrorAppTagStatementImpl extends WithArgument<String> implements ErrorAppTagStatement {
    ErrorAppTagStatementImpl(final StmtContext<String, ErrorAppTagStatement, ?> context) {
        super(context);
    }

    @Override
    public String getValue() {
        return argument();
    }
}
