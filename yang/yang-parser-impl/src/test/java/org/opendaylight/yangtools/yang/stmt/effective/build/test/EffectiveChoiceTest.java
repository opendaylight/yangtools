/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.effective.build.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class EffectiveChoiceTest {
    private static final YangStatementSourceImpl FOO = new YangStatementSourceImpl(
            "/choice-case-qname-test/foo.yang", false);


    @Test(expected = IllegalArgumentException.class)
    public void ChoiceCaseQNameTest1() throws ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, FOO);
        EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Module fooModule = result.findModuleByName("foo", null);
        DataSchemaNode choiceNode = fooModule.getDataChildByName("testing-choice");

        QName testQName = null;
        ChoiceCaseNode caseNode = ((ChoiceSchemaNode)choiceNode).getCaseNodeByName(testQName);

        assertNull(caseNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ChoiceCaseQNameTest2() throws ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, FOO);
        EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Module fooModule = result.findModuleByName("foo", null);
        DataSchemaNode choiceNode = fooModule.getDataChildByName("testing-choice");

        String testString = null;
        ChoiceCaseNode caseNode = ((ChoiceSchemaNode)choiceNode).getCaseNodeByName(testString);

        assertNull(caseNode);
    }

    private static void addSources(final CrossSourceStatementReactor.BuildAction reactor, final
    YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }

}
