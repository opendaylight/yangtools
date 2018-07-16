/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.rpc;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveOperationDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class RpcEffectiveStatementImpl extends AbstractEffectiveOperationDefinition<RpcStatement>
        implements RpcDefinition, RpcEffectiveStatement {
    RpcEffectiveStatementImpl(final StmtContext<QName, RpcStatement,
            EffectiveStatement<QName, RpcStatement>> ctx) {
        super(ctx);
    }
}
