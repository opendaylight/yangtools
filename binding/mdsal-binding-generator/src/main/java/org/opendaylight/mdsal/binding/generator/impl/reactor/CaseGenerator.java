/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code case} statement.
 */
final class CaseGenerator extends AbstractCompositeGenerator<CaseEffectiveStatement> {
    CaseGenerator(final CaseEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.DATA_OBJECT);

        // We also are implementing target choice's type. This is tricky, as we need to cover two distinct cases:
        // - being a child of a choice (i.e. normal definition)
        // - being a child of an augment (i.e. augmented into a choice)
        final AbstractCompositeGenerator<?> parent = getParent();
        final ChoiceGenerator choice;
        if (parent instanceof AbstractAugmentGenerator) {
            final AbstractCompositeGenerator<?> target = ((AbstractAugmentGenerator) parent).targetGenerator();
            verify(target instanceof ChoiceGenerator, "Unexpected parent augment %s target %s", parent, target);
            choice = (ChoiceGenerator) target;
        } else {
            verify(parent instanceof ChoiceGenerator, "Unexpected parent %s", parent);
            choice = (ChoiceGenerator) parent;
        }

        // Most generators have a parent->child dependency due to parent methods' return types and therefore children
        // must not request parent's type. That is not true for choice->case relationship and hence we do not need to
        // go through DefaultType here.
        builder.addImplementsType(choice.getGeneratedType(builderFactory));
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
}
