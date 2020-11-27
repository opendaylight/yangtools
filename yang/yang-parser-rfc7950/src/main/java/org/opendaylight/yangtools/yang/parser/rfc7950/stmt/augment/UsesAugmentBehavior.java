/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Behavior of an {@code augment} inside of a {@code uses} statement.
 */
final class UsesAugmentBehavior extends AbstractAugmentBehavior<Descendant> {
    static final UsesAugmentBehavior INSTANCE = new UsesAugmentBehavior();

    private UsesAugmentBehavior() {
        // Hidden on purpose;
    }

    @Override
    Descendant parseArgument(final StmtContext<?, ?, ?> ctx, final String value) {
        SourceException.throwIf(value.startsWith("/"), ctx.sourceReference(),
            "Absolute schema node identifier is not allowed when used within a uses statement");
        return ArgumentUtils.parseDescendantSchemaNodeIdentifier(ctx, value);
    }

    @Override
    StmtContext<?, ?, ?> searchRoot(final StmtContext<?, ?, ?> ctx) {
        return ctx.coerceParentContext();
    }

    @Override
    InferenceAction inferenceAction(
            final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> ctx,
            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target, final boolean allowsMandatory) {
        return new UserAugmentInference(ctx, target, allowsMandatory);
    }
}
