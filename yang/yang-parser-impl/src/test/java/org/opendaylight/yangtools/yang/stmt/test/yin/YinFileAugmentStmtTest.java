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

public class YinFileAugmentStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
            modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
            assertEquals(10, modules.size());
    }

    @Test
    public void testAugment() {
        Module testModule = TestUtils.findModule(modules, "main-impl");
        assertNotNull(testModule);

        Set<AugmentationSchema> augmentations = testModule.getAugmentations();
        assertEquals(1, augmentations.size());

        Iterator<AugmentationSchema> augmentIterator = augmentations.iterator();
        AugmentationSchema augment = augmentIterator.next();
        assertNotNull(augment);
        assertTrue(augment.getTargetPath().toString().contains(
                "(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)modules, " +
                        "(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)module, " +
                        "(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)configuration"));

        assertEquals(1, augment.getChildNodes().size());
        ChoiceCaseNode caseNode = (ChoiceCaseNode) augment.getDataChildByName("main-impl");
        assertNotNull(caseNode);
    }

}
