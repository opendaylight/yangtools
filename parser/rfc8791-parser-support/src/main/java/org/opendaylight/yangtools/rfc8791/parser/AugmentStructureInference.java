/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureArgument;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Inference action, split out of {@link AugmentStructureStatementSupport} for clarity and reuse.
 */
final class AugmentStructureInference implements InferenceAction {
    private final @NonNull Mutable<AugmentStructureArgument, ?, ?> augmentStmt;
    private final @NonNull Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> targetReq;

    AugmentStructureInference(final Mutable<AugmentStructureArgument, ?, ?> augmentStmt,
            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> targetReq) {
        this.augmentStmt = requireNonNull(augmentStmt);
        this.targetReq = requireNonNull(targetReq);
    }

    @Override
    public void apply(final InferenceContext ctx) {
        final var target = targetReq.resolve(ctx);

        // FIXME: implement this
        throw new UnsupportedOperationException(
            "Unimplemented augmentation of " + target + " by " + augmentStmt);
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        throw new InferenceException(augmentStmt, "Augment structure target'%s' not found", augmentStmt.getArgument());
    }
}
