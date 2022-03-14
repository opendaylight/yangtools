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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultActionRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code action} statement.
 */
final class ActionGenerator extends CompositeSchemaTreeGenerator<ActionEffectiveStatement, ActionRuntimeType> {
    ActionGenerator(final ActionEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().getIdentifier());
    }

    @Override
    ClassPlacement classPlacement() {
        // We do not generate Actions for groupings as they are inexact, and do not capture an actual instantiation --
        // therefore they do not have an InstanceIdentifier. We still need to allocate a package name for the purposes
        // of generating shared classes for input/output
        return getParent() instanceof GroupingGenerator ? ClassPlacement.PHANTOM : ClassPlacement.TOP_LEVEL;
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(implementedType(builderFactory));

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, statement().argument());

//        addGetterMethods(builder, builderFactory);

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(module, statement(), builder);

        return builder.build();
    }

    private @NonNull Type implementedType(final TypeBuilderFactory builderFactory) {
        final GeneratedType input = getChild(this, InputEffectiveStatement.class).getOriginal()
            .getGeneratedType(builderFactory);
        final GeneratedType output = getChild(this, OutputEffectiveStatement.class).getOriginal()
            .getGeneratedType(builderFactory);

        final AbstractCompositeGenerator<?, ?> parent = getParent();
        if (parent instanceof ListGenerator) {
            final KeyGenerator keyGen = ((ListGenerator) parent).keyGenerator();
            if (keyGen != null) {
                return BindingTypes.keyedListAction(Type.of(parent.typeName()), keyGen.getGeneratedType(builderFactory),
                    input, output);
            }
        }

        return BindingTypes.action(Type.of(parent.typeName()), input, output);
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // actions are a separate concept
    }

    @Override
    CompositeRuntimeTypeBuilder<ActionEffectiveStatement, ActionRuntimeType> createBuilder(
            final ActionEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ActionRuntimeType build(final GeneratedType generatedType, final ActionEffectiveStatement statement,
                    final List<RuntimeType> childTypes, final List<AugmentRuntimeType> augmentTypes) {
                verify(augmentTypes.isEmpty(), "Unexpected augments %s", augmentTypes);
                return new DefaultActionRuntimeType(generatedType, statement, childTypes);
            }
        };
    }
}
