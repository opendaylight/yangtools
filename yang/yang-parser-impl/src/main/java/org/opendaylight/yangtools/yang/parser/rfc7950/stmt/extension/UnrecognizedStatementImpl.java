/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class UnrecognizedStatementImpl extends AbstractDeclaredStatement<String> implements UnrecognizedStatement {
    UnrecognizedStatementImpl(final StmtContext<String, ?, ?> context) {
        super(context);
    }

    @Nullable
    @Override
    public String getArgument() {
        return argument();
    }
}
