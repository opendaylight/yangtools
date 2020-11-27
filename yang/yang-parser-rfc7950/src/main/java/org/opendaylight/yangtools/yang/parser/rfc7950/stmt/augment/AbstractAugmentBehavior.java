/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Base class for the two distinct behaviors of augment:
 * <ul>
 *   <li>as an immediate child of a {@code module} or a {@code submodule}, backed by {@link RootAugmentBehavior}</li>
 *   <li>as an immediate child of a {@code uses}, backed by {@link UsesAugmentBehavior}</li>
 * </ul>
 * @author nite
 *
 */
abstract class AbstractAugmentBehavior<T extends SchemaNodeIdentifier> {
    static @NonNull SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // As per:
        //   https://tools.ietf.org/html/rfc6020#section-7.15
        //   https://tools.ietf.org/html/rfc7950#section-7.17
        //
        // The argument is either Absolute or Descendant based on whether the statement is declared within a 'uses'
        // statement. The mechanics differs wildly between the two cases, so let's start by ensuring our argument
        // is in the correct domain.
        return selectBehavior(ctx).parseArgument(ctx, value);
    }

    static void onFullDefinitionDeclared(
            final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> ctx,
            final boolean allowsMandatory) {
        final AbstractAugmentBehavior<?> behavior =  selectBehavior(ctx);

        final ModelActionBuilder augmentAction = ctx.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        augmentAction.requiresCtx(ctx, ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target = augmentAction.mutatesEffectiveCtxPath(
            behavior.searchRoot(ctx), SchemaTreeNamespace.class, ctx.getArgument().getNodeIdentifiers());

        augmentAction.apply(behavior.inferenceAction(ctx, target, allowsMandatory));
    }

    abstract InferenceAction inferenceAction(
        Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> ctx,
        Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target, boolean allowsMandatory);

    abstract @NonNull T parseArgument(StmtContext<?, ?, ?> ctx, String value);

    abstract @NonNull StmtContext<?, ?, ?> searchRoot(@NonNull StmtContext<?, ?, ?> ctx);

    private static AbstractAugmentBehavior<?> selectBehavior(final StmtContext<?, ?, ?> ctx) {
        return ctx.coerceParentContext().publicDefinition() == YangStmtMapping.USES
            ? UsesAugmentBehavior.INSTANCE : RootAugmentBehavior.INSTANCE;
    }
}
