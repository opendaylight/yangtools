/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

class NotificationStmtTest extends AbstractYangTest {
    @Test
    void notificationTest() throws Exception {
        final var result = assertEffectiveModel("/model/baz.yang", "/model/bar.yang");

        final var testModule = result.findModules("baz").iterator().next();
        assertNotNull(testModule);

        final var notifications = testModule.getNotifications();
        assertEquals(1, notifications.size());

        final var notification = notifications.iterator().next();
        assertEquals("event", notification.getQName().getLocalName());
        assertEquals(3, notification.getChildNodes().size());

        var leaf = (LeafSchemaNode) notification.getDataChildByName(QName.create(testModule.getQNameModule(),
            "event-class"));
        assertNotNull(leaf);
        leaf = (LeafSchemaNode) notification.getDataChildByName(QName.create(testModule.getQNameModule(), "severity"));
        assertNotNull(leaf);
        final var anyXml = (AnyxmlSchemaNode) notification.getDataChildByName(QName.create(
            testModule.getQNameModule(), "reporting-entity"));
        assertNotNull(anyXml);
    }
}
