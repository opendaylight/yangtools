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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.Submodule;

class Bug3799Test extends AbstractYangTest {
    @Test
    void test() {
        final var schema = assertEffectiveModelDir("/bugs/bug3799");

        var modules = schema.getModules();
        assertNotNull(modules);
        assertEquals(1, modules.size());

        Module testModule = modules.iterator().next();
        var subModules = testModule.getSubmodules();
        assertNotNull(subModules);
        assertEquals(1, subModules.size());

        Submodule testSubmodule = subModules.iterator().next();

        var notifications = testSubmodule.getNotifications();
        assertNotNull(notifications);
        assertEquals(1, notifications.size());

        NotificationDefinition bazNotification = notifications.iterator().next();
        var childNodes = bazNotification.getChildNodes();
        assertNotNull(childNodes);
        assertEquals(1, childNodes.size());

        DataSchemaNode child = childNodes.iterator().next();
        assertTrue(child instanceof LeafSchemaNode);

        LeafSchemaNode leafBar = (LeafSchemaNode) child;
        String bar = leafBar.getQName().getLocalName();
        assertEquals("bar", bar);
    }
}
