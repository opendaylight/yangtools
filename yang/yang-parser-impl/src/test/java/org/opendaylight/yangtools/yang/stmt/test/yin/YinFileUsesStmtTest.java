/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinFileUsesStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, modules.size());
    }

    @Test
    public void testUses() {
        Module testModule = TestUtils.findModule(modules, "main-impl");
        assertNotNull(testModule);

        Set<AugmentationSchema> augmentations = testModule.getAugmentations();
        assertEquals(1, augmentations.size());

        Iterator<AugmentationSchema> augmentIterator = augmentations.iterator();
        AugmentationSchema augment = augmentIterator.next();

        ChoiceCaseNode caseNode = (ChoiceCaseNode) augment.getDataChildByName("main-impl");
        assertNotNull(caseNode);

        ContainerSchemaNode container = (ContainerSchemaNode) caseNode.getDataChildByName("notification-service");
        assertNotNull(container);

        assertEquals(1, container.getUses().size());
        UsesNode usesNode = container.getUses().iterator().next();
        assertNotNull(usesNode);
        assertTrue(usesNode.getGroupingPath().toString().contains("[(urn:opendaylight:params:xml:ns:yang:controller:" +
                "config?revision=2013-04-05)service-ref]"));
        assertEquals(1, usesNode.getRefines().size());
    }
}
