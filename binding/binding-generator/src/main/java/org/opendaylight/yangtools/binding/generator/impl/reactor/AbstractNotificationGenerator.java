/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultNotificationRuntimeType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
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
    @NonNullByDefault
    AbstractNotificationGenerator(final NotificationEffectiveStatement statement,
            final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
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
    ClassPlacement classPlacement() {
        return ClassPlacement.TOP_LEVEL;
    }

    @Override
    final LegacyArchetype<NotificationEffectiveStatement> createTypeImpl() {
        final var statement = statement();
        final var builder = new CodegenGeneratedTypeBuilder<>(typeName(), statement);
        builder.addImplementsType(BindingTypes.DATA_OBJECT);
        builder.addImplementsType(notificationType(builder.typeRef()));

        final var orig = getOriginal();
        if (equals(orig)) {
            addUsesInterfaces(builder);
            addGetterMethods(builder);
        } else {
            builder.addImplementsType(orig.getGeneratedType());
        }

        addAugmentable(builder);
        addConcreteInterfaceMethods(builder);

        addQNameConstant(builder, localName());

        annotateDeprecatedIfNecessary(builder);

        return builder.build();
    }

    @Override
    final void addAsGetterMethod(final LegacyArchetype.Builder<?> builder) {
        // Notifications are a distinct concept
    }

    @Override
    final CompositeRuntimeTypeBuilder<NotificationEffectiveStatement, NotificationRuntimeType> createBuilder(
            final NotificationEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            NotificationRuntimeType build(final Archetype type, final NotificationEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultNotificationRuntimeType(type, statement, children, augments);
            }
        };
    }

    @NonNullByDefault
    abstract Type notificationType(TypeRef self);
}
