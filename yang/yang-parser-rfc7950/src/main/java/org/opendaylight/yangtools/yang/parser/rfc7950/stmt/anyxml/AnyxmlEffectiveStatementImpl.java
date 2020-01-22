/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml;

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveMustConstraintAwareDataSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class AnyxmlEffectiveStatementImpl extends AbstractEffectiveMustConstraintAwareDataSchemaNode<AnyxmlStatement>
        implements AnyxmlEffectiveStatement, AnyxmlSchemaNode, DerivableSchemaNode {

    private final AnyxmlSchemaNode original;
    private final boolean mandatory;

    AnyxmlEffectiveStatementImpl(final StmtContext<QName, AnyxmlStatement, AnyxmlEffectiveStatement> ctx) {
        super(ctx);
        this.original = (AnyxmlSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        mandatory = findFirstEffectiveSubstatementArgument(MandatoryEffectiveStatement.class).orElse(Boolean.FALSE)
                .booleanValue();
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public Optional<AnyxmlSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public String toString() {
        return AnyxmlEffectiveStatementImpl.class.getSimpleName() + "["
                + "qname=" + getQName()
                + ", path=" + getPath()
                + "]";
    }
}
