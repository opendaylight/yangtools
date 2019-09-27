/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class NotificationStmtTest {

    @Test
    public void notificationTest() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(sourceForResource("/model/baz.yang"), sourceForResource("/model/bar.yang"))
                .buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModules("baz").iterator().next();
        assertNotNull(testModule);

        final Set<NotificationDefinition> notifications = testModule.getNotifications();
        assertEquals(1, notifications.size());

        final NotificationDefinition notification = notifications.iterator().next();
        assertEquals("event", notification.getQName().getLocalName());
        assertEquals(3, notification.getChildNodes().size());

        LeafSchemaNode leaf = (LeafSchemaNode) notification.getDataChildByName(QName.create(testModule.getQNameModule(),
                "event-class"));
        assertNotNull(leaf);
        leaf = (LeafSchemaNode) notification.getDataChildByName(QName.create(testModule.getQNameModule(), "severity"));
        assertNotNull(leaf);
        final AnyxmlSchemaNode anyXml = (AnyxmlSchemaNode) notification.getDataChildByName(QName.create(
            testModule.getQNameModule(), "reporting-entity"));
        assertNotNull(anyXml);
    }
}
