/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8639.model.api.SubscribedNotificationsConstants;
import org.opendaylight.yangtools.rfc8639.model.api.SubscriptionStateNotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

class SubscribedNotificationsTest {
    @Test
    void testSubscribedNotifications() throws Exception {
        final var reactor = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                        new SubscriptionStateNotificationStatementSupport(YangParserConfiguration.DEFAULT))
                .build();

        final var context = reactor.newBuild()
            .addLibSources(
                YangStatementStreamSource.create(new URLYangTextSource(
                    SubscribedNotificationsTest.class.getResource("/ietf-inet-types@2013-07-15.yang"))),
                YangStatementStreamSource.create(new URLYangTextSource(
                    SubscribedNotificationsTest.class.getResource("/ietf-interfaces@2018-02-20.yang"))),
                YangStatementStreamSource.create(new URLYangTextSource(
                    SubscribedNotificationsTest.class.getResource("/ietf-ip@2018-02-22.yang"))),
                YangStatementStreamSource.create(new URLYangTextSource(
                    SubscribedNotificationsTest.class.getResource("/ietf-netconf-acm@2018-02-14.yang"))),
                YangStatementStreamSource.create(new URLYangTextSource(
                    SubscribedNotificationsTest.class.getResource("/ietf-network-instance@2019-01-21.yang"))),
                YangStatementStreamSource.create(new URLYangTextSource(
                    SubscribedNotificationsTest.class.getResource("/ietf-restconf@2017-01-26.yang"))),
                YangStatementStreamSource.create(new URLYangTextSource(
                    SubscribedNotificationsTest.class.getResource("/ietf-yang-schema-mount@2019-01-14.yang"))),
                YangStatementStreamSource.create(new URLYangTextSource(
                    SubscribedNotificationsTest.class.getResource("/ietf-yang-types@2013-07-15.yang"))))
            .addSources(
                YangStatementStreamSource.create(new URLYangTextSource(
                    SubscribedNotificationsTest.class.getResource("/ietf-subscribed-notifications@2019-09-09.yang"))))
            .buildEffective();

        final var notifications = context.getModuleStatement(SubscribedNotificationsConstants.RFC8639_MODULE)
            .streamEffectiveSubstatements(NotificationEffectiveStatement.class)
            .toList();

        assertEquals(7, notifications.size());
        for (var notif : notifications) {
            final var sub = notif.findFirstEffectiveSubstatement(SubscriptionStateNotificationEffectiveStatement.class);
            assertTrue(sub.isPresent(), "No marker in " + notif.argument());
        }
    }
}
