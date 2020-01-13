/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anydata;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveMustConstraintAwareDataSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * YANG 1.1 AnyData effective statement implementation.
 */
@Beta
final class AnydataEffectiveStatementImpl extends AbstractEffectiveMustConstraintAwareDataSchemaNode<AnydataStatement>
        implements AnydataEffectiveStatement, AnydataSchemaNode, DerivableSchemaNode {

    private final AnydataSchemaNode original;
    private final ContainerSchemaNode schema;
    private final boolean mandatory;

    AnydataEffectiveStatementImpl(final StmtContext<QName, AnydataStatement, AnydataEffectiveStatement> ctx) {
        super(ctx);
        this.original = (AnydataSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
        mandatory = findFirstEffectiveSubstatementArgument(MandatoryEffectiveStatement.class).orElse(Boolean.FALSE)
                .booleanValue();

        /*
         * :TODO we need to determine a way how to set schema of AnyData
         */
        this.schema = null;
    }

    @Override
    public Optional<AnydataSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public Optional<ContainerSchemaNode> getDataSchema() {
        return Optional.ofNullable(schema);
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
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

        final AnydataEffectiveStatementImpl other = (AnydataEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("qname", getQName()).add("path", getPath()).toString();
    }
}
