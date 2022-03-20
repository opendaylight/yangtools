/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;

/**
 * Generator corresponding to a {@code augment} statement used as a child of a {@code module} statement.
 */
final class ModuleAugmentGenerator extends AbstractAugmentGenerator {
    ModuleAugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    TargetAugmentEffectiveStatement effectiveIn(final SchemaTreeAwareEffectiveStatement<?, ?> target) {
        return effectiveIn(target, Function.identity());
    }

    @NonNull AugmentRequirement startLinkage(final GeneratorContext context) {
        return new AugmentRequirement(this,
            context.resolveModule(statement().argument().firstNodeIdentifier().getModule()));
    }
}
