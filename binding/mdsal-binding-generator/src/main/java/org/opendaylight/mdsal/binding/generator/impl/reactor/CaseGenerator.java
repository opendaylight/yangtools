/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import java.util.List;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultCaseRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code case} statement.
 */
final class CaseGenerator extends CompositeSchemaTreeGenerator<CaseEffectiveStatement, CaseRuntimeType> {
    CaseGenerator(final CaseEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
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
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {

        // We also are implementing target choice's type. This is tricky, as we need to cover two distinct cases:
        // - being a child of a choice (i.e. normal definition)
        // - being a child of an augment (i.e. augmented into a choice)
        final AbstractCompositeGenerator<?, ?> parent = getParent();
        final ChoiceGenerator choice;
        if (parent instanceof AbstractAugmentGenerator augGen) {
            final AbstractCompositeGenerator<?, ?> target = augGen.targetGenerator();
            verify(target instanceof ChoiceGenerator, "Unexpected parent augment %s target %s", parent, target);
            choice = (ChoiceGenerator) target;
        } else {
            verify(parent instanceof ChoiceGenerator, "Unexpected parent %s", parent);
            choice = (ChoiceGenerator) parent;
        }

        // Most generators have a parent->child dependency due to parent methods' return types and therefore children
        // must not request parent's type. That is not true for choice->case relationship and hence we do not need to
        // go through DefaultType here
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        // Note: this needs to be the first type we mention as we are relying on that fact for global runtime type
        //       choice/case indexing.
        builder.addImplementsType(choice.getGeneratedType(builderFactory));

        builder.addImplementsType(BindingTypes.DATA_OBJECT);
        addAugmentable(builder);
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName());

        addGetterMethods(builder, builderFactory);

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(module, statement(), builder);
        builder.setModuleName(module.statement().argument().getLocalName());

        return builder.build();
    }

    @Override
    CompositeRuntimeTypeBuilder<CaseEffectiveStatement, CaseRuntimeType> createBuilder(
            final CaseEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            CaseRuntimeType build(final GeneratedType generatedType, final CaseEffectiveStatement statement,
                    final List<RuntimeType> childTypes, final List<AugmentRuntimeType> augmentTypes) {
                return new DefaultCaseRuntimeType(generatedType, statement, childTypes, augmentTypes);
            }
        };
    }
}
