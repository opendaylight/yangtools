/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test.yin;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinFileChoiceStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(10, modules.size());
    }

    @Test
    public void testChoiceAndCases() {
        Module testModule = TestUtils.findModule(modules, "config");
        assertNotNull(testModule);

        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName("modules");
        assertNotNull(container);

        ListSchemaNode list = (ListSchemaNode) container.getDataChildByName("module");
        assertNotNull(list);

        ChoiceSchemaNode choice = (ChoiceSchemaNode) list.getDataChildByName("configuration");
        assertNotNull(choice);

        assertEquals("configuration", choice.getQName().getLocalName());
        assertTrue(choice.getConstraints().isMandatory());
        assertTrue(choice.isConfiguration());
        assertEquals(1, choice.getCases().size());

        // this choice is augmented (see main-impl.yang.xml)
        Iterator<ChoiceCaseNode> casesIterator = choice.getCases().iterator();
        ChoiceCaseNode caseNode = casesIterator.next();
        assertEquals("main-impl", caseNode.getQName().getLocalName());
        assertEquals(13, caseNode.getChildNodes().size());

        RevisionAwareXPath whenCondition = caseNode.getConstraints().getWhenCondition();
        assertNotNull(whenCondition);

        choice = (ChoiceSchemaNode) list.getDataChildByName("state");
        assertNotNull(choice);

        assertEquals("state", choice.getQName().getLocalName());
        assertFalse(choice.getConstraints().isMandatory());
        assertFalse(choice.isConfiguration());
        assertTrue(choice.getCases().isEmpty());
    }
}
