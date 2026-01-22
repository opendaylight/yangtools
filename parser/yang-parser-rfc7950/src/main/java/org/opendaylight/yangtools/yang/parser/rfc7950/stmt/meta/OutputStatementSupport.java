/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.UndeclaredStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.UndeclaredCurrent;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class OutputStatementSupport
        extends AbstractOperationContainerStatementSupport<OutputStatement, OutputEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(OutputStatement.DEFINITION)
            .addAny(AnyxmlStatement.DEFINITION)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(ContainerStatement.DEFINITION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(LeafStatement.DEFINITION)
            .addAny(LeafListStatement.DEFINITION)
            .addAny(ListStatement.DEFINITION)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(OutputStatement.DEFINITION)
            .addAny(AnydataStatement.DEFINITION)
            .addAny(AnyxmlStatement.DEFINITION)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(ContainerStatement.DEFINITION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(LeafStatement.DEFINITION)
            .addAny(LeafListStatement.DEFINITION)
            .addAny(ListStatement.DEFINITION)
            .addAny(YangStmtMapping.MUST)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .build();

    private OutputStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(OutputStatement.DEFINITION, config, validator, YangConstants::operationOutputQName);
    }

    public static @NonNull OutputStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new OutputStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull OutputStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new OutputStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    protected OutputStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createOutput(ctx.getArgument(), substatements);
    }

    @Override
    protected OutputStatement attachDeclarationReference(final OutputStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateOutput(stmt, reference);
    }

    @Override
    OutputEffectiveStatement copyEffective(final int flags, final Current<QName, OutputStatement> stmt,
            final OutputEffectiveStatement original) {
        return EffectiveStatements.copyOutput(original, stmt.getArgument(), flags);
    }

    @Override
    OutputEffectiveStatement createEffective(final int flags, final Current<QName, OutputStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return EffectiveStatements.createOutput(stmt.declared(), stmt.getArgument(), flags, substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    OutputEffectiveStatement createUndeclaredEffective(final int flags,
            final UndeclaredCurrent<QName, OutputStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return UndeclaredStatements.createOutput(stmt.getArgument(), flags, substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }
}
