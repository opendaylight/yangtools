/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.DeprecatedAnnotation;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A composition interface {@link AbstractExplicitGenerator}s that contribute getter methods to
 * {@link DataContainerArchetype}.
 *
 * @param <G> the generator class
 */
@NonNullByDefault
sealed interface DataContainerMethod<G extends AbstractExplicitGenerator<?, ?> & DataContainerMethod<G>>
        permits AbstractTypeObjectGenerator, ListGenerator {

    // public interface
    default void addAsGetterMethod(final DataContainerArchetype.Builder builder) {
        addAsGetterMethod(thisMethodGenerator(), builder);
    }

    // private implementation
    private static <G extends AbstractExplicitGenerator<?, ?> & DataContainerMethod<G>> void addAsGetterMethod(
            final G generator, final DataContainerArchetype.Builder builder) {
        if (generator.isAugmenting()) {
            // Do not process augmented nodes: they will be taken care of in their home augmentation
            return;
        }
        if (generator.isAddedByUses()) {
            // If this generator has been added by a uses node, it is already taken care of by the corresponding
            // grouping. There is one exception to this rule: 'type leafref' can use a relative path to point
            // outside of its home grouping. In this case we need to examine the instantiation until we succeed in
            // resolving the reference.
            generator.addAsGetterMethodOverride(builder);
            return;
        }

        final var returnType = generator.methodReturnType();
        generator.constructGetter(builder, returnType);
        generator.constructRequire(builder, returnType);
    }

    /**
     * {@return the implementer's {@code this}}
     */
    G thisMethodGenerator();

    default Type methodReturnType() {
        return thisMethodGenerator().getGeneratedType();
    }

    default void addAsGetterMethodOverride(final DataContainerArchetype.Builder builder) {
        // No-op for most cases
    }

    default void constructRequire(final DataContainerArchetype.Builder builder, final Type returnType) {
        // No-op in most cases
    }

    static void addDeprecatedAnnotation(final MethodSignature.Builder builder,
            final EffectiveStatement<?, ?> statement) {
        if (statement instanceof DocumentedNode.WithStatus withStatus) {
            final var deprecated = DeprecatedAnnotation.ofStatus(withStatus.getStatus());
            if (deprecated != null) {
                builder.addAnnotation(deprecated);
            }
        }
    }
}
