/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

class YinFileUsesStmtTest extends AbstractYinModulesTest {
    @Test
    void testUses() {
        final var testModule = context.findModules("main-impl").iterator().next();

        final var augmentations = testModule.getAugmentations();
        assertEquals(1, augmentations.size());

        final var augmentIterator = augmentations.iterator();
        final var augment = augmentIterator.next();

        final var container = assertInstanceOf(ContainerSchemaNode.class, augment.findDataChildByName(
            QName.create(testModule.getQNameModule(), "main-impl"),
            QName.create(testModule.getQNameModule(), "notification-service")).orElseThrow());

        assertEquals(1, container.getUses().size());
        final var usesNode = container.getUses().iterator().next();
        assertNotNull(usesNode);
        assertEquals("(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)service-ref",
            usesNode.getSourceGrouping().getQName().toString());
        assertEquals(1, usesNode.getRefines().size());
    }
}
