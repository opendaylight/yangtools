/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class TypedefStatementSupport extends
        AbstractQNameStatementSupport<TypedefStatement, TypedefEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPEDEF)
        .addOptional(YangStmtMapping.DEFAULT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addMandatory(YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.UNITS)
        .build();

    public TypedefStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.TYPEDEF, StatementPolicy.exactReplica(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ctx.parseIdentifier(value);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, TypedefStatement, TypedefEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final Mutable<?, ?, ?> parent = stmt.getParentContext();
        if (parent != null) {
            // Shadowing check: make sure we do not trample on pre-existing definitions. This catches sibling
            // declarations and parent declarations which have already been declared.
            checkConflict(parent, stmt);
            parent.addToNs(ParserNamespaces.TYPE, stmt.getArgument(), stmt);

            final StmtContext<?, ?, ?> grandParent = parent.getParentContext();
            if (grandParent != null) {
                // Shadowing check: make sure grandparent does not see a conflicting definition. This is required to
                // ensure that a typedef in child scope does not shadow a typedef in parent scope which occurs later in
                // the text. For that check we need the full declaration of our model.

                final ModelActionBuilder action = stmt.newInferenceAction(ModelProcessingPhase.FULL_DECLARATION);
                action.requiresCtx(grandParent.getRoot(), ModelProcessingPhase.FULL_DECLARATION);
                action.apply(new InferenceAction() {
                    @Override
                    public void apply(final InferenceContext ctx) {
                        checkConflict(grandParent, stmt);
                    }

                    @Override
                    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                        // No-op
                    }
                });
            }
        }
    }

    @Override
    protected TypedefStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createTypedef(ctx.getArgument(), substatements);
    }

    @Override
    protected TypedefStatement attachDeclarationReference(final TypedefStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateTypedef(stmt, reference);
    }

    @Override
    protected TypedefEffectiveStatement createEffective(final Current<QName, TypedefStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final TypedefStatement declared = stmt.declared();
        checkState(!substatements.isEmpty(), "Refusing to create empty typedef for %s", stmt.declared());

        final TypeEffectiveStatement<?> typeEffectiveStmt = findFirstStatement(substatements,
            TypeEffectiveStatement.class);
        final String dflt = findFirstArgument(substatements, DefaultEffectiveStatement.class, null);
        SourceException.throwIf(
            EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(stmt.yangVersion(), typeEffectiveStmt, dflt), stmt,
            "Typedef '%s' has default value '%s' marked with an if-feature statement.", stmt.argument(), dflt);

        return EffectiveStatements.createTypedef(declared, computeFlags(substatements), substatements);
    }

    private static void checkConflict(final StmtContext<?, ?, ?> parent, final StmtContext<QName, ?, ?> stmt) {
        final QName arg = stmt.getArgument();
        final StmtContext<?, ?, ?> existing = parent.namespaceItem(ParserNamespaces.TYPE, arg);
        // RFC7950 sections 5.5 and 6.2.1: identifiers must not be shadowed
        SourceException.throwIf(existing != null, stmt, "Duplicate name for typedef %s", arg);
    }

    private static int computeFlags(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();
    }
}
