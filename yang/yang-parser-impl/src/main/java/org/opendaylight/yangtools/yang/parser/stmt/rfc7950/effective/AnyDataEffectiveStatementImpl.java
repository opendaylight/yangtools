/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AbstractEffectiveDataSchemaNode;

/**
 * YANG 1.1 AnyData effective statement implementation.
 */
@Beta
public final class AnyDataEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<AnydataStatement> implements
        AnyDataSchemaNode, DerivableSchemaNode {

    private final AnyDataSchemaNode original;
    private final ContainerSchemaNode schema;

    public AnyDataEffectiveStatementImpl(
            final StmtContext<QName, AnydataStatement, EffectiveStatement<QName, AnydataStatement>> ctx) {
        super(ctx);
        this.original = (AnyDataSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        /*
         * :TODO we need to determine a way how to set schema of AnyData
         */
        this.schema = null;
    }

    @Override
    public Optional<AnyDataSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQName(),getPath());
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

        final AnyDataEffectiveStatementImpl other = (AnyDataEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", getPath()).toString();
    }

    @Override
    public ContainerSchemaNode getSchemaOfAnyData() {
        return schema;
    }
}
