/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement.WithArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class LeafListStatementImpl extends WithArgument<QName> implements LeafListStatement {
    LeafListStatementImpl(final StmtContext<QName, LeafListStatement, ?> context) {
        super(context);
    }
}
