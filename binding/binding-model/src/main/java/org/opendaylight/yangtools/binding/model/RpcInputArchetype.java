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
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.model.impl.RpcInputArchetypeImpl;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link RpcInput} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface RpcInputArchetype extends AugmentableArchetype permits RpcInputArchetypeImpl {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<RpcInput> contract() {
        return RpcInput.class;
    }

    @Override
    InputEffectiveStatement statement();

    static RpcInputArchetype of(final TypeName typeName, final InputEffectiveStatement statement,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<GetterMethod> getters, final List<AugmentationArchetype> augmentations) {
        return new RpcInputArchetypeImpl(typeName, statement, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters), TypeMethods.copyList(augmentations));
    }
}
