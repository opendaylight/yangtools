/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.parser.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.rfc8639.model.api.SubscribedNotificationsStatements;
import org.opendaylight.yangtools.rfc8639.parser.SubscriptionStateNotificationStatementSupport;
import org.opendaylight.yangtools.rfc8639.parser.inject.InjectRfc8639ParserExtension;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.AbstractParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.osgi.service.component.annotations.Component;

/**
 * Parser support for {@code ietf-subscribed-notifications.yang}.
 *
 * @since 14.0.20
 */
@MetaInfServices
@NonNullByDefault
@Component(service = ParserExtension.class)
public sealed class Rfc8639ParserExtension extends AbstractParserExtension permits InjectRfc8639ParserExtension {
    /**
     * Default constructor.
     */
    public Rfc8639ParserExtension() {
        super(SubscribedNotificationsStatements.SUBSCRIPTION_STATE_NOTIFICATION);
    }

    @Override
    public final StatementSupportBundle configureBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builder()
            .addSupport(new SubscriptionStateNotificationStatementSupport(config))
            .build();
    }
}
