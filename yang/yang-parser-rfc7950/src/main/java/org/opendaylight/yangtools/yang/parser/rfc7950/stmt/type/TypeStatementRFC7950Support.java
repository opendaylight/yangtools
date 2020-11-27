/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Class providing necessary support for processing YANG 1.1 Type statement.
 */
@Beta
public final class TypeStatementRFC7950Support extends AbstractTypeStatementSupport {
    private static final ImmutableMap<String, StatementSupport<?, ?, ?>> ARGUMENT_SPECIFIC_SUPPORTS = ImmutableMap.of(
        LEAF_REF, new LeafrefSpecificationRFC7950Support(),
        IDENTITY_REF, new IdentityrefSpecificationRFC7950Support());
    private static final TypeStatementRFC7950Support INSTANCE = new TypeStatementRFC7950Support();

    private TypeStatementRFC7950Support() {
        // Hidden
    }

    public static TypeStatementRFC7950Support getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean hasArgumentSpecificSupports() {
        return !ARGUMENT_SPECIFIC_SUPPORTS.isEmpty() || super.hasArgumentSpecificSupports();
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        final StatementSupport<?, ?, ?> potential = ARGUMENT_SPECIFIC_SUPPORTS.get(argument);
        return potential != null ? potential : super.getSupportSpecificForArgument(argument);
    }

    @Override
    Bit addRestrictedBit(final EffectiveStmtCtx stmt, final BitsTypeDefinition base, final BitEffectiveStatement bit) {
        // FIXME: this looks like a duplicate of BitsSpecificationEffectiveStatement
        final Optional<Uint32> declaredPosition = bit.getDeclaredPosition();
        final Uint32 effectivePos;
        if (declaredPosition.isEmpty()) {
            effectivePos = getBaseTypeBitPosition(bit.argument(), base, stmt);
        } else {
            effectivePos = declaredPosition.get();
        }

        return EffectiveTypeUtil.buildBit(bit, effectivePos);
    }

    @Override
    EnumPair addRestrictedEnum(final EffectiveStmtCtx stmt, final EnumTypeDefinition base,
            final EnumEffectiveStatement enumStmt) {
        final EnumEffectiveStatement enumSubStmt = enumStmt;
        final Optional<Integer> declaredValue =
                enumSubStmt.findFirstEffectiveSubstatementArgument(ValueEffectiveStatement.class);
        final int effectiveValue;
        if (declaredValue.isEmpty()) {
            effectiveValue = getBaseTypeEnumValue(enumSubStmt.getDeclared().rawArgument(), base, stmt);
        } else {
            effectiveValue = declaredValue.orElseThrow();
        }

        return EffectiveTypeUtil.buildEnumPair(enumSubStmt, effectiveValue);
    }

    private static Uint32 getBaseTypeBitPosition(final String bitName, final BitsTypeDefinition baseType,
            final EffectiveStmtCtx stmt) {
        for (Bit baseTypeBit : baseType.getBits()) {
            if (bitName.equals(baseTypeBit.getName())) {
                return baseTypeBit.getPosition();
            }
        }

        throw new SourceException(stmt.sourceReference(), "Bit '%s' is not a subset of its base bits type %s.", bitName,
            baseType.getQName());
    }


    private static int getBaseTypeEnumValue(final String enumName, final EnumTypeDefinition baseType,
            final EffectiveStmtCtx ctx) {
        for (EnumPair baseTypeEnumPair : baseType.getValues()) {
            if (enumName.equals(baseTypeEnumPair.getName())) {
                return baseTypeEnumPair.getValue();
            }
        }

        throw new SourceException(ctx.sourceReference(), "Enum '%s' is not a subset of its base enumeration type %s.",
            enumName, baseType.getQName());
    }

}
