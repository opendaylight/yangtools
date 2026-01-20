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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public interface SubscriptionStateNotificationEffectiveStatement
        extends EffectiveStatement<Empty, @NonNull SubscriptionStateNotificationStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return SubscriptionStateNotificationStatement.DEFINITION;
    }
}
