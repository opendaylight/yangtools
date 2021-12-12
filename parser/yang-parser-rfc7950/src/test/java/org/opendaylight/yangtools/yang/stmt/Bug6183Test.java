/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug6183Test {
    private static final String FOO_NS = "foo";

    @Test
    public void testYang10() throws Exception {
        assertSchemaContext(StmtTestUtils.parseYangSources("/bugs/bug6183/yang10"));
    }

    @Test
    public void testYang11() throws Exception {
        assertSchemaContext(StmtTestUtils.parseYangSources("/bugs/bug6183/yang11"));
    }

    public void assertSchemaContext(final SchemaContext context) throws Exception {
        assertNotNull(context);
        assertEquals(3, context.getChildNodes().size());
        assertEquals(1, context.getModules().size());
        assertEquals(4, context.getModules().iterator().next().getAugmentations().size());

        assertTrue(context.getDataChildByName(foo("before")) instanceof ContainerSchemaNode);
        assertTrue(context.getDataChildByName(foo("after")) instanceof ContainerSchemaNode);

        final DataSchemaNode dataChildByName = context.getDataChildByName(foo("my-choice"));
        assertTrue(dataChildByName instanceof ChoiceSchemaNode);
        final ChoiceSchemaNode myChoice = (ChoiceSchemaNode) dataChildByName;

        assertEquals(4, myChoice.getCases().size());

        final CaseSchemaNode implCase = myChoice.findCase(foo("implicit-case-container")).get();
        final CaseSchemaNode declCaseOne = myChoice.findCase(foo("declared-case-one")).get();
        final CaseSchemaNode secondImplCase = myChoice.findCase(foo("second-implicit-case-container")).get();
        final CaseSchemaNode declCaseTwo = myChoice.findCase(foo("declared-case-two")).get();

        assertEquals(1, declCaseOne.getChildNodes().size());
        getLeafSchemaNode(declCaseOne, "leaf-in-declare-case-one");
        assertEquals(1, declCaseTwo.getChildNodes().size());
        getLeafSchemaNode(declCaseTwo, "leaf-in-declare-case-two");

        assertEquals(2, implCase.getChildNodes().size());
        getLeafSchemaNode(implCase, "leaf-after-container");
        final ContainerSchemaNode implCaseContainer = getContainerSchemaNode(implCase, "implicit-case-container");

        assertEquals(3, implCaseContainer.getChildNodes().size());
        getLeafSchemaNode(implCaseContainer, "leaf-inside-container");
        getLeafSchemaNode(implCaseContainer, "declared-leaf-in-case-container");
        final ContainerSchemaNode declContInCaseCont = getContainerSchemaNode(implCaseContainer,
                "declared-container-in-case-container");

        assertEquals(1, declContInCaseCont.getChildNodes().size());
        getLeafSchemaNode(declContInCaseCont, "declared-leaf");

        assertEquals(2, secondImplCase.getChildNodes().size());
        getLeafSchemaNode(secondImplCase, "leaf-after-second-container");
        final ContainerSchemaNode secondImplCaseContainer = getContainerSchemaNode(secondImplCase,
                "second-implicit-case-container");

        assertEquals(2, secondImplCaseContainer.getChildNodes().size());
        getLeafSchemaNode(secondImplCaseContainer, "leaf-inside-second-container");
        getLeafSchemaNode(secondImplCaseContainer, "declared-leaf-in-second-case-container");
    }

    private static ContainerSchemaNode getContainerSchemaNode(final DataNodeContainer parent,
            final String containerName) {
        final DataSchemaNode dataChildByName = parent.getDataChildByName(foo(containerName));
        assertTrue(dataChildByName instanceof ContainerSchemaNode);
        return (ContainerSchemaNode) dataChildByName;
    }

    private static LeafSchemaNode getLeafSchemaNode(final DataNodeContainer parent, final String leafName) {
        final DataSchemaNode dataChildByName = parent.getDataChildByName(foo(leafName));
        assertTrue(dataChildByName instanceof LeafSchemaNode);
        return (LeafSchemaNode) dataChildByName;
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }
}
