/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.base.VerifyException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultCaseRuntimeType;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code case} statement.
 */
final class CaseGenerator extends DataContainerGenerator<CaseEffectiveStatement, CaseRuntimeType> {
    @NonNullByDefault
    CaseGenerator(final CaseEffectiveStatement statement, final CompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.CASE;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    CaseObjectArchetype createTypeImpl(final TypeName typeName, final CaseEffectiveStatement statement,
            final List<@NonNull GroupingArchetype> groupings) {
        // We also are implementing target choice's type. This is tricky, as we need to cover two distinct cases:
        // - being a child of a choice (i.e. normal definition)
        // - being a child of an augment (i.e. augmented into a choice)
        final var parent = getParent();
        final var choice = switch (parent) {
            case AugmentGenerator augmentGen -> {
                final var target = augmentGen.targetGenerator();
                if (!(target instanceof ChoiceGenerator targetChoice)) {
                    throw new VerifyException("Unexpected parent augment " + parent + " target " + target);
                }
                yield targetChoice;
            }
            case ChoiceGenerator choiceGen -> choiceGen;
            default -> throw new VerifyException("Unexpected parent " + parent);
        };

        // Most generators have a parent->child dependency due to parent methods' return types and therefore children
        // must not request parent's type. That is not true for choice->case relationship and hence we do not need to
        // go through DefaultType here
        return CaseObjectArchetype.of(typeName, statement, choice.getParent().typeName(), choice.typeName(), groupings,
            collectTypeObjects(), collectGetters());
    }

    @Override
    CompositeRuntimeTypeBuilder<CaseEffectiveStatement, CaseRuntimeType> createBuilder(
            final CaseEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            CaseRuntimeType build(final Archetype type, final CaseEffectiveStatement statement,
                    final List<RuntimeType> childTypes, final List<AugmentRuntimeType> augmentTypes) {
                return new DefaultCaseRuntimeType((CaseObjectArchetype) type, statement, childTypes, augmentTypes);
            }
        };
    }
}
