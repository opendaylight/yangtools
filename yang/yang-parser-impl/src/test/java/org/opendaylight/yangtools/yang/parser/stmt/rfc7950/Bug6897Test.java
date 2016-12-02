/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6897Test {
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void notificationsInDataContainersTest() throws ReactorException, SourceException, FileNotFoundException,
            URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils
                .parseYangSource("/rfc7950/notifications-in-data-nodes/foo.yang");
        assertNotNull(schemaContext);

        assertContainsNotifications(schemaContext, "root", "grp-notification", "aug-notification");
        assertContainsNotifications(schemaContext, "top-list", "top-list-notification");
        assertContainsNotifications(schemaContext, "top", "top-notification");

        final Set<GroupingDefinition> groupings = schemaContext.getGroupings();
        assertEquals(1, groupings.size());
        assertContainsNotifications(groupings.iterator().next(), "grp-notification");

        final Set<Module> modules = schemaContext.getModules();
        assertEquals(1, modules.size());
        final Module foo = modules.iterator().next();
        final Set<AugmentationSchema> augmentations = foo.getAugmentations();
        assertEquals(1, augmentations.size());
        assertContainsNotifications(augmentations.iterator().next(), "aug-notification", "grp-notification");
    }

    private static void assertContainsNotifications(final SchemaContext schemaContext, final String dataContainerName,
            final String... notificationNames) {
        final DataSchemaNode dataChildByName = schemaContext.getDataChildByName(QName.create(FOO_NS, FOO_REV,
                dataContainerName));
        assertTrue(dataChildByName instanceof NotificationNodeContainer);
        assertContainsNotifications((NotificationNodeContainer) dataChildByName, notificationNames);
    }

    private static void assertContainsNotifications(final NotificationNodeContainer notificationContainer,
            final String... notificationNames) {
        final Set<NotificationDefinition> notifications = notificationContainer.getNotifications();
        assertEquals(notificationNames.length, notifications.size());

        final Set<QName> notificationQNames = new HashSet<>();
        notifications.forEach(n -> notificationQNames.add(n.getQName()));

        for (final String notificationName : notificationNames) {
            assertTrue(notificationQNames.contains(QName.create(FOO_NS, FOO_REV, notificationName)));
        }
    }

    @Test
    public void invalid10Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/notifications-in-data-nodes/foo10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith("NOTIFICATION is not valid for"));
        }
    }

    @Test
    public void invalid11Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/notifications-in-data-nodes/foo-invalid.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e
                    .getCause()
                    .getMessage()
                    .startsWith(
                            "Notification (foo?revision=1970-01-01)grp-notification is defined within"
                                    + " an rpc, action, or another notification"));
        }
    }
}
