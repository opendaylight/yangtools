/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A simple {@link Generator} which (potentially) has dependencies on other generators.
 */
abstract class AbstractDependentGenerator<S extends EffectiveStatement<?, ?>, R extends RuntimeType>
        extends AbstractExplicitGenerator<S, R> {
    AbstractDependentGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    /**
     * Discover and link this generator's dependencies.
     *
     * @param context GeneratorContext of this generator
     */
    abstract void linkDependencies(GeneratorContext context);
}
