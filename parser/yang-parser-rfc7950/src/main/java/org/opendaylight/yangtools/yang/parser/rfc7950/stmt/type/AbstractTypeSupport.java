/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.BuiltInType;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

/**
 * Abstract base of all type-related statement support classes.
 */
abstract sealed class AbstractTypeSupport extends AbstractQNameStatementSupport<TypeStatement, TypeEffectiveStatement>
        permits AbstractTypeStatementSupport, BitsSpecificationSupport, Decimal64SpecificationSupport,
                EnumSpecificationSupport, IdentityRefSpecificationSupport, InstanceIdentifierSpecificationSupport,
                LeafrefSpecificationSupport, UnionSpecificationSupport {
    AbstractTypeSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(TypeStatement.DEF, StatementPolicy.exactReplica(), config, requireNonNull(validator));
    }

    @Override
    public final String internArgument(final String rawArgument) {
        final BuiltInType builtin;
        return (builtin = BuiltInType.forSimpleName(rawArgument)) != null ? builtin.simpleName() : rawArgument;
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // RFC7950 section 7.3 states that:
        //
        //      The name of the type MUST NOT be one of the YANG built-in types.
        //
        // Therefore, if the string matches one of built-in types, it cannot legally refer to a typedef. Hence consult
        // built in types and if it's not there parse it as a node identifier.
        final var rawArgument = ctx.getRawArgument();
        final var builtin = BuiltInType.forSimpleName(rawArgument);
        return builtin != null ? builtin.typeName() : ctx.identifierBinding().parseIdentifierRefArg(ctx, rawArgument);
    }

    @Override
    protected final TypeStatement attachDeclarationReference(final TypeStatement stmt,
            final DeclarationReference reference) {
        return switch (stmt) {
            case TypeStatement.OfBits specific -> new RefBitsSpecification(specific, reference);
            case TypeStatement.OfDecimal64 specific ->  new RefDecimal64Specification(specific, reference);
            case TypeStatement.OfEnum specific -> new RefEnumSpecification(specific, reference);
            case TypeStatement.OfIdentityref specific -> new RefIdentityRefSpecification(specific, reference);
            case TypeStatement.OfInstanceIdentifier specific ->
                new RefInstanceIdentifierSpecification(specific, reference);
            case TypeStatement.OfLeafref specific -> new RefLeafrefSpecification(specific, reference);
            case TypeStatement.OfUnion specific -> new RefUnionSpecification(specific, reference);
            default -> DeclaredStatementDecorators.decorateType(stmt, reference);
        };
    }
}
