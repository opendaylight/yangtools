/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

class YinFileUsesStmtTest extends AbstractYinModulesTest {
    @Test
    void testUses() {
        final Module testModule = context.findModules("main-impl").iterator().next();

        final Collection<? extends AugmentationSchemaNode> augmentations = testModule.getAugmentations();
        assertEquals(1, augmentations.size());

        final Iterator<? extends AugmentationSchemaNode> augmentIterator = augmentations.iterator();
        final AugmentationSchemaNode augment = augmentIterator.next();

        final ContainerSchemaNode container = (ContainerSchemaNode) augment.findDataChildByName(
            QName.create(testModule.getQNameModule(), "main-impl"),
            QName.create(testModule.getQNameModule(), "notification-service")).get();

        assertEquals(1, container.getUses().size());
        final UsesNode usesNode = container.getUses().iterator().next();
        assertNotNull(usesNode);
        assertEquals("(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)service-ref",
            usesNode.getSourceGrouping().getQName().toString());
        assertEquals(1, usesNode.getRefines().size());
    }
}
