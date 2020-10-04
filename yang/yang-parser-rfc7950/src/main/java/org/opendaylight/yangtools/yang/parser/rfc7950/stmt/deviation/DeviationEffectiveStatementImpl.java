/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviation;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;

final class DeviationEffectiveStatementImpl extends WithSubstatements<Absolute, DeviationStatement>
        implements DeviationEffectiveStatement, Deviation, DocumentedNodeMixin<Absolute, DeviationStatement> {
    DeviationEffectiveStatementImpl(final DeviationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }

    @Override
    public Absolute getTargetPath() {
        return verifyNotNull(argument());
    }

    @Override
    public Collection<? extends DeviateDefinition> getDeviates() {
        return filterEffectiveStatements(DeviateDefinition.class);
    }

    @Override
    public DeviationEffectiveStatement asEffectiveStatement() {
        return this;
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
