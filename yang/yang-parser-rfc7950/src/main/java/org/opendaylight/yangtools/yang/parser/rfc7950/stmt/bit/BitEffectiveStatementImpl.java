/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;

import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + qname.hashCode();
        result = prime * result + schemaPath.hashCode();
        result = prime * result + Objects.hashCode(declaredPosition);
        result = prime * result + getUnknownSchemaNodes().hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BitsTypeDefinition.Bit other = (BitsTypeDefinition.Bit) obj;
        return Objects.equals(qname, other.getQName()) && Objects.equals(schemaPath, other.getPath());
    }

    @Override
    public String toString() {
        return BitEffectiveStatementImpl.class.getSimpleName() + "[name=" + qname.getLocalName() + ", position="
                + declaredPosition + "]";
    }
}
