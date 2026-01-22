/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.BitsSpecification;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.UnionSpecification;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

/**
 * Abstract base of all type-related statement support classes.
 */
abstract sealed class AbstractTypeSupport<T extends TypeStatement>
        extends AbstractQNameStatementSupport<TypeStatement, TypeEffectiveStatement>
        permits AbstractTypeStatementSupport, AbstractTypeSupport.Specific {
    abstract static sealed class Specific<T extends TypeStatement> extends AbstractTypeSupport<T>
            permits BitsSpecificationSupport, Decimal64SpecificationSupport, EnumSpecificationSupport,
                    IdentityRefSpecificationSupport, InstanceIdentifierSpecificationSupport,
                    LeafrefSpecificationSupport, UnionSpecificationSupport {
        private final @NonNull Class<T> declaredClass;

        Specific(final YangParserConfiguration config, final SubstatementValidator validator,
                final Class<T> declaredClass) {
            super(config, validator);
            this.declaredClass = requireNonNull(declaredClass);
        }

        @Override
        protected abstract T createDeclared(BoundStmtCtx<QName> ctx, ImmutableList<DeclaredStatement<?>> substatements);

        @Override
        protected final TypeEffectiveStatement createEffective(final Current<QName, TypeStatement> stmt,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            return createEffectiveImpl(verifyNotNull(stmt.tryDeclaring(declaredClass)), substatements);
        }

        abstract @NonNull TypeEffectiveStatement createEffectiveImpl(final @NonNull Current<QName, T> stmt,
                @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);
    }

    private static final ImmutableMap<String, QName> BUILTIN_TYPES = Maps.uniqueIndex(List.of(
        TypeDefinitions.BINARY,
        TypeDefinitions.BITS,
        TypeDefinitions.BOOLEAN,
        TypeDefinitions.DECIMAL64,
        TypeDefinitions.EMPTY,
        TypeDefinitions.ENUMERATION,
        TypeDefinitions.IDENTITYREF,
        TypeDefinitions.INSTANCE_IDENTIFIER,
        TypeDefinitions.INT8,
        TypeDefinitions.INT16,
        TypeDefinitions.INT32,
        TypeDefinitions.INT64,
        TypeDefinitions.LEAFREF,
        TypeDefinitions.STRING,
        TypeDefinitions.UINT8,
        TypeDefinitions.UINT16,
        TypeDefinitions.UINT32,
        TypeDefinitions.UINT64,
        TypeDefinitions.UNION),
        QName::getLocalName);

    AbstractTypeSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(TypeStatement.DEF, StatementPolicy.exactReplica(), config, requireNonNull(validator));
    }

    @Override
    public final String internArgument(final String rawArgument) {
        final QName builtin;
        return (builtin = BUILTIN_TYPES.get(rawArgument)) != null ? builtin.getLocalName() : rawArgument;
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // RFC7950 section 7.3 states that:
        //
        //      The name of the type MUST NOT be one of the YANG built-in types.
        //
        // Therefore, if the string matches one of built-in types, it cannot legally refer to a typedef. Hence consult
        // built in types and if it's not there parse it as a node identifier.
        final String rawArgument = ctx.getRawArgument();
        final QName builtin = BUILTIN_TYPES.get(rawArgument);
        return builtin != null ? builtin : ctx.identifierBinding().parseIdentifierRefArg(ctx, rawArgument);
    }

    @Override
    protected final TypeStatement attachDeclarationReference(final TypeStatement stmt,
            final DeclarationReference reference) {
        return switch (stmt) {
            case BitsSpecification specific -> new RefBitsSpecification(specific, reference);
            case Decimal64Specification specific ->  new RefDecimal64Specification(specific, reference);
            case EnumSpecification specific -> new RefEnumSpecification(specific, reference);
            case IdentityRefSpecification specific -> new RefIdentityRefSpecification(specific, reference);
            case InstanceIdentifierSpecification specific ->
                new RefInstanceIdentifierSpecification(specific, reference);
            case LeafrefSpecification specific -> new RefLeafrefSpecification(specific, reference);
            case UnionSpecification specific -> new RefUnionSpecification(specific, reference);
            default -> DeclaredStatementDecorators.decorateType(stmt, reference);
        };
    }
}
