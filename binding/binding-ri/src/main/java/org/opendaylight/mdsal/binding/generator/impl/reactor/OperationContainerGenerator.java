/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to an {@code input} or an {@code output} statement.
 */
abstract sealed class OperationContainerGenerator<S extends DataTreeEffectiveStatement<?>,
            R extends CompositeRuntimeType> extends CompositeSchemaTreeGenerator<S, R>
        permits InputGenerator, OutputGenerator {
    private final ConcreteType baseInterface;

    OperationContainerGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent,
            final ConcreteType baseInterface) {
        super(statement, parent);
        this.baseInterface = requireNonNull(baseInterface);
    }

    @Override
    final CollisionDomain parentDomain() {
        return getParent().parentDomain();
    }

    @Override
    final AbstractCompositeGenerator<?, ?> getPackageParent() {
        return getParent().getParent();
    }

    @Override
    final Member createMember(final CollisionDomain domain) {
        return createMember(domain, getParent().ensureMember());
    }

    abstract @NonNull Member createMember(@NonNull CollisionDomain domain, Member parent);

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final AbstractCompositeGenerator<?, ?> parent = getParent();
        if (parent instanceof ActionGenerator actionParent && actionParent.isAddedByUses()) {
            //        final ActionDefinition orig = findOrigAction(parentSchema, action).get();
            //        // Original definition may live in a different module, make sure we account for that
            //        final ModuleContext origContext = moduleContext(
            //            orig.getPath().getPathFromRoot().iterator().next().getModule());
            //        input = context.addAliasType(origContext, orig.getInput(), action.getInput());
            //        output = context.addAliasType(origContext, orig.getOutput(), action.getOutput());

            throw new UnsupportedOperationException("Lookup in original");
        }

        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(baseInterface);
        addAugmentable(builder);

        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);
        addGetterMethods(builder, builderFactory);

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName());

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(module, statement(), builder);
//                builder.setSchemaPath(schemaNode.getPath());
        builder.setModuleName(module.statement().argument().getLocalName());

        return builder.build();
    }
}
