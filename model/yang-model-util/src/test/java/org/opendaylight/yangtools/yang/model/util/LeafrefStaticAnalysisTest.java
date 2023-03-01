/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class LeafrefStaticAnalysisTest {
    private static final QName FOO = QName.create("leafrefs", "foo");

    private static EffectiveModelContext context;
    private static GroupingDefinition grp;
    private static ListSchemaNode foo;
    private static ContainerSchemaNode bar;
    private static Module module;

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYangResource("/leafrefs.yang");
        module = context.getModules().iterator().next();

        foo = (ListSchemaNode) module.findDataChildByName(FOO).get();
        bar = (ContainerSchemaNode) foo.findDataChildByName(QName.create(FOO, "bar")).get();
        grp = module.getGroupings().iterator().next();
    }

    @Test
    void testGrpOuterId() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(QName.create(FOO, "outer-id")).get();
        // Cannot be found as the reference goes outside of the grouping
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "outer-id"));
        assertThrowsInvalidPath(stack, leaf);
    }

    @Test
    void testFooOuterId() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(QName.create(FOO, "outer-id")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "outer-id"));
        final SchemaNode found = (SchemaNode) stack.resolvePathExpression(((LeafrefTypeDefinition) leaf.getType())
                .getPathStatement());

        assertThat(found, isA(LeafSchemaNode.class));
        assertEquals(QName.create(FOO, "id"), found.getQName());
    }

    @Test
    void testGrpOuterIndirectProp() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(
                QName.create(FOO, "outer-indirect-prop")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "outer-indirect-prop"));
        // Cannot resolve deref outer-id
        assertThrowsInvalidPath(stack, leaf);
    }

    @Test
    void testFooOuterIndirectProp() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(
                QName.create(FOO, "outer-indirect-prop")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "outer-indirect-prop"));
        final SchemaNode found = (SchemaNode) stack.resolvePathExpression(((LeafrefTypeDefinition) leaf.getType())
                .getPathStatement());

        assertThat(found, isA(LeafSchemaNode.class));
        assertEquals(QName.create(FOO, "prop"), found.getQName());
    }

    @Test
    void testGrpIndirect() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(QName.create(FOO, "indirect")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "indirect"));
        final SchemaNode found = (SchemaNode) stack.resolvePathExpression(((LeafrefTypeDefinition) leaf.getType())
                .getPathStatement());

        assertThat(found, isA(LeafSchemaNode.class));
        assertEquals(QName.create(FOO, "prop"), found.getQName());
    }

    @Test
    void testFooIndirect() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(QName.create(FOO, "indirect")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "indirect"));
        final SchemaNode found = (SchemaNode) stack.resolvePathExpression(((LeafrefTypeDefinition) leaf.getType())
                .getPathStatement());

        assertThat(found, isA(LeafSchemaNode.class));
        assertEquals(QName.create(FOO, "prop"), found.getQName());
    }

    @Test
    void testGrpDerefNonExistent() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(
                QName.create(FOO, "deref-non-existent")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "deref-non-existent"));
        assertThrowsMissingXyzzy(stack, leaf, "grouping (leafrefs)grp");
    }

    @Test
    void testFooDerefNonExistent() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(
                QName.create(FOO, "deref-non-existent")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "deref-non-existent"));
        assertThrowsMissingXyzzy(stack, leaf, "schema parent (leafrefs)bar");
    }

    @Test
    void testGrpNonExistentDeref() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(
                QName.create(FOO, "non-existent-deref")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "non-existent-deref"));
        assertThrowsMissingXyzzy(stack, leaf, "schema parent (leafrefs)foo");
    }

    @Test
    void testFooNonExistentDeref() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(
                QName.create(FOO, "non-existent-deref")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "non-existent-deref"));
        assertThrowsMissingXyzzy(stack, leaf, "schema parent (leafrefs)foo");
    }

    @Test
    void testNonExistentRelativeXpath() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(
                QName.create(FOO, "indirect-with-current")).get();
        final SchemaInferenceStack stack = SchemaInferenceStack.ofDataTreePath(context,
                foo.getQName(), bar.getQName(), QName.create(FOO, "indirect-with-current"));
        assertThrowsMissingChild(stack, leaf, "(leafrefs)n", "module (leafrefs)leafrefs");
    }

    private static void assertThrowsInvalidPath(final SchemaInferenceStack stack, final LeafSchemaNode leaf) {
        final IllegalArgumentException ex = assertThrowsIAE(stack, leaf);
        assertThat(ex.getMessage(), startsWith("Illegal parent access in "));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(IllegalStateException.class));
        assertEquals("Unexpected current EmptyGroupingEffectiveStatement{argument=(leafrefs)grp}", cause.getMessage());
    }

    private static void assertThrowsMissingXyzzy(final SchemaInferenceStack stack, final LeafSchemaNode leaf,
            final String parentDesc) {
        assertThrowsMissingChild(stack, leaf, "(leafrefs)xyzzy", parentDesc);
    }

    private static void assertThrowsMissingChild(final SchemaInferenceStack stack, final LeafSchemaNode leaf,
            final String childName, final String parentDesc) {
        assertEquals("Data tree child " + childName + " not present in " + parentDesc,
            assertThrowsIAE(stack, leaf).getMessage());
    }

    private static IllegalArgumentException assertThrowsIAE(final SchemaInferenceStack stack,
            final LeafSchemaNode leaf) {
        return assertThrows(IllegalArgumentException.class,
            () -> stack.resolvePathExpression(((LeafrefTypeDefinition) leaf.getType()).getPathStatement()));
    }
}
