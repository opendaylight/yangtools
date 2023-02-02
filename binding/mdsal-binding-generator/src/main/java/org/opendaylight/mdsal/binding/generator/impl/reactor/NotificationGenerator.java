/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.List;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultNotificationRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code notification} statement.
 */
final class NotificationGenerator
        extends CompositeSchemaTreeGenerator<NotificationEffectiveStatement, NotificationRuntimeType> {
    NotificationGenerator(final NotificationEffectiveStatement statement,
            final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.NOTIFICATION;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
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

        builderFactory.addCodegenInformation(module, statement(), builder);
        annotateDeprecatedIfNecessary(builder);

        return builder.build();
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // Notifications are a distinct concept
    }

    @Override
    CompositeRuntimeTypeBuilder<NotificationEffectiveStatement, NotificationRuntimeType> createBuilder(
            final NotificationEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            NotificationRuntimeType build(final GeneratedType type, final NotificationEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultNotificationRuntimeType(type, statement, children, augments);
            }
        };
    }

    private Type notificationType(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        final AbstractCompositeGenerator<?, ?> parent = getParent();
        if (parent instanceof ModuleGenerator) {
            return BindingTypes.notification(builder);
        }

        final Type parentType = Type.of(parent.typeName());
        if (parent instanceof ListGenerator listGen) {
            final KeyGenerator keyGen = listGen.keyGenerator();
            if (keyGen != null) {
                return BindingTypes.keyedListNotification(builder, parentType, keyGen.getGeneratedType(builderFactory));
            }
        }
        return BindingTypes.instanceNotification(builder, parentType);
    }
}
