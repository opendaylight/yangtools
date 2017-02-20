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
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class ChoiceStmtTest {

    private static final StatementStreamSource CHOICE_MODULE = sourceForResource("/model/foo.yang");
    private static final StatementStreamSource IMPORTED_MODULE1 = sourceForResource("/model/bar.yang");
    private static final StatementStreamSource IMPORTED_MODULE2 = sourceForResource("/model/baz.yang");
    private static final StatementStreamSource INCLUDED_MODULE = sourceForResource("/model/subfoo.yang");

    @Test
    public void choiceAndCaseTest() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(CHOICE_MODULE, IMPORTED_MODULE1, IMPORTED_MODULE2, INCLUDED_MODULE);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModuleByName("foo", null);
        assertNotNull(testModule);

        final ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(QName.create(
                testModule.getQNameModule(), "transfer"));
        assertNotNull(container);

        final ChoiceSchemaNode choice = (ChoiceSchemaNode) container.getDataChildByName(QName.create(
                testModule.getQNameModule(), "how"));
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
