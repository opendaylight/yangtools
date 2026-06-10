/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.concepts.Immutable;

class GeneratorResult implements Immutable {
    private static final class Nested extends GeneratorResult {
        Nested(final Archetype generatedType) {
            super(generatedType);
        }

        @Override
        Archetype enclosedType() {
            return generatedType();
        }
    }

    private static final @NonNull GeneratorResult EMPTY = new GeneratorResult();

    private final @Nullable Archetype generatedType;

    private GeneratorResult() {
        generatedType = null;
    }

    private GeneratorResult(final Archetype generatedType) {
        this.generatedType = requireNonNull(generatedType);
    }

    static @NonNull GeneratorResult empty() {
        return EMPTY;
    }

    static @NonNull GeneratorResult member(final Archetype generatedType) {
        return new Nested(generatedType);
    }

    static @NonNull GeneratorResult toplevel(final Archetype generatedType) {
        return new GeneratorResult(generatedType);
    }

    final @Nullable Archetype generatedType() {
        return generatedType;
    }

    @Nullable Archetype enclosedType() {
        return null;
    }
}
