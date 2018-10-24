/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.argument;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementWithArgumentBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class ArgumentEffectiveStatementImpl extends DeclaredEffectiveStatementWithArgumentBase<QName, ArgumentStatement>
        implements ArgumentEffectiveStatement {
    ArgumentEffectiveStatementImpl(final StmtContext<QName, ArgumentStatement, ?> ctx) {
        super(ctx);
    }
}