/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDataSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Internal implementation of AnyxmlEffectiveStatement.
 *
 * @deprecated This class is visible only for historical purposes and is going to be hidden.
 */
// FIXME: 3.0.0: hide this class and make it final
@Deprecated
public class AnyxmlEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<AnyxmlStatement>
        implements AnyxmlEffectiveStatement, AnyXmlSchemaNode, DerivableSchemaNode {

    private final ImmutableSet<MustDefinition> mustConstraints;
    private final AnyXmlSchemaNode original;
    private final boolean mandatory;

    AnyxmlEffectiveStatementImpl(final StmtContext<QName, AnyxmlStatement, AnyxmlEffectiveStatement> ctx) {
        super(ctx);
        this.original = (AnyXmlSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        mandatory = findFirstEffectiveSubstatementArgument(MandatoryEffectiveStatement.class).orElse(Boolean.FALSE)
                .booleanValue();
        mustConstraints = ImmutableSet.copyOf(allSubstatementsOfType(MustDefinition.class));
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public Collection<MustDefinition> getMustConstraints() {
        return mustConstraints;
    }

    @Override
    public Optional<AnyXmlSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
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

        AnyxmlEffectiveStatementImpl other = (AnyxmlEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return AnyxmlEffectiveStatementImpl.class.getSimpleName() + "["
                + "qname=" + getQName()
                + ", path=" + getPath()
                + "]";
    }
}
