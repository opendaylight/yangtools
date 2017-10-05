/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class CaseEffectiveStatementImpl extends AbstractEffectiveSimpleDataNodeContainer<CaseStatement> implements
        ChoiceCaseNode, DerivableSchemaNode {

    private final ChoiceCaseNode original;
    private final boolean configuration;

    public CaseEffectiveStatementImpl(
            final StmtContext<QName, CaseStatement, EffectiveStatement<QName, CaseStatement>> ctx) {
        super(ctx);
        this.original = (ChoiceCaseNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);

        if (ctx.isConfiguration()) {
            configuration = ctx.allSubstatementsStream().anyMatch(StmtContext::isConfiguration);
        } else {
            configuration = false;
        }
    }

    @Override
    public Optional<ChoiceCaseNode> getOriginal() {
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
