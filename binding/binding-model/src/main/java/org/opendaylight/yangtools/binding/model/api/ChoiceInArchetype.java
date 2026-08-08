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
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.impl.ChoiceInArchetype0;
import org.opendaylight.yangtools.binding.model.impl.ChoiceInArchetype1;
import org.opendaylight.yangtools.binding.model.impl.ChoiceInArchetypeN;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

/**
 * An {@link Archetype} for a {@link ChoiceIn} generated for an {@link ChoiceEffectiveStatement}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface ChoiceInArchetype extends Archetype
        permits ChoiceInArchetype0, ChoiceInArchetype1, ChoiceInArchetypeN {
    @Override
    ChoiceEffectiveStatement statement();

    /**
     * {@return the parent name}
     */
    TypeName parentName();

    /**
     * {@return possible {@link CaseObjectArchetype}s}
     */
    List<CaseObjectArchetype> cases();

    static ChoiceInArchetype of(final TypeName name, final ChoiceEffectiveStatement statement,
            final TypeName parentName, final List<CaseObjectArchetype> cases) {
        return switch (cases.size()) {
            case 0 -> new ChoiceInArchetype0(name, statement, parentName);
            case 1 -> new ChoiceInArchetype1(name, statement, parentName, cases.getFirst());
            default -> new ChoiceInArchetypeN(name, statement, parentName, List.copyOf(cases));
        };
    }
}
