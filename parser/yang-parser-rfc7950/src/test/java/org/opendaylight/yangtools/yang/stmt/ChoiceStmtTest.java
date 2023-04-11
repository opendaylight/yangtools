/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

class ChoiceStmtTest extends AbstractModelTest {
    @Test
    void choiceAndCaseTest() {
        final var container = assertInstanceOf(ContainerSchemaNode.class, FOO.getDataChildByName(fooQName("transfer")));
        final var choice = assertInstanceOf(ChoiceSchemaNode.class, container.getDataChildByName(fooQName("how")));
        assertEquals(5, choice.getCases().size());

        var caseNode = choice.findCaseNodes("input").iterator().next();
        assertNotNull(caseNode);
        caseNode = choice.findCaseNodes("output").iterator().next();
        assertNotNull(caseNode);
        caseNode = choice.findCaseNodes("interval").iterator().next();
        assertNotNull(caseNode);
        caseNode = choice.findCaseNodes("daily").iterator().next();
        assertNotNull(caseNode);
        caseNode = choice.findCaseNodes("manual").iterator().next();
        assertNotNull(caseNode);
        assertEquals("interval", choice.getDefaultCase().orElseThrow().getQName().getLocalName());
    }
}
