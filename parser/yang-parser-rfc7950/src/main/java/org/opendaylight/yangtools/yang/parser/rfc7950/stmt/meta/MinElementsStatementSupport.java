/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractInternedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class MinElementsStatementSupport
        extends AbstractInternedStatementSupport<Integer, MinElementsStatement, MinElementsEffectiveStatement> {
    private static final CharMatcher DIGIT = CharMatcher.inRange('0', '9');

    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.MIN_ELEMENTS).build();

    public MinElementsStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.MIN_ELEMENTS, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public String internArgument(final String rawArgument) {
        return rawArgument == null ? null : switch (rawArgument) {
            case "0" -> "0";
            case "1" -> "1";
            case "2" -> "2";
            default -> rawArgument;
        };
    }

    @Override
    public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final var len = value.length();
        if (len != 0 && DIGIT.matchesAllOf(value)) {
            final var ch = value.charAt(0);
            if (len == 1) {
                return ch - '0';
            }
            if (ch != '0') {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new SourceException(ctx, e, "Invalid min-elements argument \"%s\"", value);
                }
            }
        }
        throw new SourceException(ctx, "Invalid min-elements argument \"%s\"", value);
    }

    @Override
    protected MinElementsStatement createDeclared(final Integer argument,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createMinElements(argument, substatements);
    }

    @Override
    protected MinElementsStatement attachDeclarationReference(final MinElementsStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateMinElements(stmt, reference);
    }

    @Override
    protected MinElementsStatement createEmptyDeclared(final Integer argument) {
        return DeclaredStatements.createMinElements(argument);
    }

    @Override
    protected MinElementsEffectiveStatement createEffective(final MinElementsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createMinElements(declared, substatements);
    }

    @Override
    protected MinElementsEffectiveStatement createEmptyEffective(final MinElementsStatement declared) {
        return EffectiveStatements.createMinElements(declared);
    }
}
