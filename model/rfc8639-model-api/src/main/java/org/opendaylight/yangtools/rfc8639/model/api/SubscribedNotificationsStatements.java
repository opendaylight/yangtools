/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * {@link StatementDefinition}s for statements defined by RFC8639.
 */
@NonNullByDefault
public enum SubscribedNotificationsStatements implements StatementDefinition {
    SUBSCRIPTION_STATE_NOTIFICATION("subscription-state-notification") {
        @Override
        public @Nullable ArgumentDefinition argumentDefinition() {
            return null;
        }

        @Override
        public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
            return SubscriptionStateNotificationStatement.class;
        }

        @Override
        public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
            return SubscriptionStateNotificationEffectiveStatement.class;
        }
    };

    private final QName statementName;

    SubscribedNotificationsStatements(final String statementName) {
        this.statementName = QName.create(SubscribedNotificationsConstants.RFC8639_MODULE, statementName).intern();
    }

    @Override
    public final QName statementName() {
        return statementName;
    }
}
