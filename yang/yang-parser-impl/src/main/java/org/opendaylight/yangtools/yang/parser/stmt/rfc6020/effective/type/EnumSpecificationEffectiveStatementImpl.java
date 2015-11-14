/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.EnumPairImpl;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

public final class EnumSpecificationEffectiveStatementImpl extends
        DeclaredEffectiveStatementBase<String, EnumSpecification> implements
        TypeEffectiveStatement<EnumSpecification> {

    private final EnumTypeDefinition typeDefinition;

    public EnumSpecificationEffectiveStatementImpl(final StmtContext<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>> ctx) {
        super(ctx);

        final EnumerationTypeBuilder builder = BaseTypes.enumerationTypeBuilder(ctx.getSchemaPath().get());
        Integer highestValue = null;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof EnumPair) {
                EnumPair p = (EnumPair) stmt;

                if (p.getValue() == null) {
                    final Integer newValue;
                    if (highestValue == null) {
                        newValue = 0;
                    } else if (highestValue != 2147483647) {
                        newValue = highestValue + 1;
                    } else {
                        throw new SourceException("Enum " + p + " must have a value statement",
                            ctx.getStatementSourceReference());
                    }

                    p = new EnumPairImpl(p.getName(), newValue, p.getPath(), p.getDescription(), p.getReference(),
                        p.getStatus(), p.getUnknownSchemaNodes());
                }

                if (highestValue == null || highestValue < p.getValue()) {
                    highestValue = p.getValue();
                }

                builder.addEnum(p);
            }
            if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Override
    public EnumTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
