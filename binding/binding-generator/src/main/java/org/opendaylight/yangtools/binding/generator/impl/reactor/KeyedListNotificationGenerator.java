/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * A {@link NotificationGenerator} generating {@link KeyedListNotification}s.
 */
final class KeyedListNotificationGenerator extends AbstractNotificationGenerator {
    private final KeyGenerator keyGen;

    KeyedListNotificationGenerator(final NotificationEffectiveStatement statement,
            final ListGenerator parent, final KeyGenerator keyGen) {
        super(statement, parent);
        this.keyGen = requireNonNull(keyGen);
    }

    @Override
    ParameterizedType notificationType(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        return BindingTypes.keyedListNotification(builder.typeRef(), TypeRef.of(getParent().typeName()),
            keyGen.getGeneratedType(builderFactory));
    }
}
