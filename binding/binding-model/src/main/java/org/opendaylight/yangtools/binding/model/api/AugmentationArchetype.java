/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link Augmentation} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface AugmentationArchetype extends DataContainerArchetype permits AugmentationArchetypeImpl {
    @Override
    AugmentEffectiveStatement statement();

    /**
     * {@return the augmentation target archetype}
     */
    AugmentableArchetype target();

    static AugmentationArchetype of(final JavaTypeName typeName, final AugmentEffectiveStatement statement,
            final AugmentableArchetype target, final List<GroupingArchetype> groupings,
            final List<TypeObjectArchetype<?>> typeObjects, final List<GetterMethod> getters) {
        return new AugmentationArchetypeImpl(typeName, statement, target, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters));
    }
}
