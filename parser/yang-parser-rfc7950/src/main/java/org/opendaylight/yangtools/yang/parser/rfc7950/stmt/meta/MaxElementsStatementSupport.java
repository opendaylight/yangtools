/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsValue;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class MaxElementsStatementSupport
        extends AbstractStatementSupport<MaxElementsValue, MaxElementsStatement, MaxElementsEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.MAX_ELEMENTS).build();
    private static final String UNBOUNDED_STR = "unbounded";

    static {
        verify(UNBOUNDED_STR.equals(MaxElementsValue.Unbounded.INSTANCE.toString()));
    }

    public MaxElementsStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.MAX_ELEMENTS, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public String internArgument(final String rawArgument) {
        return switch (rawArgument) {
            case "1" -> "1";
            case "2" -> "2";
            case UNBOUNDED_STR -> UNBOUNDED_STR;
            case null, default -> rawArgument;
        };
    }

    @Override
    public MaxElementsValue parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return MaxElementsValue.ofArgument(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid max-elements argument \"%s\"", value);
        }
    }

    @Override
    protected MaxElementsStatement createDeclared(final BoundStmtCtx<MaxElementsValue> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createMaxElements(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected MaxElementsStatement attachDeclarationReference(final MaxElementsStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateMaxElements(stmt, reference);
    }

    @Override
    protected MaxElementsEffectiveStatement createEffective(final Current<MaxElementsValue, MaxElementsStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createMaxElements(stmt.declared(), substatements);
    }
}
