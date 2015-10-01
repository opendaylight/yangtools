/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.InstanceIdentifierType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RequireInstanceEffectiveStatementImpl;

public final class InstanceIdentifierSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, InstanceIdentifierSpecification>
        implements TypeEffectiveStatement<InstanceIdentifierSpecification> {

    public InstanceIdentifierSpecificationEffectiveStatementImpl(
            final StmtContext<String, InstanceIdentifierSpecification, EffectiveStatement<String, InstanceIdentifierSpecification>> ctx) {
        super(ctx);
    }

    @Override
    public TypeDefinitionBuilder<InstanceIdentifierTypeDefinition> newTypeDefinitionBuilder() {
        return new AbstractTypeDefinitionBuilder<InstanceIdentifierTypeDefinition>() {
            private boolean requireInstance;

            @Override
            protected void addEffectiveStatement(@Nonnull final EffectiveStatement<?, ?> stmt) {
                if (stmt instanceof RequireInstanceEffectiveStatementImpl) {
                    final boolean require = ((RequireInstanceEffectiveStatementImpl) stmt).argument();
                    if (requireInstance) {
                        Preconditions.checkArgument(require, "Attempted to weaken require-instance");
                    }

                    requireInstance = require;
                }
            }

            @Override
            public InstanceIdentifierTypeDefinition build() {
                // FIXME: this is not entirely right
                return InstanceIdentifierType.create(requireInstance);
            }
        };
    }
}
