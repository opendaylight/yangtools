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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.NotificationBody;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultNotificationBodyRuntimeType;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.GetterMethod;
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
    @NonNullByDefault
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
    NotificationBodyArchetype createTypeImpl(final TypeName typeName,
            final NotificationEffectiveStatement statement, final List<@NonNull GroupingArchetype> groupings) {
        return NotificationBodyArchetype.of(typeName, statement, groupings, collectTypeObjects(),
            collectGetters());
    }

    @Override
    void addAsGetterMethod(final List<GetterMethod.Builder> list) {
        // Notifications are a distinct concept
    }

    @Override
    CompositeRuntimeTypeBuilder<NotificationEffectiveStatement, NotificationBodyRuntimeType> createBuilder(
            final NotificationEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            NotificationBodyRuntimeType build(final Archetype type, final NotificationEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                // uninstantiated: cannot be targeted by 'augment'
                if (!augments.isEmpty()) {
                    throw new VerifyException("Unexpected augments " + augments);
                }
                return new DefaultNotificationBodyRuntimeType((NotificationBodyArchetype) type, statement, children);
            }
        };
    }
}
