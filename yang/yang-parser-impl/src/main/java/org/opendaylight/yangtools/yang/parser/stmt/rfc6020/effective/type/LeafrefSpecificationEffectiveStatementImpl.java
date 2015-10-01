/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.Leafref;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PathEffectiveStatementImpl;

public final class LeafrefSpecificationEffectiveStatementImpl extends EffectiveStatementBase<String, LeafrefSpecification>
        implements TypeEffectiveStatement<LeafrefSpecification> {

    public LeafrefSpecificationEffectiveStatementImpl(final StmtContext<String, LeafrefSpecification, EffectiveStatement<String, LeafrefSpecification>> ctx) {
        super(ctx);
    }

    @Override
    public TypeDefinitionBuilder<LeafrefTypeDefinition> newTypeDefinitionBuilder() {
        return new AbstractTypeDefinitionBuilder<LeafrefTypeDefinition>() {
            private RevisionAwareXPath xpath = null;

            @Override
            protected void addEffectiveStatement(@Nonnull final EffectiveStatement<?, ?> stmt) {
                if (stmt instanceof PathEffectiveStatementImpl) {
                    xpath = ((PathEffectiveStatementImpl) stmt).argument();
                } else {
                    super.addEffectiveStatement(stmt);
                }
            }

            @Override
            public LeafrefTypeDefinition build() {
                // FIXME: this is not quite right
                return Leafref.create(getPath(), xpath);
            }
        };
    }
}
