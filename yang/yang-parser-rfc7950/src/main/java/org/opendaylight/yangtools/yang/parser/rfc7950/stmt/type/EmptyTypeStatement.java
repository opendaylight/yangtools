/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithRawStringArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class EmptyTypeStatement extends WithRawStringArgument implements TypeStatement {
    EmptyTypeStatement(final StmtContext<String, ?, ?> context) {
        super(context);
    }
}
