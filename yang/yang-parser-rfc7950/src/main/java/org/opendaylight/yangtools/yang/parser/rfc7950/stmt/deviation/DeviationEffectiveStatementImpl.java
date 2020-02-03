/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;

final class DeviationEffectiveStatementImpl extends WithSubstatements<SchemaNodeIdentifier, DeviationStatement>
        implements DeviationEffectiveStatement, Deviation,
            DocumentedNodeMixin<SchemaNodeIdentifier, DeviationStatement> {
    DeviationEffectiveStatementImpl(final DeviationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }

    @Override
    public SchemaPath getTargetPath() {
        return argument().asSchemaPath();
    }

    @Override
    public List<DeviateDefinition> getDeviates() {
        return filterEffectiveStatementsList(DeviateDefinition.class);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTargetPath(), getDeviates(), getDescription().orElse(null), getReference().orElse(null));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DeviationEffectiveStatementImpl)) {
            return false;
        }
        final DeviationEffectiveStatementImpl other = (DeviationEffectiveStatementImpl) obj;
        return Objects.equals(getTargetPath(), other.getTargetPath())
                && Objects.equals(getDeviates(), other.getDeviates())
                && Objects.equals(getDescription(),  other.getDescription())
                && Objects.equals(getReference(), other.getReference());
    }

    @Override
    public String toString() {
        return DeviationEffectiveStatementImpl.class.getSimpleName() + "["
                + "targetPath=" + getTargetPath()
                + ", deviates=" + getDeviates()
                + ", description=" + getDescription().orElse(null)
                + ", reference=" + getReference().orElse(null)
                + "]";
    }
}
