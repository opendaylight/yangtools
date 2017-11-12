/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
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

        final ChoiceCaseNode implCase = myChoice.getCaseNodeByName(foo("implicit-case-container"));
        assertNotNull(implCase);
        final ChoiceCaseNode declCaseOne = myChoice.getCaseNodeByName(foo("declared-case-one"));
        assertNotNull(declCaseOne);
        final ChoiceCaseNode secondImplCase = myChoice.getCaseNodeByName(foo("second-implicit-case-container"));
        assertNotNull(secondImplCase);
        final ChoiceCaseNode declCaseTwo = myChoice.getCaseNodeByName(foo("declared-case-two"));
        assertNotNull(declCaseTwo);

        assertEquals(1, declCaseOne.getChildNodes().size());
        assertFalse(getLeafSchemaNode(declCaseOne, "leaf-in-declare-case-one").isAugmenting());
        assertEquals(1, declCaseTwo.getChildNodes().size());
        assertFalse(getLeafSchemaNode(declCaseTwo, "leaf-in-declare-case-two").isAugmenting());

        assertEquals(2, implCase.getChildNodes().size());
        assertTrue(getLeafSchemaNode(implCase, "leaf-after-container").isAugmenting());
        final ContainerSchemaNode implCaseContainer = getContainerSchemaNode(implCase, "implicit-case-container");

        assertEquals(3, implCaseContainer.getChildNodes().size());
        assertTrue(getLeafSchemaNode(implCaseContainer, "leaf-inside-container").isAugmenting());
        assertFalse(getLeafSchemaNode(implCaseContainer, "declared-leaf-in-case-container").isAugmenting());
        final ContainerSchemaNode declContInCaseCont = getContainerSchemaNode(implCaseContainer,
                "declared-container-in-case-container");

        assertEquals(1, declContInCaseCont.getChildNodes().size());
        assertFalse(getLeafSchemaNode(declContInCaseCont, "declared-leaf").isAugmenting());

        assertEquals(2, secondImplCase.getChildNodes().size());
        assertTrue(getLeafSchemaNode(secondImplCase, "leaf-after-second-container").isAugmenting());
        final ContainerSchemaNode secondImplCaseContainer = getContainerSchemaNode(secondImplCase,
                "second-implicit-case-container");

        assertEquals(2, secondImplCaseContainer.getChildNodes().size());
        assertTrue(getLeafSchemaNode(secondImplCaseContainer, "leaf-inside-second-container").isAugmenting());
        assertFalse(getLeafSchemaNode(secondImplCaseContainer, "declared-leaf-in-second-case-container")
            .isAugmenting());
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
