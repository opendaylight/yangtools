/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public class YinFileUsesStmtTest {

    private SchemaContext context;

    @Before
    public void init() throws URISyntaxException, ReactorException, SAXException, IOException {
        context = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, context.getModules().size());
    }

    @Test
    public void testUses() {
        final Module testModule = TestUtils.findModule(context, "main-impl").get();
        assertNotNull(testModule);

        final Set<AugmentationSchema> augmentations = testModule.getAugmentations();
        assertEquals(1, augmentations.size());

        final Iterator<AugmentationSchema> augmentIterator = augmentations.iterator();
        final AugmentationSchema augment = augmentIterator.next();

        final ChoiceCaseNode caseNode = (ChoiceCaseNode) augment.getDataChildByName(QName.create(testModule.getQNameModule(),
                "main-impl"));
        assertNotNull(caseNode);

        final ContainerSchemaNode container = (ContainerSchemaNode) caseNode.getDataChildByName(QName.create(
                testModule.getQNameModule(), "notification-service"));
        assertNotNull(container);

        assertEquals(1, container.getUses().size());
        final UsesNode usesNode = container.getUses().iterator().next();
        assertNotNull(usesNode);
        assertTrue(usesNode
                .getGroupingPath()
                .toString()
                .contains(
                        "[(urn:opendaylight:params:xml:ns:yang:controller:" + "config?revision=2013-04-05)service-ref]"));
        assertEquals(1, usesNode.getRefines().size());
    }
}
