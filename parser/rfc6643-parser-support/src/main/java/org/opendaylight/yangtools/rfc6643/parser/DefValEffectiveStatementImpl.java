/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.collect.ImmutableList;
import java.util.Objects;
import org.opendaylight.yangtools.rfc6643.model.api.DefValEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.DefValSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.DefValStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class DefValEffectiveStatementImpl extends UnknownEffectiveStatementBase<String, DefValStatement>
        implements DefValEffectiveStatement, DefValSchemaNode {
    DefValEffectiveStatementImpl(final Current<String, DefValStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements);
    }

    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Override
    public DefValEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNodeType(), getNodeParameter());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DefValEffectiveStatementImpl)) {
            return false;
        }
        final DefValEffectiveStatementImpl other = (DefValEffectiveStatementImpl) obj;
        return Objects.equals(getNodeType(), other.getNodeType())
            && Objects.equals(getNodeParameter(), other.getNodeParameter());
    }
}
