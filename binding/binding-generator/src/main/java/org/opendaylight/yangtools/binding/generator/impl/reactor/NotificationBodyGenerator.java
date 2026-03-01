/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.base.VerifyException;
import java.util.List;
import org.opendaylight.yangtools.binding.NotificationBody;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultNotificationBodyRuntimeType;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.NotificationBodyRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A composite generator producing {@link NotificationBody}s for {@code notifications} declared in {@code grouping}s.
 */
final class NotificationBodyGenerator
        extends CompositeSchemaTreeGenerator<NotificationEffectiveStatement, NotificationBodyRuntimeType> {
    NotificationBodyGenerator(final NotificationEffectiveStatement statement, final GroupingGenerator parent) {
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
        final var typeName = typeName();
        final var builder = builderFactory.newGeneratedTypeBuilder(typeName);
        builder.addImplementsType(BindingTypes.notificationBody(TypeRef.of(typeName())));
        narrowImplementedInterface(builder);
        addUsesInterfaces(builder, builderFactory);
        addGetterMethods(builder, builderFactory);

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(currentModule(), statement(), builder);

        return builder.build();
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // Notifications are a distinct concept
    }

    @Override
    CompositeRuntimeTypeBuilder<NotificationEffectiveStatement, NotificationBodyRuntimeType> createBuilder(
            final NotificationEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            NotificationBodyRuntimeType build(final GeneratedType type, final NotificationEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                // uninstantiated: cannot be targeted by 'augment'
                if (augments.isEmpty()) {
                    return new DefaultNotificationBodyRuntimeType(type, statement, children);
                }
                throw new VerifyException("Unexpected augments " + augments);
            }
        };
    }
}
