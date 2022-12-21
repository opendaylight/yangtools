/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

class YinFileChoiceStmtTest extends AbstractYinModulesTest {
    @Test
    void testChoiceAndCases() {
        final Module testModule = context.findModules("config").iterator().next();
        assertNotNull(testModule);

        final ListSchemaNode list = (ListSchemaNode) testModule.findDataChildByName(
            QName.create(testModule.getQNameModule(), "modules"),
            QName.create(testModule.getQNameModule(), "module")).get();

        ChoiceSchemaNode choice = (ChoiceSchemaNode) list.findDataChildByName(QName.create(testModule.getQNameModule(),
            "configuration")).get();

        assertEquals("configuration", choice.getQName().getLocalName());
        assertTrue(choice.isMandatory());
        assertEquals(Optional.of(Boolean.TRUE), choice.effectiveConfig());
        assertEquals(1, choice.getCases().size());

        // this choice is augmented (see main-impl.yang.xml)
        final var casesIterator = choice.getCases().iterator();
        final CaseSchemaNode caseNode = casesIterator.next();
        assertEquals("main-impl", caseNode.getQName().getLocalName());
        assertEquals(13, caseNode.getChildNodes().size());

        assertTrue(caseNode.getWhenCondition().isPresent());

        choice = (ChoiceSchemaNode) list.findDataChildByName(QName.create(testModule.getQNameModule(), "state")).get();

        assertEquals("state", choice.getQName().getLocalName());
        assertFalse(choice.isMandatory());
        assertEquals(Optional.of(Boolean.FALSE), choice.effectiveConfig());
        assertTrue(choice.getCases().isEmpty());
    }
}
