/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yin_element;

import org.opendaylight.yangtools.yang.model.api.stmt.YinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class YinElementEffectiveStatementImpl extends DeclaredEffectiveStatementBase<Boolean, YinElementStatement>
        implements YinElementEffectiveStatement {
    YinElementEffectiveStatementImpl(final StmtContext<Boolean, YinElementStatement, ?> ctx) {
        super(ctx);
    }
}
