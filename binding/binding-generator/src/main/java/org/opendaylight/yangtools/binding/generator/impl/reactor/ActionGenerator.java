/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultActionRuntimeType;
import org.opendaylight.yangtools.binding.model.ActionArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * Generator corresponding to a {@code action} statement.
 */
final class ActionGenerator extends AbstractInvokableGenerator<ActionEffectiveStatement, ActionRuntimeType> {
    ActionGenerator(final ActionEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.ACTION;
    }

    @Override
    ClassPlacement classPlacement() {
        // We do not generate Actions for groupings as they are inexact, and do not capture an actual instantiation --
        // therefore they do not have an InstanceIdentifier. We still need to allocate a package name for the purposes
        // of generating shared classes for input/output
        return getParent() instanceof GroupingGenerator ? ClassPlacement.PHANTOM : ClassPlacement.TOP_LEVEL;
    }

    @Override
    ActionArchetype newArchetype() {
        return new ActionArchetype(typeName());
    }

    @Override
    void addImplementedType(final TypeBuilderFactory builderFactory, final GeneratedTypeBuilder builder,
            final GeneratedType input, final GeneratedType output) {
        final var parent = getParent();
        final var parentType = Type.of(parent.typeName());
        if (parent instanceof ListGenerator list) {
            final var keyGen = list.keyGenerator();
            if (keyGen != null) {
                final var keyType = keyGen.getGeneratedType(builderFactory);
                builder.addImplementsType(BindingTypes.keyedListAction(parentType, keyType, input, output));
                builder.addMethod(Naming.ACTION_INVOKE_NAME).setAbstract(true)
                    .addParameter(BindingTypes.objectIdentifierWithKey(parentType, keyType), "path")
                    .addParameter(input, "input")
                    .setReturnType(Types.listenableFutureTypeFor(BindingTypes.rpcResult(output)))
                    .addAnnotation(OVERRIDE_ANNOTATION);
                return;
            }
        }
        builder.addImplementsType(BindingTypes.action(parentType, input, output));
        builder.addMethod(Naming.ACTION_INVOKE_NAME).setAbstract(true)
            .addParameter(BindingTypes.objectIdentifier(parentType), "path")
            .addParameter(input, "input")
            .setReturnType(Types.listenableFutureTypeFor(BindingTypes.rpcResult(output)))
            .addAnnotation(OVERRIDE_ANNOTATION);
    }

    @Override
    CompositeRuntimeTypeBuilder<ActionEffectiveStatement, ActionRuntimeType> createBuilder(
            final ActionEffectiveStatement statement) {
        return new InvokableRuntimeTypeBuilder<>(statement) {
            @Override
            ActionRuntimeType build(final GeneratedType generatedType, final ActionEffectiveStatement statement,
                    final List<RuntimeType> childTypes) {
                return new DefaultActionRuntimeType(generatedType, statement, childTypes);
            }
        };
    }
}
