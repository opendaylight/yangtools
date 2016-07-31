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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.BitsSpecification;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.BitBuilder;
import org.opendaylight.yangtools.yang.model.util.type.BitsTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

public final class BitsSpecificationEffectiveStatementImpl extends
        DeclaredEffectiveStatementBase<String, BitsSpecification> implements TypeEffectiveStatement<BitsSpecification> {

    private final BitsTypeDefinition typeDefinition;

    public BitsSpecificationEffectiveStatementImpl(final StmtContext<String, BitsSpecification, EffectiveStatement<String, BitsSpecification>> ctx) {
        super(ctx);

        final BitsTypeBuilder builder = BaseTypes.bitsTypeBuilder(ctx.getSchemaPath().get());
        Long highestPosition = null;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof Bit) {
                Bit b = (Bit) stmt;

                if (b.getPosition() == null) {
                    final Long newPos;
                    if (highestPosition == null) {
                        newPos = 0L;
                    } else if (highestPosition != 4294967295L) {
                        newPos = highestPosition + 1;
                    } else {
                        throw new SourceException(ctx.getStatementSourceReference(),
                            "Bit %s must have a position statement", b);
                    }

                    b = BitBuilder.create(b.getPath(), newPos).setDescription(b.getDescription())
                            .setReference(b.getReference()).setStatus(b.getStatus())
                            .setUnknownSchemaNodes(b.getUnknownSchemaNodes()).build();
                }

                SourceException.throwIf(b.getPosition() < 0L && b.getPosition() > 4294967295L,
                        ctx.getStatementSourceReference(), "Bit %s has illegal position", b);

                if (highestPosition == null || highestPosition < b.getPosition()) {
                    highestPosition = b.getPosition();
                }

                builder.addBit(b);
            }
            if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Override
    public BitsTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
