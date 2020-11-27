/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractAugmentStatementSupport
        extends BaseStatementSupport<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAugmentStatementSupport.class);

    AbstractAugmentStatementSupport() {
        super(YangStmtMapping.AUGMENT);
    }

    @Override
    public final SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return AbstractAugmentBehavior.parseArgumentValue(ctx, value);
    }

    @Override
    public final void onFullDefinitionDeclared(
            final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augmentNode) {
        if (!augmentNode.isSupportedByFeatures()) {
            // We need this augment node to be present, but it should not escape to effective world
            augmentNode.setIsSupportedToBuildEffective(false);
        }

        super.onFullDefinitionDeclared(augmentNode);

        if (!StmtContextUtils.isInExtensionBody(augmentNode)) {
            AbstractAugmentBehavior.onFullDefinitionDeclared(augmentNode, allowsMandatory(augmentNode));
        }
    }

    @Override
    protected final AugmentStatement createDeclared(final StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularAugmentStatement(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected final AugmentStatement createEmptyDeclared(
            final StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> ctx) {
        return new EmptyAugmentStatement(ctx.getRawArgument(), ctx.getArgument());
    }

    @Override
    protected final List<? extends StmtContext<?, ?, ?>> statementsToBuild(
            final Current<SchemaNodeIdentifier, AugmentStatement> stmt,
            final List<? extends StmtContext<?, ?, ?>> substatements) {
        // Pick up the marker left by onFullDefinitionDeclared() inference action. If it is present we need to pass our
        // children through target's implicit wrapping.
        final StatementContextBase<?, ?, ?> implicitDef = stmt.getFromNamespace(AugmentImplicitHandlingNamespace.class,
            stmt.caerbannog());
        return implicitDef == null ? substatements : Lists.transform(substatements, subCtx -> {
            verify(subCtx instanceof StatementContextBase);
            return implicitDef.wrapWithImplicit((StatementContextBase<?, ?, ?>) subCtx);
        });
    }

    @Override
    protected final AugmentEffectiveStatement createEffective(
            final Current<SchemaNodeIdentifier, AugmentStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final int flags = new FlagsBuilder()
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();

        try {
            return new AugmentEffectiveStatementImpl(stmt.declared(), stmt.getArgument(), flags,
                StmtContextUtils.getRootModuleQName(stmt.caerbannog()), substatements,
                (AugmentationSchemaNode) stmt.original());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt.sourceReference(), e);
        }
    }

    /**
     * Since YANG 1.1, if an augmentation is made conditional with a "when" statement, it is allowed to add mandatory
     * nodes.
     */
    abstract boolean allowsMandatory(StmtContext<?, ?, ?> ctx);

    static boolean hasWhenSubstatement(final StmtContext<?, ?, ?> ctx) {
        return ctx.hasSubstatement(WhenEffectiveStatement.class);
    }
}
