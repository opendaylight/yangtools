/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.ArrayDeque;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;

/**
 * Utility to resolve instantiated {@code augment} statements to their {@link AugmentGenerator} counterparts.
 * This is essentially a stack of {@link DataContainerGenerator}s which should be examined.
 */
final class AugmentResolver implements Mutable {
    private final ArrayDeque<CompositeGenerator<?, ?>> stack = new ArrayDeque<>();

    void enter(final DataContainerGenerator<?, ?> generator) {
        stack.push(generator);
    }

    void exit() {
        stack.pop();
    }

    @NonNull AugmentGenerator getAugment(final AugmentEffectiveStatement statement) {
        for (var generator : stack) {
            final var found = generator.findAugmentByStatement(statement);
            if (found != null) {
                return found;
            }
        }
        throw new IllegalStateException("Failed to resolve " + statement + " in " + stack);
    }
}
