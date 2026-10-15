/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class FeatureStatementSupport
        extends AbstractQNameStatementSupport<FeatureStatement, FeatureEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(FeatureStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addAny(IfFeatureStatement.DEF)
            .addOptional(StatusStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .build();

    public FeatureStatementSupport(final YangParserConfiguration config) {
        super(FeatureStatement.DEF, StatementPolicy.reject(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, FeatureStatement, FeatureEffectiveStatement> stmt) {
        // Do not build effective statement if the set of supported features does not include this feature
        final var featureSet = stmt.namespaceItem(ParserNamespaces.SUPPORTED_FEATURES, Empty.value());
        if (featureSet != null && !featureSet.contains(stmt.getArgument())) {
            stmt.setUnsupported();
        }
    }

    @Override
    public void onStatementDefinitionDeclared(final Mutable<QName, FeatureStatement, FeatureEffectiveStatement> stmt) {
        // RFC7950 section 6.2.1:
        //        o  All feature names defined in a module and its submodules share the
        //           same feature identifier namespace.
        if (stmt.coerceParentContext().producesAnyOf(ModuleStatement.DEF, SubmoduleStatement.DEF)) {
            stmt.putToNs(ParserNamespaces.FEATURE, stmt.getArgument(), stmt, (prev, next) ->
                new SourceException(next, "Feature name collision: '%s' already declared at %s", next.getArgument(),
                    prev.sourceReference()));
        }
    }

    @Override
    protected FeatureStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createFeature(ctx.getArgument(), substatements);
    }

    @Override
    protected FeatureStatement attachDeclarationReference(final FeatureStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateFeature(stmt, reference);
    }

    @Override
    protected FeatureEffectiveStatement createEffective(final Current<QName, FeatureStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createFeature(stmt.declared(), computeFlags(substatements), substatements);
    }

    private static int computeFlags(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();
    }
}
