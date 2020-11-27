/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UserAugmentInference extends AbstractAugmentInference {
    private static final Logger LOG = LoggerFactory.getLogger(UserAugmentInference.class);

    UserAugmentInference(final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> ctx,
            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target, final boolean allowsMandatory) {
        super(ctx, target, allowsMandatory);
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        final SchemaNodeIdentifier augmentArg = augment.getArgument();
        final Optional<StmtContext<?, ?, ?>> targetNode =
            SchemaTreeNamespace.findNode(augment.coerceParentContext(), augmentArg);
        // Do not fail, if this is a uses-augment to an unknown node
        if (targetNode.isPresent() && StmtContextUtils.isUnknownStatement(targetNode.get())) {
            augment.setIsSupportedToBuildEffective(false);
            LOG.warn("Uses-augment to unknown node {}. Augmentation has not been performed. At line: {}",
                augmentArg, augment.sourceReference());
            return;
        }

        super.prerequisiteFailed(failed);
    }
}