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
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
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
     * {@return the {@link ChoiceInArchetype} in which this object is a branch}
     */
    ChoiceInArchetype choice();

    static CaseObjectArchetype of(final JavaTypeName typeName, final AugmentEffectiveStatement statement,
            final AugmentableArchetype target, final List<GroupingArchetype> groupings,
            final List<TypeObjectArchetype<?>> typeObjects, final List<MethodSignature> methods) {
        return new CaseObjectArchetypeImpl(typeName, statement, target, TypeMethods.copyList(groupings),
            TypeMethods.copyList(methods), TypeMethods.copyList(typeObjects));
    }
}
