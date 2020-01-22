/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

// FIXME: 5.0.0: hide this class
public final class BitEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<QName, BitStatement>
        implements BitEffectiveStatement {

    private final QName qname;
    private final SchemaPath schemaPath;
    private final Long declaredPosition;

    BitEffectiveStatementImpl(final StmtContext<QName, BitStatement, ?> ctx) {
        super(ctx);

        qname = ctx.getStatementArgument();
        schemaPath = ctx.getSchemaPath().get();

        Long declaredPositionInit = null;
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof PositionEffectiveStatement) {
                declaredPositionInit = ((PositionEffectiveStatement) effectiveStatement).argument();
            }
        }

        declaredPosition = declaredPositionInit;
    }

    public Long getDeclaredPosition() {
        return declaredPosition;
    }

    public String getName() {
        return qname.getLocalName();
    }

    public QName getQName() {
        return qname;
    }

    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public String toString() {
        return BitEffectiveStatementImpl.class.getSimpleName() + "[name=" + qname.getLocalName() + ", position="
                + declaredPosition + "]";
    }
}
