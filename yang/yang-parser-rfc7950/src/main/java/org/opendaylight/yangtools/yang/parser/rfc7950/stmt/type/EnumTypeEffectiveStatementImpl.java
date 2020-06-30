/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class EnumTypeEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, TypeStatement>
        implements TypeEffectiveStatement<TypeStatement> {

    private final @NonNull EnumTypeDefinition typeDefinition;

    EnumTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final EnumTypeDefinition baseType) {
        super(ctx);

        final EnumerationTypeBuilder builder = RestrictedTypes.newEnumerationBuilder(baseType,
                ctx.getSchemaPath().get());

        final YangVersion yangVersion = ctx.getRootVersion();
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof EnumEffectiveStatement) {
                SourceException.throwIf(yangVersion != YangVersion.VERSION_1_1, ctx.getStatementSourceReference(),
                        "Restricted enumeration type is allowed only in YANG 1.1 version.");

                final EnumEffectiveStatement enumSubStmt = (EnumEffectiveStatement) stmt;
                final Optional<Integer> declaredValue =
                        enumSubStmt.findFirstEffectiveSubstatementArgument(ValueEffectiveStatement.class);
                final int effectiveValue;
                if (declaredValue.isEmpty()) {
                    effectiveValue = getBaseTypeEnumValue(enumSubStmt.getDeclared().rawArgument(), baseType, ctx);
                } else {
                    effectiveValue = declaredValue.orElseThrow();
                }

                builder.addEnum(EffectiveTypeUtil.buildEnumPair(enumSubStmt, effectiveValue));
            } else if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode) stmt);
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

    @Override
    public EnumTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
