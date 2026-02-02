/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * The declared representation of {@code subscription-state-notification} extension.
 */
public interface SubscriptionStateNotificationStatement extends DeclaredStatement<Empty> {
    /**
     * The definition of {@code sn:subscription-state-notification} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Empty, @NonNull SubscriptionStateNotificationStatement,
        @NonNull SubscriptionStateNotificationEffectiveStatement> DEF = StatementDefinition.of(
            SubscriptionStateNotificationStatement.class, SubscriptionStateNotificationEffectiveStatement.class,
            SubscribedNotificationsConstants.RFC8639_MODULE, "subscription-state-notification");

    @Override
    default StatementDefinition<Empty, ?, ?> statementDefinition() {
        return DEF;
    }
}
