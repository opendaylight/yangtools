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
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultNotificationRuntimeType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * Generator corresponding to a {@code notification} statement in a {@code module} or {@code submodule}, resulting in
 * a {@link Notification}.
 */
final class NotificationGenerator extends AbstractNotificationGenerator<NotificationRuntimeType> {
    @NonNullByDefault
    NotificationGenerator(final NotificationEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
    }

    @Override
    LegacyArchetype<NotificationEffectiveStatement> createTypeImpl(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement) {
        final var builder = LegacyArchetype.builder(typeName, statement)
            .addImplementsType(BindingTypes.DATA_OBJECT)
            .addImplementsType(BindingTypes.notification(TypeRef.of(typeName)));
        addAugmentable(builder);
        addConcreteInterfaceMethods(builder);
        addQNameConstant(builder, localName());
        addUsesInterfaces(builder);
        addGetterMethods(builder);
        return builder.build();
    }

    @Override
    CompositeRuntimeTypeBuilder<NotificationEffectiveStatement, NotificationRuntimeType> createBuilder(
            final NotificationEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            NotificationRuntimeType build(final Archetype type, final NotificationEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultNotificationRuntimeType(type, statement, children, augments);
            }
        };
    }
}
