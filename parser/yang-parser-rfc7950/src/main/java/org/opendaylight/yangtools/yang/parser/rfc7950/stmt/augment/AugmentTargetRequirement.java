/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * An inference action triggered when augmentation target's effective model becomes available. This action has the sole
 * purpose of populating XXXNamespace with target's effective model.
 */
final class AugmentTargetRequirement implements InferenceAction {
    private final @NonNull Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augment;
    private final @NonNull Prerequisite<EffectiveStatement<?, ?>> req;

    AugmentTargetRequirement(final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augment,
            final Prerequisite<EffectiveStatement<?, ?>> req) {
        this.augment = requireNonNull(augment);
        this.req = requireNonNull(req);
    }

    @Override
    public void apply(final InferenceContext ctx) {
        final var reqStmt = req.resolve(ctx);
        verify(reqStmt instanceof SchemaTreeAwareEffectiveStatement,
            "Undefined execution on statement %s not recognizing RFC7950 schema tree", reqStmt);
        augment.addToNs(AugmentTargetNamespace.class, Empty.value(), (SchemaTreeAwareEffectiveStatement<?, ?>) reqStmt);
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        throw new InferenceException(augment, "Augment target '%s' failed to reach effective model",
            augment.argument());
    }

}
