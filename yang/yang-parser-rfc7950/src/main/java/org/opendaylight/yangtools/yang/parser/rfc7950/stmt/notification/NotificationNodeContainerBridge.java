/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Compatibility bridge between {@link NotificationNodeContainer#findNotification(QName)} and
 * {@link SchemaTreeAwareEffectiveStatement}.
 */
@Beta
public interface NotificationNodeContainerBridge<A, D extends DeclaredStatement<A>>
        extends SchemaTreeAwareEffectiveStatement<A, D>, NotificationNodeContainer {

    @Override
    default Optional<NotificationDefinition> findNotification(final QName qname) {
        // 'notification' identifier must never collide with another element, hence if we look it up and it ends up
        // being an NotificationDefinition, we have found a match.
        final SchemaTreeEffectiveStatement<?> child = get(Namespace.class, qname);
        return child instanceof NotificationDefinition ? Optional.of((NotificationDefinition) child) : Optional.empty();
    }
}
