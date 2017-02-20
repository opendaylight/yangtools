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
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class NotificationStmtTest {

    private static final StatementStreamSource NOTIFICATION_MODULE = sourceForResource("/model/baz.yang");
    private static final StatementStreamSource IMPORTED_MODULE = sourceForResource("/model/bar.yang");

    @Test
    public void notificationTest() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(NOTIFICATION_MODULE, IMPORTED_MODULE);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModuleByName("baz", null);
        assertNotNull(testModule);

        final Set<NotificationDefinition> notifications = testModule.getNotifications();
        assertEquals(1, notifications.size());

        final NotificationDefinition notification = notifications.iterator().next();
        assertEquals("event", notification.getQName().getLocalName());
        assertEquals(3, notification.getChildNodes().size());

        LeafSchemaNode leaf = (LeafSchemaNode) notification.getDataChildByName(QName.create(testModule.getQNameModule(), "event-class"));
        assertNotNull(leaf);
        leaf = (LeafSchemaNode) notification.getDataChildByName(QName.create(testModule.getQNameModule(), "severity"));
        assertNotNull(leaf);
        final AnyXmlSchemaNode anyXml = (AnyXmlSchemaNode) notification.getDataChildByName(QName.create(testModule.getQNameModule(), "reporting-entity"));
        assertNotNull(anyXml);
    }
}
