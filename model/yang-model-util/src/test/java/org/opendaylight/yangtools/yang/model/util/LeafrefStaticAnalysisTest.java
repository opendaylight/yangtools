/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

        foo = assertInstanceOf(ListSchemaNode.class, module.getDataChildByName(FOO));
        bar = assertInstanceOf(ContainerSchemaNode.class, foo.getDataChildByName(QName.create(FOO, "bar")));
        grp = module.getGroupings().iterator().next();
    }

    @Test
    void testGrpOuterId() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class, grp.getDataChildByName(QName.create(FOO, "outer-id")));
        // Cannot be found as the reference goes outside of the grouping
        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "outer-id"));
        assertThrowsInvalidPath(stack, leaf);
    }

    @Test
    void testFooOuterId() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class, bar.getDataChildByName(QName.create(FOO, "outer-id")));
        final var leafType = assertInstanceOf(LeafrefTypeDefinition.class, leaf.getType());
        final var stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "outer-id"));
        final var found = assertInstanceOf(LeafSchemaNode.class,
            stack.resolvePathExpression(leafType.getPathStatement()));
        assertEquals(QName.create(FOO, "id"), found.getQName());
    }

    @Test
    void testGrpOuterIndirectProp() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            grp.getDataChildByName(QName.create(FOO, "outer-indirect-prop")));
        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "outer-indirect-prop"));
        // Cannot resolve deref outer-id
        assertThrowsInvalidPath(stack, leaf);
    }

    @Test
    void testFooOuterIndirectProp() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            bar.getDataChildByName(QName.create(FOO, "outer-indirect-prop")));
        final var leafType = assertInstanceOf(LeafrefTypeDefinition.class, leaf.getType());
        final var stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "outer-indirect-prop"));
        final var found = assertInstanceOf(LeafSchemaNode.class,
            stack.resolvePathExpression(leafType.getPathStatement()));
        assertEquals(QName.create(FOO, "prop"), found.getQName());
    }

    @Test
    void testGrpIndirect() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class, grp.getDataChildByName(QName.create(FOO, "indirect")));
        final var leafType = assertInstanceOf(LeafrefTypeDefinition.class, leaf.getType());
        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "indirect"));
        final var found = assertInstanceOf(LeafSchemaNode.class,
            stack.resolvePathExpression(leafType.getPathStatement()));
        assertEquals(QName.create(FOO, "prop"), found.getQName());
    }

    @Test
    void testFooIndirect() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class, bar.getDataChildByName(QName.create(FOO, "indirect")));
        final var leafType = assertInstanceOf(LeafrefTypeDefinition.class, leaf.getType());
        final var stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "indirect"));
        final var found = assertInstanceOf(LeafSchemaNode.class,
            stack.resolvePathExpression(leafType.getPathStatement()));
        assertEquals(QName.create(FOO, "prop"), found.getQName());
    }

    @Test
    void testGrpDerefNonExistent() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            grp.getDataChildByName(QName.create(FOO, "deref-non-existent")));
        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "deref-non-existent"));
        assertThrowsMissingXyzzy(stack, leaf, "grouping (leafrefs)grp");
    }

    @Test
    void testFooDerefNonExistent() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            bar.getDataChildByName(QName.create(FOO, "deref-non-existent")));
        final var stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "deref-non-existent"));
        assertThrowsMissingXyzzy(stack, leaf, "schema parent (leafrefs)bar");
    }

    @Test
    void testGrpNonExistentDeref() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            grp.getDataChildByName(QName.create(FOO, "non-existent-deref")));
        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(grp.getQName());
        stack.enterSchemaTree(QName.create(FOO, "non-existent-deref"));
        assertThrowsMissingXyzzy(stack, leaf, "schema parent (leafrefs)foo");
    }

    @Test
    void testFooNonExistentDeref() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            bar.findDataChildByName(QName.create(FOO, "non-existent-deref")));
        final var stack = SchemaInferenceStack.ofDataTreePath(context, foo.getQName(), bar.getQName());
        stack.enterSchemaTree(QName.create(FOO, "non-existent-deref"));
        assertThrowsMissingXyzzy(stack, leaf, "schema parent (leafrefs)foo");
    }

    @Test
    void testNonExistentRelativeXpath() {
        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            bar.getDataChildByName(QName.create(FOO, "indirect-with-current")));
        final var stack = SchemaInferenceStack.ofDataTreePath(context,
                foo.getQName(), bar.getQName(), QName.create(FOO, "indirect-with-current"));
        assertThrowsMissingChild(stack, leaf, "(leafrefs)n", "module (leafrefs)leafrefs");
    }

    private static void assertThrowsInvalidPath(final SchemaInferenceStack stack, final LeafSchemaNode leaf) {
        final var ex = assertThrowsIAE(stack, leaf);
        assertThat(ex.getMessage(), startsWith("Illegal parent access in "));
        final var cause = assertInstanceOf(IllegalStateException.class, ex.getCause());
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
        return assertThrows(IllegalArgumentException.class, () -> stack.resolvePathExpression(
            assertInstanceOf(LeafrefTypeDefinition.class, leaf.getType()).getPathStatement()));
    }
}
