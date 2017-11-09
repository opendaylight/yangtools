/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.InvalidRangeConstraintException;
import org.opendaylight.yangtools.yang.model.util.type.RangeRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;

public final class IntegralTypeEffectiveStatementImpl<T extends RangeRestrictedTypeDefinition<T, N>,
        N extends Number & Comparable<N>> extends DeclaredEffectiveStatementBase<String, TypeStatement>
        implements TypeEffectiveStatement<TypeStatement> {

    private final T typeDefinition;

    private IntegralTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final RangeRestrictedTypeBuilder<T, N> builder) {
        super(ctx);

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof RangeEffectiveStatementImpl) {
                final RangeEffectiveStatementImpl rangeStmt = (RangeEffectiveStatementImpl)stmt;
                builder.setRangeConstraint(rangeStmt, rangeStmt.argument());
            }
            if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode)stmt);
            }
        }

        try {
            typeDefinition = builder.build();
        } catch (InvalidRangeConstraintException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid range constraint: %s",
                e.getOffendingRanges());
        }
    }

    public static IntegralTypeEffectiveStatementImpl<Int8TypeDefinition, Byte> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Int8TypeDefinition baseType) {
        return new IntegralTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newInt8Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static IntegralTypeEffectiveStatementImpl<Int16TypeDefinition, Short> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Int16TypeDefinition baseType) {
        return new IntegralTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newInt16Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static IntegralTypeEffectiveStatementImpl<Int32TypeDefinition, Integer> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Int32TypeDefinition baseType) {
        return new IntegralTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newInt32Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static IntegralTypeEffectiveStatementImpl<Int64TypeDefinition, Long> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Int64TypeDefinition baseType) {
        return new IntegralTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newInt64Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static IntegralTypeEffectiveStatementImpl<Uint8TypeDefinition, Short> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Uint8TypeDefinition baseType) {
        return new IntegralTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newUint8Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static IntegralTypeEffectiveStatementImpl<Uint16TypeDefinition, Integer> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Uint16TypeDefinition baseType) {
        return new IntegralTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newUint16Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static IntegralTypeEffectiveStatementImpl<Uint32TypeDefinition, Long> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Uint32TypeDefinition baseType) {
        return new IntegralTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newUint32Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static IntegralTypeEffectiveStatementImpl<Uint64TypeDefinition, BigInteger> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Uint64TypeDefinition baseType) {
        return new IntegralTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newUint64Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    @Nonnull
    @Override
    public T getTypeDefinition() {
        return typeDefinition;
    }
}
