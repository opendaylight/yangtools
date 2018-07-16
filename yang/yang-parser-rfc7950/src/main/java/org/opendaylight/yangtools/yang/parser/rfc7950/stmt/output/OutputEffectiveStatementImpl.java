/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveOperationContainerSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class OutputEffectiveStatementImpl extends AbstractEffectiveOperationContainerSchemaNode<OutputStatement>
        implements OutputEffectiveStatement {
    OutputEffectiveStatementImpl(
            final StmtContext<QName, OutputStatement, EffectiveStatement<QName, OutputStatement>> ctx) {
        super(ctx);
    }
}
