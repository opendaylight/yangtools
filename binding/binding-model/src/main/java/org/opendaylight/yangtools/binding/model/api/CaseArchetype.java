/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for individual cases in a {@link ChoiceInArchetype}.
 *
 * @since 16.0.0
 */
public sealed interface CaseArchetype extends InterfaceArchetype permits CaseArchetypeImpl {
    /**
     * A builder of {@link CaseArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends InterfaceArchetypeBuilder<Builder, CaseEffectiveStatement> {
        private final ChoiceInArchetype choice;

        private Builder(final JavaTypeName typeName, final CaseEffectiveStatement statement,
                final ChoiceInArchetype choice) {
            super(typeName, statement);
            this.choice = requireNonNull(choice);
            // FIXME: do not add these, expose target from toString() and make sure CaseTemplate generates them
            // Note: this needs to be the first type we mention as we are relying on that fact for global runtime type
            //       choice/case indexing.
            addImplementsType(choice);
            addImplementsType(BindingTypes.DATA_OBJECT);
        }

        @Override
        public CaseArchetype build() {
            return new CaseArchetypeImpl(typeName, statement, choice, annotations(), implementsTypes(), constants(),
                methodDefinitions(), enclosedTypes());
        }

        @Override
        Class<CaseArchetype> archetypeClass() {
            return CaseArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final CaseEffectiveStatement statement,
            final ChoiceInArchetype choice) {
        return new Builder(typeName, statement, choice);
    }

    @Override
    CaseEffectiveStatement statement();

    /**
     * {@return the {@link ChoiceInArchetype} in which this object is a branch}
     */
    ChoiceInArchetype choice();
}
