/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.in;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6897Test extends AbstractYangTest {
    private static final String FOO_NS = "foo";

    @Test
    void notificationsInDataContainersTest() {
        final var context = assertEffectiveModel("/rfc7950/notifications-in-data-nodes/foo.yang");

        assertContainsNotifications(context, "root", "grp-notification", "aug-notification");
        assertContainsNotifications(context, "top-list", "top-list-notification");
        assertContainsNotifications(context, "top", "top-notification");

        final Collection<? extends GroupingDefinition> groupings = context.getGroupings();
        assertEquals(1, groupings.size());
        assertContainsNotifications(groupings.iterator().next(), "grp-notification");

        final Collection<? extends Module> modules = context.getModules();
        assertEquals(1, modules.size());
        final Module foo = modules.iterator().next();
        final Collection<? extends AugmentationSchemaNode> augmentations = foo.getAugmentations();
        assertEquals(1, augmentations.size());
        assertContainsNotifications(augmentations.iterator().next(), "aug-notification", "grp-notification");
    }

    private static void assertContainsNotifications(final SchemaContext schemaContext, final String dataContainerName,
        final String... notificationNames) {
        final DataSchemaNode dataChildByName = schemaContext.getDataChildByName(
            QName.create(FOO_NS, dataContainerName));
        assertThat(dataChildByName, instanceOf(NotificationNodeContainer.class));
        assertContainsNotifications((NotificationNodeContainer) dataChildByName, notificationNames);
    }

    private static void assertContainsNotifications(final NotificationNodeContainer notificationContainer,
        final String... notificationNames) {
        final var notifications = notificationContainer.getNotifications();
        assertEquals(notificationNames.length, notifications.size());

        final var notificationQNames = notifications.stream()
            .map(NotificationDefinition::getQName)
            .collect(Collectors.toUnmodifiableSet());

        for (final String notificationName : notificationNames) {
            assertThat(QName.create(FOO_NS, notificationName), in(notificationQNames));
        }
    }

    @Test
    void invalid10Test() {
        assertInvalidSubstatementException(startsWith("NOTIFICATION is not valid for"),
            "/rfc7950/notifications-in-data-nodes/foo10.yang");
    }

    @Test
    void invalid11Test() {
        assertSourceException(
            startsWith("Notification (foo)grp-notification is defined within an rpc, action, or another notification"),
            "/rfc7950/notifications-in-data-nodes/foo-invalid.yang");
    }

    @Test
    void testNotificationWithinListWithoutKey() {
        assertSourceException(
            startsWith("Notification (bar-namespace?revision=2016-12-08)my-notification is defined within a list that "
                + "has no key statement"),
            "/rfc7950/notifications-in-data-nodes/bar-invalid.yang");
    }

    @Test
    void testNotificationInUsedGroupingWithinCase() {
        assertSourceException(
            startsWith("Notification (baz-namespace?revision=2016-12-08)notification-in-grouping is defined within a "
                + "case statement"),
            "/rfc7950/notifications-in-data-nodes/baz-invalid.yang");
    }
}
