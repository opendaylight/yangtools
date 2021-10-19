/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code notification} statement.
 */
final class NotificationGenerator extends AbstractCompositeGenerator<NotificationEffectiveStatement> {
    NotificationGenerator(final NotificationEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().getIdentifier());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        builder.addImplementsType(BindingTypes.DATA_OBJECT);
        builder.addImplementsType(notificationType(builder, builderFactory));

        addAugmentable(builder);
        addUsesInterfaces(builder, builderFactory);

        addConcreteInterfaceMethods(builder);
        addGetterMethods(builder, builderFactory);

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName());

        addCodegenInformation(module, statement(), builder);
        annotateDeprecatedIfNecessary(builder);

        return builder.build();
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // Notifications are a distinct concept
    }

    private Type notificationType(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        final AbstractCompositeGenerator<?> parent = getParent();
        if (parent instanceof ModuleGenerator) {
            return BindingTypes.notification(builder);
        }

        final Type parentType = Type.of(parent.typeName());
        if (parent instanceof ListGenerator) {
            final KeyGenerator keyGen = ((ListGenerator) parent).keyGenerator();
            if (keyGen != null) {
                return BindingTypes.keyedListNotification(builder, parentType, keyGen.getGeneratedType(builderFactory));
            }
        }
        return BindingTypes.instanceNotification(builder, parentType);
    }
}
