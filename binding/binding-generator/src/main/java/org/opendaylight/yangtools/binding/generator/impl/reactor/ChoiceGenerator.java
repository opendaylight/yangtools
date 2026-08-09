/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultChoiceRuntimeType;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.ChoiceInArchetype;
import org.opendaylight.yangtools.binding.model.GetterAnnotation;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.ReturnType;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code choice} statement.
 */
final class ChoiceGenerator extends CompositeSchemaTreeGenerator<ChoiceEffectiveStatement, ChoiceRuntimeType> {
    static final class ChoiceBuilder extends CompositeRuntimeTypeBuilder<ChoiceEffectiveStatement, ChoiceRuntimeType> {
        private final List<CaseRuntimeType> augmentedCases = new ArrayList<>();

        ChoiceBuilder(final ChoiceEffectiveStatement statement) {
            super(statement);
        }

        @Override
        void processAugment(final AugmentResolver resolver, final AugmentGenerator augment) {
            augment.fillRuntimeCasesIn(resolver, statement(), augmentedCases);
        }

        @Override
        boolean isAugmentedChild(final QName qname) {
            for (var augmented : augmentedCases) {
                if (qname.equals(augmented.statement().argument())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        ChoiceRuntimeType build(final Archetype type, final ChoiceEffectiveStatement statement,
                final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
            if (!(type instanceof ChoiceInArchetype archetype)) {
                throw new VerifyException("Unexpected type " + type);
            }
            if (!augments.isEmpty()) {
                throw new VerifyException("Unexpected augments " + augments);
            }
            children.addAll(augmentedCases);
            return new DefaultChoiceRuntimeType(archetype, children);
        }
    }

    @NonNullByDefault
    ChoiceGenerator(final ChoiceEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.CHOICE;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    ChoiceInArchetype methodReturnType() {
        return getArchetype();
    }

    @Override
    GetterMethod constructGetter(final ReturnType returnType, final Iterator<@NonNull GetterAnnotation> annotations) {
        return constructGetter(statement(), returnType, annotations);
    }

    @NonNullByDefault
    ChoiceInArchetype getArchetype() {
        return (ChoiceInArchetype) getGeneratedType();
    }

    @Override
    ChoiceInArchetype createTypeImpl(final TypeName typeName, final ChoiceEffectiveStatement statement,
            final List<GroupingArchetype> groupings) {
        if (!groupings.isEmpty()) {
            throw new VerifyException("Illegal grouping in " + statement);
        }
        final var cases = new ArrayList<@NonNull CaseObjectArchetype>();
        for (var child : this) {
            if (child instanceof CaseGenerator childCase) {
                cases.add((CaseObjectArchetype) childCase.getOriginal().getGeneratedType());
            }
        }
        return ChoiceInArchetype.of(typeName, statement, getParent().typeName(), cases);
    }

    @Override
    ChoiceBuilder createBuilder(final ChoiceEffectiveStatement statement) {
        return new ChoiceBuilder(statement);
    }
}
