/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

/**
 * Abstract base of all type-related statement support classes.
 */
abstract class AbstractTypeSupport<T extends TypeStatement>
        extends AbstractQNameStatementSupport<T, EffectiveStatement<QName, T>> {
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
        super(YangStmtMapping.TYPE, StatementPolicy.exactReplica(), config, requireNonNull(validator));
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
}
