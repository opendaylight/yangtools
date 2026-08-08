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
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.model.api.GetterMethod;
import org.opendaylight.yangtools.binding.model.impl.CaseObjectArchetypeImpl;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for individual cases in a {@link ChoiceInArchetype}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface CaseObjectArchetype extends AugmentableArchetype permits CaseObjectArchetypeImpl {
    @Override
    CaseEffectiveStatement statement();

    /**
     * {@return the name of the {@link ChoiceIn} in which this object is a branch}
     */
    TypeName parentName();

    static CaseObjectArchetype of(final TypeName typeName, final CaseEffectiveStatement statement,
            final TypeName parentName, final List<GroupingArchetype> groupings,
            final List<TypeObjectArchetype<?>> typeObjects, final List<GetterMethod> getters) {
        return new CaseObjectArchetypeImpl(typeName, statement, parentName, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters));
    }
}
