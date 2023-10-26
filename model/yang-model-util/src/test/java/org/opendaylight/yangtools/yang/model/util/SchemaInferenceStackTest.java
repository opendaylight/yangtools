/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.PathExpression.LocationPathSteps;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;

class SchemaInferenceStackTest {
    private static EffectiveModelContext context;
    private static Module myModule;

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYangResourceDirectory("/schema-context-util");
        myModule = context.findModule(XMLNamespace.of("uri:my-module"), Revision.of("2014-10-07")).orElseThrow();
    }

    @Test
    void findDataSchemaNodeTest() {
        final var importedModule = context.findModule(XMLNamespace.of("uri:imported-module"),
                Revision.of("2014-10-07")).orElseThrow();

        final QName myImportedContainer = QName.create(importedModule.getQNameModule(), "my-imported-container");
        final QName myImportedLeaf = QName.create(importedModule.getQNameModule(), "my-imported-leaf");

        final var testNode = assertInstanceOf(ContainerSchemaNode.class,
            importedModule.getDataChildByName(myImportedContainer)).getDataChildByName(myImportedLeaf);

        final var expr = mock(PathExpression.class);
        doReturn(true).when(expr).isAbsolute();
        doReturn(new LocationPathSteps(YangLocationPath.absolute(
                YangXPathAxis.CHILD.asStep(myImportedContainer), YangXPathAxis.CHILD.asStep(myImportedLeaf))))
                .when(expr).getSteps();

        assertEquals(testNode, SchemaInferenceStack.of(context).resolvePathExpression(expr));
    }

    @Test
    void findDataSchemaNodeTest2() {
        final QName myLeafInGrouping2 = QName.create(myModule.getQNameModule(), "my-leaf-in-gouping2");
        final var expr = mock(PathExpression.class);
        doReturn(true).when(expr).isAbsolute();
        doReturn(new LocationPathSteps(YangLocationPath.relative(YangXPathAxis.CHILD.asStep(myLeafInGrouping2))))
                .when(expr).getSteps();

        final var grouping = getGroupingByName(myModule, "my-grouping");
        final var stack = SchemaInferenceStack.of(context);
        assertSame(grouping, stack.enterGrouping(grouping.getQName()));
        assertEquals(grouping.getDataChildByName(myLeafInGrouping2), stack.resolvePathExpression(expr));
    }

    @Test
    void enterGroupingNegativeTest() {
        final var stack = SchemaInferenceStack.of(context);
        assertNotExistentGrouping(stack, "module (uri:my-module?revision=2014-10-07)my-module");
        stack.enterDataTree(QName.create(myModule.getQNameModule(), "my-container"));
        assertNotExistentGrouping(stack, "schema parent (uri:my-module?revision=2014-10-07)my-container");
    }

    @Test
    void enterNestedTypedefTest() {
        final var stack = SchemaInferenceStack.of(context);
        stack.enterDataTree(QName.create(myModule.getQNameModule(), "my-container"));
        assertNotNull(stack.enterTypedef(QName.create(myModule.getQNameModule(), "my-typedef-in-container")));
    }

    @Test
    void enterTypedefNegativeTest() {
        final var stack = SchemaInferenceStack.of(context);
        assertNotExistentTypedef(stack, "module (uri:my-module?revision=2014-10-07)my-module");
        stack.enterDataTree(QName.create(myModule.getQNameModule(), "my-container"));
        assertNotExistentTypedef(stack, "schema parent (uri:my-module?revision=2014-10-07)my-container");
    }

    @Test
    void rootIsCurrent() {
        final var stack = SchemaInferenceStack.of(context);
        assertEquals(Status.CURRENT, stack.effectiveStatus());
    }

    @Test
    void myGroupingIsCurrent() {
        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(QName.create(myModule.getQNameModule(), "my-grouping"));
        assertEquals(Status.CURRENT, stack.effectiveStatus());
    }

    @Test
    void myLeafInContainerIsDeprecated() {
        final var stack = SchemaInferenceStack.of(context);
        stack.enterDataTree(QName.create(myModule.getQNameModule(), "my-container"));
        stack.enterDataTree(QName.create(myModule.getQNameModule(), "my-leaf-in-container"));
        assertEquals(Status.DEPRECATED, stack.effectiveStatus());
    }

    @Test
    void twoInGroupingIsObsolete() {
        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(QName.create(myModule.getQNameModule(), "my-name"));
        stack.enterDataTree(QName.create(myModule.getQNameModule(), "two"));
        assertEquals(Status.OBSOLETE, stack.effectiveStatus());
    }

    @Test
    void twoInMyNameInputIsObsolete() {
        final var stack = SchemaInferenceStack.of(context);
        stack.enterSchemaTree(QName.create(myModule.getQNameModule(), "my-name"));
        stack.enterSchemaTree(QName.create(myModule.getQNameModule(), "input"));
        stack.enterSchemaTree(QName.create(myModule.getQNameModule(), "my-choice"));
        stack.enterSchemaTree(QName.create(myModule.getQNameModule(), "case-two"));
        stack.enterSchemaTree(QName.create(myModule.getQNameModule(), "two"));
        assertEquals(Status.OBSOLETE, stack.effectiveStatus());
    }

    private static void assertNotExistentGrouping(final SchemaInferenceStack stack, final String parentDesc) {
        final QName nonExistent = QName.create(myModule.getQNameModule(), "non-existent");
        assertEquals("Grouping (uri:my-module?revision=2014-10-07)non-existent not present in " + parentDesc,
            assertThrows(IllegalArgumentException.class, () -> stack.enterGrouping(nonExistent)).getMessage());
    }

    private static void assertNotExistentTypedef(final SchemaInferenceStack stack, final String parentDesc) {
        final QName nonExistent = QName.create(myModule.getQNameModule(), "non-existent");
        assertEquals("Typedef (uri:my-module?revision=2014-10-07)non-existent not present in " + parentDesc,
            assertThrows(IllegalArgumentException.class, () -> stack.enterTypedef(nonExistent)).getMessage());
    }

    private static GroupingDefinition getGroupingByName(final DataNodeContainer dataNodeContainer, final String name) {
        for (var grouping : dataNodeContainer.getGroupings()) {
            if (grouping.getQName().getLocalName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }
}