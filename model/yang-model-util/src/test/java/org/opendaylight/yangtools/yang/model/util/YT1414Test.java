/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaTreeInference;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1414Test {
    private static EffectiveModelContext context1;
    private static Module myModule1;
    private static Module myModule2;

    private static QName foo1;
    private static QName foo2;

    @BeforeClass
    public static void beforeClass() {
        context1 = YangParserTestUtils.parseYangResourceDirectory("/schema-context-util");
        final EffectiveModelContext context2 = YangParserTestUtils.parseYangResource("/yt1231.yang");
        myModule1 = context1.findModule(XMLNamespace.of("uri:my-module"), Revision.of("2014-10-07")).get();
        foo2 = QName.create("foo", "foo");
        myModule2 = context2.findModule(foo2.getModule()).get();
        foo1 = QName.create(myModule1.getQNameModule(), "my-container");
    }

    @Test
    public void basicTest() {
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context1);
        stack.enterDataTree(foo1);
        final SchemaTreeInference tree = stack.toSchemaTreeInference();
        final SchemaInferenceStack stackFromTree = SchemaInferenceStack.ofInference(tree);
        assertTrue(tree.toSchemaNodeIdentifier().getNodeIdentifiers().contains(foo1));
        assertTrue(stackFromTree.toSchemaNodeIdentifier().getNodeIdentifiers().contains(foo1));
    }

    @Test
    public void safeModeTest() {
        System.setProperty("org.opendaylight.yangtools.yang.model.spi.validation", "safe");
        final SchemaTreeEffectiveStatement<?> schema1 = ((ModuleEffectiveStatement) myModule1)
                .findSchemaTreeNode(foo1).get();
        final SchemaTreeEffectiveStatement<?> schema2 = ((ModuleEffectiveStatement) myModule2)
                .findSchemaTreeNode(foo2).get();
        final ImmutableList.Builder<SchemaTreeEffectiveStatement<?>> builder = ImmutableList.builderWithExpectedSize(2);
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> DefaultSchemaTreeInference.unsafeOf(context1, builder.add(schema1).add(schema2).build()));
        assertThat(ex, instanceOf(IllegalArgumentException.class));
        assertThat(ex.getMessage(), startsWith("Cannot resolve step (foo)foo in"));
    }
}
