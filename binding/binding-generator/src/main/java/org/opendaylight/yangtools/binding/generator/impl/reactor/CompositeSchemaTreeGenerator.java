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
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Abstract base class for {@link DataContainerGenerator}s which are also {@link SchemaTreeChild}ren.
 */
abstract sealed class CompositeSchemaTreeGenerator<
        S extends SchemaTreeEffectiveStatement<?>,
        R extends CompositeRuntimeType> extends DataContainerGenerator<S, R>
        permits AbstractNotificationGenerator, CaseGenerator, ChoiceGenerator, ContainerGenerator, ListGenerator,
                NotificationBodyGenerator, OperationContainerGenerator, OperationGenerator {
    @NonNullByDefault
    CompositeSchemaTreeGenerator(final S statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    /**
     * {@return the {@link TypeName} to use as the {@code P} parameter of {@link ChildOf}}
     */
    @NonNullByDefault
    final TypeName parentNameForChildOf() {
        var ancestor = getParent();
        while (true) {
            // choice/case hierarchy does not factor into 'ChildOf' hierarchy, hence we need to skip them
            if (ancestor instanceof CaseGenerator || ancestor instanceof ChoiceGenerator) {
                ancestor = ancestor.getParent();
                continue;
            }

            // if we into a choice we need to follow the hierararchy of that choice
            if (ancestor instanceof AugmentGenerator augment
                && augment.targetGenerator() instanceof ChoiceGenerator targetChoice) {
                ancestor = targetChoice;
                continue;
            }

            return ancestor.typeName();
        }
    }
}
