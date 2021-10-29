/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.binding.generator.impl.rt.DerivedChoiceRuntimeType;
import org.opendaylight.mdsal.binding.generator.impl.rt.OriginalChoiceRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code choice} statement.
 */
final class ChoiceGenerator extends CompositeSchemaTreeGenerator<ChoiceEffectiveStatement, ChoiceRuntimeType> {
    ChoiceGenerator(final ChoiceEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.choiceIn(Type.of(getParent().typeName())));

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName());

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(module, statement(), builder);
//      newType.setSchemaPath(schemaNode.getPath());
        builder.setModuleName(module.statement().argument().getLocalName());

        return builder.build();
    }

    @Override
    ChoiceRuntimeType createRuntimeType(final GeneratedType type, final ChoiceEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
        final var original = getOriginal();
        if (!statement.equals(original.statement())) {
            return new DerivedChoiceRuntimeType(type, statement, children, augments,
                original.runtimeType().orElseThrow());
        }

        // Pick up any case statements added by augments which are not reflected in our children. This can happen when
        // a choice is added via uses into two different places and then augmented. Since groupings are reused, validity
        // of such use is not guarded by compile-time checks.
        //
        // Furthermore such case statements can be freely propagated via copy builders and thus can occur in unexpected
        // places. If that happens, though, the two case statements can be equivalent, e.g. by having the exact same
        // shape -- in which case Binding -> DOM translation needs to correct this mishap and play pretend the correct
        // case was used.
        final var augmentedCases = new ArrayList<CaseRuntimeType>();
        for (var augment : original.augments()) {
            for (var gen : augment) {
                if (gen instanceof CaseGenerator) {
                    ((CaseGenerator) gen).runtimeType().ifPresent(augmented -> {
                        for (var child : Iterables.concat(children, augmentedCases)) {
                            if (child instanceof CaseRuntimeType && child.javaType().equals(augmented.javaType())) {
                                return;
                            }
                        }
                        augmentedCases.add(augmented);
                    });
                }
            }
        }

        return new OriginalChoiceRuntimeType(type, statement, children, augments, augmentedCases);
    }
}
