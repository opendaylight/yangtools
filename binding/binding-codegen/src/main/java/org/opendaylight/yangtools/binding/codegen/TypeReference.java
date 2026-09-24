/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * A {@link BlockFragment} emitting a reference to a type, potentially parameterized with some types.
 */
@NonNullByDefault
sealed interface TypeReference extends BlockFragment permits ParameterizedTypeReference, RawTypeReference {

    static TypeReference of(final GeneratedClass inClass, final TypeName type) {
        return new RawTypeReference(inClass, type);
    }

    static TypeReference of(final GeneratedClass inClass, final TypeName type, final TypeName arg0) {
        return new ParameterizedTypeReference(inClass, type, List.of(arg0));
    }

    static TypeReference of(final GeneratedClass inClass, final TypeName type, final Archetype arg0) {
        return of(inClass, type, arg0.name());
    }

    static TypeReference of(final GeneratedClass inClass, final TypeName type, final TypeName arg0,
            final TypeName arg1) {
        return new ParameterizedTypeReference(inClass, type, List.of(arg0, arg1));
    }

    static TypeReference of(final GeneratedClass inClass, final TypeName type, final TypeName arg0,
            final Archetype arg1) {
        return of(inClass, type, arg0, arg1.name());
    }

    static TypeReference of(final GeneratedClass inClass, final TypeName type, final TypeName... args) {
        return new ParameterizedTypeReference(inClass, type, List.of(args));
    }

    static TypeReference ofDiamond(final GeneratedClass inClass, final TypeName type) {
        return new ParameterizedTypeReference(inClass, type, List.of());
    }
}
