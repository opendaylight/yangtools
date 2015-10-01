/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public final class EnumSpecificationEffectiveStatementImpl extends EffectiveStatementBase<String, EnumSpecification>
        implements TypeEffectiveStatement<EnumSpecification> {
    public EnumSpecificationEffectiveStatementImpl(final StmtContext<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>> ctx) {
        super(ctx);
    }

    @Override
    public TypeDefinitionBuilder<EnumTypeDefinition> newTypeDefinitionBuilder() {
        return new AbstractTypeDefinitionBuilder<EnumTypeDefinition>() {
            private final List<EnumPair> enums = new ArrayList<>();

            @Override
            protected void addEffectiveStatement(@Nonnull final EffectiveStatement<?, ?> stmt) {
                if (stmt instanceof EnumEffectiveStatementImpl) {
                    enums.add(((EnumEffectiveStatementImpl) stmt).asEnumPair());
                } else {
                    super.addEffectiveStatement(stmt);
                }
            }

            @Override
            public EnumTypeDefinition build() {
                final String defaultValue = getDefaultValue();
                EnumPair defaultPair = null;
                if (defaultValue != null) {
                    for (EnumPair e : enums) {
                        if (defaultValue.equals(e.getName())) {
                            defaultPair = e;
                            break;
                        }
                    }
                }

                // FIXME: this is not quite right
                return EnumerationType.create(getPath(), enums, Optional.fromNullable(defaultPair));
            }
        };
    }
}
