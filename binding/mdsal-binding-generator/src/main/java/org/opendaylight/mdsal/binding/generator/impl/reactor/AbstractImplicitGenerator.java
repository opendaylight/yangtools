/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * An implicit {@link Generator}, not associated with any particular statement.
 */
abstract class AbstractImplicitGenerator extends Generator {
    AbstractImplicitGenerator(final ModuleGenerator parent) {
        super(parent);
    }

    @Override
    final void pushToInference(final SchemaInferenceStack inferenceStack) {
        // No-op
    }

    @Override
    final ClassPlacement classPlacement() {
        return ClassPlacement.TOP_LEVEL;
    }

    @Override
    final Member createMember(final CollisionDomain domain) {
        return domain.addSecondary(this, ((ModuleGenerator) getParent()).getPrefixMember(), classSuffix());
    }

    abstract @NonNull String classSuffix();
}
