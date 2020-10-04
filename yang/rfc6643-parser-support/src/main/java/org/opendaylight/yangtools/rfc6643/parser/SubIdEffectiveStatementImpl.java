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
import org.opendaylight.yangtools.rfc6643.model.api.SubIdEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdSchemaNode;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class SubIdEffectiveStatementImpl extends UnknownEffectiveStatementBase<Uint32, SubIdStatement>
        implements SubIdEffectiveStatement, SubIdSchemaNode {

    private final SchemaPath path;

    SubIdEffectiveStatementImpl(final StmtContext<Uint32, SubIdStatement, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(ctx, substatements);
        path = ctx.getParentContext().getSchemaPath().get().createChild(getNodeType());
    }

    @Override
    public Uint32 getArgument() {
        return argument();
    }

    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public SubIdEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, getNodeType(), getNodeParameter());
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
        final SubIdEffectiveStatementImpl other = (SubIdEffectiveStatementImpl) obj;
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        if (!Objects.equals(getNodeType(), other.getNodeType())) {
            return false;
        }
        if (!Objects.equals(getNodeParameter(), other.getNodeParameter())) {
            return false;
        }
        return true;
    }
}
