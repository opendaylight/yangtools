/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AugmentEffectiveStatementImpl;

public final class AugmentEffectiveStatementRfc7950Impl extends AugmentEffectiveStatementImpl {
    private final Set<NotificationDefinition> notifications;

    public AugmentEffectiveStatementRfc7950Impl(
            final StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
        super(ctx);
        final Set<NotificationDefinition> notificationsInit = new HashSet<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof NotificationDefinition) {
                notificationsInit.add((NotificationDefinition) effectiveStatement);
            }
        }
        this.notifications = ImmutableSet.copyOf(notificationsInit);
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        return notifications;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AugmentEffectiveStatementRfc7950Impl other = (AugmentEffectiveStatementRfc7950Impl) obj;
        if (!Objects.equals(getTargetPath(), other.getTargetPath())) {
            return false;
        }
        if (!Objects.equals(getWhenCondition(), other.getWhenCondition())) {
            return false;
        }
        if (!getChildNodes().equals(other.getChildNodes())) {
            return false;
        }
        if (!getNotifications().equals(other.getNotifications())) {
            return false;
        }
        return true;
    }
}
