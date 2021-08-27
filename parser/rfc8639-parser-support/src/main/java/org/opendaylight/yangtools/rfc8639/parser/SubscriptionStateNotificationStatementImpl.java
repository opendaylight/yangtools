/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8639.model.api.SubscriptionStateNotificationStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithoutArgument.WithSubstatements;

final class SubscriptionStateNotificationStatementImpl extends WithSubstatements
        implements SubscriptionStateNotificationStatement {
    SubscriptionStateNotificationStatementImpl(final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(substatements);
    }
}