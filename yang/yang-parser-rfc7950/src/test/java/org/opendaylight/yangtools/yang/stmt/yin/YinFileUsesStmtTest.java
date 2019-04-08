/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

public class YinFileUsesStmtTest extends AbstractYinModulesTest {

    @Test
    public void testUses() {
        final Module testModule = TestUtils.findModule(context, "main-impl").get();

        final Set<AugmentationSchemaNode> augmentations = testModule.getAugmentations();
        assertEquals(1, augmentations.size());

        final Iterator<AugmentationSchemaNode> augmentIterator = augmentations.iterator();
        final AugmentationSchemaNode augment = augmentIterator.next();

        final ContainerSchemaNode container = (ContainerSchemaNode) augment.findDataChildByName(
            QName.create(testModule.getQNameModule(), "main-impl"),
            QName.create(testModule.getQNameModule(), "notification-service")).get();

        assertEquals(1, container.getUses().size());
        final UsesNode usesNode = container.getUses().iterator().next();
        assertNotNull(usesNode);
        assertThat(usesNode.getGroupingPath().toString(), containsString(
            "[(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)service-ref]"));
        assertEquals(1, usesNode.getRefines().size());
    }
}
