/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;

import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class BitEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<String, BitStatement>
        implements BitEffectiveStatement {
    BitEffectiveStatementImpl(final StmtContext<String, BitStatement, ?> ctx) {
        super(ctx);
    }

    @Override
    public String toString() {
        return BitEffectiveStatementImpl.class.getSimpleName() + "[name=" + argument() + ", position="
                + getDeclaredPosition().orElse(null) + "]";
    }
}
