/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Abstract base class for {@link AbstractCompositeGenerator}s which are also {@link SchemaTreeChild}ren.
 */
abstract class CompositeSchemaTreeGenerator<S extends SchemaTreeEffectiveStatement<?>, R extends CompositeRuntimeType>
        extends AbstractCompositeGenerator<S, R> {
    @NonNullByDefault
    CompositeSchemaTreeGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    /**
     * {@return the {@link JavaTypeName} to use as the {@code P} parameter of {@link ChildOf}}
     */
    @NonNullByDefault
    final JavaTypeName parentNameForChildOf() {
        var ancestor = getParent();
        while (true) {
            // choice/case hierarchy does not factor into 'ChildOf' hierarchy, hence we need to skip them
            if (ancestor instanceof CaseGenerator || ancestor instanceof ChoiceGenerator) {
                ancestor = ancestor.getParent();
                continue;
            }

            // if we into a choice we need to follow the hierararchy of that choice
            if (ancestor instanceof AbstractAugmentGenerator augment
                && augment.targetGenerator() instanceof ChoiceGenerator targetChoice) {
                ancestor = targetChoice;
                continue;
            }

            return ancestor.typeName();
        }
    }
}
