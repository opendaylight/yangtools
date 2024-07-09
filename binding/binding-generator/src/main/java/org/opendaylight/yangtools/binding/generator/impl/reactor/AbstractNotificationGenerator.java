/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultNotificationRuntimeType;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Abstract base generator corresponding to a {@code notification} statement.
 */
abstract class AbstractNotificationGenerator
        extends CompositeSchemaTreeGenerator<NotificationEffectiveStatement, NotificationRuntimeType> {
    AbstractNotificationGenerator(final NotificationEffectiveStatement statement,
            final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    static final AbstractNotificationGenerator of(final NotificationEffectiveStatement statement,
            final AbstractCompositeGenerator<?, ?> parent) {
        return switch (parent) {
            case ModuleGenerator module -> new NotificationGenerator(statement, module);
            case ListGenerator listGen ->{
                final var keyGen = listGen.keyGenerator();
                yield keyGen != null ? new KeyedListNotificationGenerator(statement, listGen, keyGen)
                    : new InstanceNotificationGenerator(statement, parent);
            }
            default -> new InstanceNotificationGenerator(statement, parent);
        };
    }

    @Override
    final StatementNamespace namespace() {
        return StatementNamespace.NOTIFICATION;
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newGeneratedTypeBuilder(typeName());

        builder.addImplementsType(BindingTypes.DATA_OBJECT);
        builder.addImplementsType(notificationType(builder, builderFactory));

        addAugmentable(builder);
        addUsesInterfaces(builder, builderFactory);

        addConcreteInterfaceMethods(builder);
        addGetterMethods(builder, builderFactory);

        final var module = currentModule();
        module.addQNameConstant(builder, localName());

        builderFactory.addCodegenInformation(module, statement(), builder);
        annotateDeprecatedIfNecessary(builder);

        return builder.build();
    }

    @Override
    final void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // Notifications are a distinct concept
    }

    @Override
    final CompositeRuntimeTypeBuilder<NotificationEffectiveStatement, NotificationRuntimeType> createBuilder(
            final NotificationEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            NotificationRuntimeType build(final GeneratedType type, final NotificationEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultNotificationRuntimeType(type, statement, children, augments);
            }
        };
    }

    abstract Type notificationType(GeneratedTypeBuilder builder, TypeBuilderFactory builderFactory);
}
