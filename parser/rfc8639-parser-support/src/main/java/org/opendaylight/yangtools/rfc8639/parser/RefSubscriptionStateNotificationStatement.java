/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8639.model.api.SubscriptionStateNotificationStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractRefStatement;

@NonNullByDefault
final class RefSubscriptionStateNotificationStatement
        extends AbstractRefStatement<Empty, SubscriptionStateNotificationStatement>
        implements SubscriptionStateNotificationStatement {
    RefSubscriptionStateNotificationStatement(final SubscriptionStateNotificationStatement delegate,
            final DeclarationReference ref) {
        super(delegate, ref);
    }
}
