/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.EnumPairBuilder;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

public final class EnumTypeEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, TypeStatement>
        implements TypeEffectiveStatement<TypeStatement> {

    private final EnumTypeDefinition typeDefinition;

    public EnumTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final EnumTypeDefinition baseType) {
        super(ctx);

        final EnumerationTypeBuilder builder = RestrictedTypes.newEnumerationBuilder(baseType,
                ctx.getSchemaPath().get());

        final YangVersion yangVersion = ctx.getRootVersion();
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof EnumEffectiveStatementImpl) {
                SourceException.throwIf(yangVersion != YangVersion.VERSION_1_1, ctx.getStatementSourceReference(),
                        "Restricted enumeration type is allowed only in YANG 1.1 version.");

                final EnumEffectiveStatementImpl enumSubStmt = (EnumEffectiveStatementImpl) stmt;

                final int effectiveValue;
                if (enumSubStmt.getDeclaredValue() == null) {
                    effectiveValue = getBaseTypeEnumValue(enumSubStmt.getName(), baseType, ctx);
                } else {
                    effectiveValue = enumSubStmt.getDeclaredValue();
                }

                final EnumPair p = EnumPairBuilder.create(enumSubStmt.getName(), effectiveValue)
                        .setDescription(enumSubStmt.getDescription()).setReference(enumSubStmt.getReference())
                        .setStatus(enumSubStmt.getStatus()).setUnknownSchemaNodes(enumSubStmt.getUnknownSchemaNodes())
                        .build();

                builder.addEnum(p);
            } else if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl) stmt);
            }
        }

        typeDefinition = builder.build();
    }

    private static int getBaseTypeEnumValue(final String enumName, final EnumTypeDefinition baseType,
            final StmtContext<?, ?, ?> ctx) {
        for (EnumPair baseTypeEnumPair : baseType.getValues()) {
            if (enumName.equals(baseTypeEnumPair.getName())) {
                return baseTypeEnumPair.getValue();
            }
        }

        throw new SourceException(ctx.getStatementSourceReference(),
                "Enum '%s' is not a subset of its base enumeration type %s.", enumName, baseType.getQName());
    }

    @Nonnull
    @Override
    public EnumTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
