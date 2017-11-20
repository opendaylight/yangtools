/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.case_;

import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveSimpleDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class CaseEffectiveStatementImpl extends AbstractEffectiveSimpleDataNodeContainer<CaseStatement>
        implements CaseEffectiveStatement, CaseSchemaNode, DerivableSchemaNode {

    private final CaseSchemaNode original;
    private final boolean configuration;

    CaseEffectiveStatementImpl(final StmtContext<QName, CaseStatement, EffectiveStatement<QName, CaseStatement>> ctx) {
        super(ctx);
        this.original = (CaseSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);

        if (ctx.isConfiguration()) {
            configuration = ctx.allSubstatementsStream().anyMatch(StmtContext::isConfiguration);
        } else {
            configuration = false;
        }
    }

    @Override
    public Optional<CaseSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getQName());
        result = prime * result + Objects.hashCode(getPath());
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
        CaseEffectiveStatementImpl other = (CaseEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return CaseEffectiveStatementImpl.class.getSimpleName() + "["
                + "qname=" + getQName()
                + "]";
    }
}
