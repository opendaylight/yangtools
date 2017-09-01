/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public class YinFileChoiceStmtTest {

    private SchemaContext context;

    @Before
    public void init() throws ReactorException, SAXException, IOException, URISyntaxException {
        context = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, context.getModules().size());
    }

    @Test
    public void testChoiceAndCases() {
        final Module testModule = TestUtils.findModule(context, "config").get();
        assertNotNull(testModule);

        final ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(QName.create(
                testModule.getQNameModule(), "modules"));
        assertNotNull(container);

        final ListSchemaNode list = (ListSchemaNode) container.getDataChildByName(QName.create(
                testModule.getQNameModule(), "module"));
        assertNotNull(list);

        ChoiceSchemaNode choice = (ChoiceSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(),
                "configuration"));
        assertNotNull(choice);

        assertEquals("configuration", choice.getQName().getLocalName());
        assertTrue(choice.getConstraints().isMandatory());
        assertTrue(choice.isConfiguration());
        assertEquals(1, choice.getCases().size());

        // this choice is augmented (see main-impl.yang.xml)
        final Iterator<ChoiceCaseNode> casesIterator = choice.getCases().iterator();
        final ChoiceCaseNode caseNode = casesIterator.next();
        assertEquals("main-impl", caseNode.getQName().getLocalName());
        assertEquals(13, caseNode.getChildNodes().size());

        final RevisionAwareXPath whenCondition = caseNode.getConstraints().getWhenCondition();
        assertNotNull(whenCondition);

        choice = (ChoiceSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(), "state"));
        assertNotNull(choice);

        assertEquals("state", choice.getQName().getLocalName());
        assertFalse(choice.getConstraints().isMandatory());
        assertFalse(choice.isConfiguration());
        assertTrue(choice.getCases().isEmpty());
    }
}
