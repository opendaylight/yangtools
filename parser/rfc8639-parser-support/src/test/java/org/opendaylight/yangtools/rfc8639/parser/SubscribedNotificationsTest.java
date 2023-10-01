/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc8639.model.api.SubscribedNotificationsConstants;
import org.opendaylight.yangtools.rfc8639.model.api.SubscriptionStateNotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class SubscribedNotificationsTest {
    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new SubscriptionStateNotificationStatementSupport(YangParserConfiguration.DEFAULT))
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void testSubscribedNotifications() throws ReactorException, IOException, YangSyntaxErrorException {
        final var context = reactor.newBuild()
            .addLibSources(
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    SubscribedNotificationsTest.class, "/ietf-inet-types@2013-07-15.yang")),
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    SubscribedNotificationsTest.class, "/ietf-interfaces@2018-02-20.yang")),
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    SubscribedNotificationsTest.class, "/ietf-ip@2018-02-22.yang")),
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    SubscribedNotificationsTest.class, "/ietf-netconf-acm@2018-02-14.yang")),
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    SubscribedNotificationsTest.class, "/ietf-network-instance@2019-01-21.yang")),
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    SubscribedNotificationsTest.class, "/ietf-restconf@2017-01-26.yang")),
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    SubscribedNotificationsTest.class, "/ietf-yang-schema-mount@2019-01-14.yang")),
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    SubscribedNotificationsTest.class, "/ietf-yang-types@2013-07-15.yang")))
            .addSources(
                YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                    SubscribedNotificationsTest.class, "/ietf-subscribed-notifications@2019-09-09.yang")))
            .buildEffective();

        final var notifications = context.getModuleStatement(SubscribedNotificationsConstants.RFC8639_MODULE)
            .streamEffectiveSubstatements(NotificationEffectiveStatement.class)
            .collect(Collectors.toUnmodifiableList());

        assertEquals(7, notifications.size());
        for (NotificationEffectiveStatement notif : notifications) {
            final var sub = notif.findFirstEffectiveSubstatement(SubscriptionStateNotificationEffectiveStatement.class);
            assertTrue("No marker in " + notif.argument(), sub.isPresent());
        }
    }
}
