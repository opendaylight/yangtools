/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.Types;

abstract class TypeReference {
    private static final class Identityref extends TypeReference {
        private final List<IdentityGenerator> referencedGenerators;

        private Type returnType;

        Identityref(final List<IdentityGenerator> referencedGenerators) {
            this.referencedGenerators = requireNonNull(referencedGenerators);
        }

        @Override
        Type methodReturnType(final TypeBuilderFactory builderFactory) {
            if (returnType == null) {
                // FIXME: This deals only with RFC6020 semantics. In order to deal with full RFC7950 semantics, we need
                //        to analyze all the types and come up with the lowest-common denominator and use that as the
                //        return type. We also need to encode restrictions, so that builder generator ends up checking
                //        identities being passed -- because the identities may be completely unrelated, in which case
                //        we cannot generate type-safe code.
                returnType = referencedGenerators.stream()
                    .map(gen -> gen.getGeneratedType(builderFactory))
                    .findFirst()
                    .orElseThrow();
            }
            return returnType;
        }
    }

    // Note: this is exposed only for legacy naming handling
    abstract static class Leafref extends TypeReference {
        private Leafref() {
            // Hidden on purpose
        }
    }

    static final class ResolvedLeafref extends Leafref {
        private final AbstractTypeObjectGenerator<?, ?> referencedGenerator;

        private ResolvedLeafref(final AbstractTypeObjectGenerator<?, ?> referencedGenerator) {
            this.referencedGenerator = requireNonNull(referencedGenerator);
        }

        @Override
        Type methodReturnType(final TypeBuilderFactory builderFactory) {
            return referencedGenerator.methodReturnElementType(builderFactory);
        }
    }

    private static final class UnresolvedLeafref extends Leafref {
        static final @NonNull UnresolvedLeafref INSTANCE = new UnresolvedLeafref();

        private UnresolvedLeafref() {
            // Hidden on purpose
        }

        @Override
        Type methodReturnType(final TypeBuilderFactory builderFactory) {
            return Types.objectType();
        }
    }

    static @NonNull TypeReference leafRef(final @Nullable AbstractTypeObjectGenerator<?, ?> referencedGenerator) {
        return referencedGenerator == null ? UnresolvedLeafref.INSTANCE : new ResolvedLeafref(referencedGenerator);
    }

    static @NonNull TypeReference identityRef(final List<IdentityGenerator> referencedGenerators) {
        return new Identityref(referencedGenerators);
    }

    abstract @NonNull Type methodReturnType(@NonNull TypeBuilderFactory builderFactory);
}
