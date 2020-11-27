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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Behavior of an {@code augment} outside of a {@code uses} statement.
 */
final class RootAugmentBehavior extends AbstractAugmentBehavior<Absolute> {
    static final RootAugmentBehavior INSTANCE = new RootAugmentBehavior();

    private RootAugmentBehavior() {
        // Hidden on purpose;
    }

    @Override
    Absolute parseArgument(final StmtContext<?, ?, ?> ctx, final String value) {
        SourceException.throwIf(!value.startsWith("/"), ctx.sourceReference(),
            "Descendant schema node identifier is not allowed when used outside of a uses statement");
        return ArgumentUtils.parseAbsoluteSchemaNodeIdentifier(ctx, value);
    }

    @Override
    StmtContext<?, ?, ?> searchRoot(final StmtContext<?, ?, ?> ctx) {
        return ctx;
    }

    @Override
    InferenceAction inferenceAction(
            final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> ctx,
            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target, final boolean allowsMandatory) {
        return new RootAugmentInference(ctx, target, allowsMandatory);
    }
}
