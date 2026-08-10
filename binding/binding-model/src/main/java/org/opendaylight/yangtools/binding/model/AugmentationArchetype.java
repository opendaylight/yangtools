/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.model.impl.AugmentationArchetype00;
import org.opendaylight.yangtools.binding.model.impl.AugmentationArchetype0N;
import org.opendaylight.yangtools.binding.model.impl.AugmentationArchetypeN0;
import org.opendaylight.yangtools.binding.model.impl.AugmentationArchetypeNN;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link Augmentation} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface AugmentationArchetype extends DataContainerArchetype
        permits AugmentationArchetype00, AugmentationArchetype0N, AugmentationArchetypeN0, AugmentationArchetypeNN {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<Augmentation> contract() {
        return Augmentation.class;
    }

    @Override
    AugmentEffectiveStatement statement();

    /**
     * {@return the augmentation target archetype}
     */
    TypeName targetName();

    static AugmentationArchetype of(final TypeName typeName, final AugmentEffectiveStatement statement,
            final TypeName targetName, final List<GroupingArchetype> groupings,
            final List<TypeObjectArchetype<?>> typeObjects, final List<GetterMethod> getters) {
        final var gtrs = TypeMethods.copyList(getters);
        return switch (groupings.size()) {
            case 0 -> of0x(typeName, statement, targetName, gtrs, typeObjects);
            default -> ofNx(typeName, statement, targetName, gtrs, TypeMethods.copyList(groupings), typeObjects);
        };
    }

    private static AugmentationArchetype of0x(final TypeName typeName, final AugmentEffectiveStatement statement,
            final TypeName targetName, final List<GetterMethod> getters,
            final List<TypeObjectArchetype<?>> typeObjects) {
        return switch (typeObjects.size()) {
            case 0 -> new AugmentationArchetype00(typeName, statement, targetName, getters);
            default -> new AugmentationArchetype0N(typeName, statement, targetName, getters,
                TypeMethods.copyList(typeObjects));
        };
    }

    private static AugmentationArchetype ofNx(final TypeName typeName, final AugmentEffectiveStatement statement,
            final TypeName targetName, final List<GetterMethod> getters, final List<Partial> partials,
            final List<TypeObjectArchetype<?>> typeObjects) {
        return switch (typeObjects.size()) {
            case 0 -> new AugmentationArchetypeN0(typeName, statement, targetName, getters, partials);
            default -> new AugmentationArchetypeNN(typeName, statement, targetName, getters, partials,
                TypeMethods.copyList(typeObjects));
        };
    }
}
