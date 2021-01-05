/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.ut;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class LeafrefStaticAnalysisTest {
    private static final QName FOO = QName.create("leafrefs", "foo");

    private static EffectiveModelContext context;
    private static GroupingDefinition grp;
    private static ListSchemaNode foo;
    private static ContainerSchemaNode bar;
    private static Module module;

    @BeforeClass
    public static void beforeClass() {
        context = YangParserTestUtils.parseYangResource("/leafrefs.yang");
        module = context.getModules().iterator().next();

        foo = (ListSchemaNode) module.findDataChildByName(FOO).get();
        bar = (ContainerSchemaNode) foo.findDataChildByName(QName.create(FOO, "bar")).get();
        grp = module.getGroupings().iterator().next();
    }

    @Test
    public void testGrpOuterId() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(QName.create(FOO, "outer-id")).get();
        // Cannot be found as the reference goes outside of the grouping
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterGrouping(QName.create(FOO, "grp"));
        stack.enterSchemaTree(QName.create(FOO, "outer-id"));
        assertNull(SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack));
    }

    @Test
    public void testFooOuterId() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(QName.create(FOO, "outer-id")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(QName.create(FOO, "foo"));
        stack.enterSchemaTree(QName.create(FOO, "bar"));
        stack.enterSchemaTree(QName.create(FOO, "outer-id"));
        final SchemaNode found = SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack);

        assertThat(found, isA(LeafSchemaNode.class));
        assertEquals(SchemaPath.create(true, FOO, QName.create(FOO, "id")), found.getPath());
    }

    @Test
    public void testGrpOuterIndirectProp() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(
            QName.create(FOO, "outer-indirect-prop")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterGrouping(QName.create(FOO, "grp"));
        stack.enterSchemaTree(QName.create(FOO, "outer-indirect-prop"));
        // Cannot resolve deref outer-id
        assertNull(SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack));
    }

    @Test
    public void testFooOuterIndirectProp() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(
            QName.create(FOO, "outer-indirect-prop")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(QName.create(FOO, "foo"));
        stack.enterSchemaTree(QName.create(FOO, "bar"));
        stack.enterSchemaTree(QName.create(FOO, "outer-indirect-prop"));
        final SchemaNode found = SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack);

        assertThat(found, isA(LeafSchemaNode.class));
        assertEquals(QName.create(FOO, "prop"), found.getQName());
    }

    @Test
    public void testGrpIndirect() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(QName.create(FOO, "indirect")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterGrouping(QName.create(FOO, "grp"));
        stack.enterSchemaTree(QName.create(FOO, "indirect"));
        final SchemaNode found = SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack);

        assertThat(found, isA(LeafSchemaNode.class));
        assertEquals(QName.create(FOO, "prop"), found.getQName());
    }

    @Test
    public void testFooIndirect() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(QName.create(FOO, "indirect")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(QName.create(FOO, "foo"));
        stack.enterSchemaTree(QName.create(FOO, "bar"));
        stack.enterSchemaTree(QName.create(FOO, "indirect"));
        final SchemaNode found = SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack);

        assertThat(found, isA(LeafSchemaNode.class));
        assertEquals(QName.create(FOO, "prop"), found.getQName());
    }

    @Test
    public void testGrpDerefNonExistent() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(
            QName.create(FOO, "deref-non-existent")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterGrouping(QName.create(FOO, "grp"));
        stack.enterSchemaTree(QName.create(FOO, "deref-non-existent"));
        assertNull(SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack));
    }

    @Test
    public void testFooDerefNonExistent() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(
            QName.create(FOO, "deref-non-existent")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(QName.create(FOO, "foo"));
        stack.enterSchemaTree(QName.create(FOO, "bar"));
        stack.enterSchemaTree(QName.create(FOO, "deref-non-existent"));
        assertNull(SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack));
    }

    @Test
    public void testGrpNonExistentDeref() {
        final LeafSchemaNode leaf = (LeafSchemaNode) grp.findDataChildByName(
            QName.create(FOO, "non-existent-deref")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterGrouping(QName.create(FOO, "grp"));
        stack.enterSchemaTree(QName.create(FOO, "non-existent-deref"));
        assertNull(SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack));
    }

    @Test
    public void testFooNonExistentDeref() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(
            QName.create(FOO, "non-existent-deref")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(QName.create(FOO, "foo"));
        stack.enterSchemaTree(QName.create(FOO, "bar"));
        stack.enterSchemaTree(QName.create(FOO, "deref-non-existent"));
        assertNull(SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
            ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack));
    }

    @Test
    public void testNonExistentRelativeXpath() {
        final LeafSchemaNode leaf = (LeafSchemaNode) bar.findDataChildByName(
                QName.create(FOO, "indirect-with-current")).get();
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(QName.create(FOO, "foo"));
        stack.enterSchemaTree(QName.create(FOO, "bar"));
        stack.enterSchemaTree(QName.create(FOO, "indirect-with-current"));
        assertNull(SchemaContextUtil.findDataSchemaNodeForRelativeXPath(module,
                ((LeafrefTypeDefinition) leaf.getType()).getPathStatement(), stack));
    }
}
