/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class ChoiceStmtTest {

    private static final YangStatementSourceImpl CHOICE_MODULE = new YangStatementSourceImpl("/model/foo.yang", false);
    private static final YangStatementSourceImpl IMPORTED_MODULE1 = new YangStatementSourceImpl("/model/bar.yang",
            false);
    private static final YangStatementSourceImpl IMPORTED_MODULE2 = new YangStatementSourceImpl("/model/baz.yang",
            false);
    private static final YangStatementSourceImpl INCLUDED_MODULE = new YangStatementSourceImpl("/model/subfoo.yang",
            false);

    @Test
    public void choiceAndCaseTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, CHOICE_MODULE, IMPORTED_MODULE1, IMPORTED_MODULE2, INCLUDED_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("foo", null);
        assertNotNull(testModule);

        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName("transfer");
        assertNotNull(container);

        ChoiceSchemaNode choice = (ChoiceSchemaNode) container.getDataChildByName("how");
        assertNotNull(choice);
        assertEquals(5, choice.getCases().size());

        ChoiceCaseNode caseNode = choice.getCaseNodeByName("input");
        assertNotNull(caseNode);
        caseNode = choice.getCaseNodeByName("output");
        assertNotNull(caseNode);
        caseNode = choice.getCaseNodeByName("interval");
        assertNotNull(caseNode);
        caseNode = choice.getCaseNodeByName("daily");
        assertNotNull(caseNode);
        caseNode = choice.getCaseNodeByName("manual");
        assertNotNull(caseNode);
        assertEquals("interval", choice.getDefaultCase());
    }
}
