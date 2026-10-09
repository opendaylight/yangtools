/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema.ChildReusePolicy;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Tests for looking up children through {@link DataSchemaContext}s, as parsers do: adding a child with
 * {@link CompositeNodeDataWithSchema#addChild(Composite, QName, ChildReusePolicy)}, and finding where a parser starts
 * with {@link DataSchemaContextTree#childByInference(EffectiveStatementInference)}.
 */
class YT1899Test {
    private static final QName TOP = QName.create("yt1899", "top");
    private static final QName OTHER = QName.create(TOP, "other");
    private static final QName OUTER = QName.create(TOP, "outer");
    private static final QName FIRST = QName.create(TOP, "first");
    private static final QName FIRST_LEAF = QName.create(TOP, "first-leaf");
    private static final QName FIRST_OTHER = QName.create(TOP, "first-other");
    private static final QName INNER = QName.create(TOP, "inner");
    private static final QName INNER_LEAF = QName.create(TOP, "inner-leaf");
    private static final QName SECOND_LEAF = QName.create(TOP, "second-leaf");
    private static final QName THUD = QName.create(TOP, "thud");
    private static final QName INPUT = QName.create(TOP, "input");

    private static EffectiveModelContext MODEL_CONTEXT;
    private static DataSchemaContextTree TREE;

    @BeforeAll
    static void beforeAll() {
        MODEL_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/yt1899/yang");
        TREE = DataSchemaContextTree.from(MODEL_CONTEXT);
    }

    @Test
    void addDirectChild() {
        final var top = newTop();
        final var child = assertAdded(top, OTHER);
        assertInstanceOf(LeafNodeDataWithSchema.class, child.data());
        assertEquals(1, top.childSizeHint());
    }

    @Test
    void addChildrenOfOneCase() {
        // Both leaves end up in the same choice node
        final var top = newTop();
        assertAdded(top, FIRST_LEAF);
        assertAdded(top, FIRST_OTHER);
        assertEquals(1, top.childSizeHint());
    }

    @Test
    void addChildOfNestedChoice() {
        final var top = newTop();
        assertAdded(top, INNER_LEAF);
        assertAdded(top, FIRST_LEAF);
        assertEquals(1, top.childSizeHint());
    }

    @Test
    void addChildrenOfTwoCases() {
        final var top = newTop();
        assertAdded(top, FIRST_LEAF);
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> top.addChild(topContext(), SECOND_LEAF, ChildReusePolicy.NOOP));
        assertEquals("Data from case " + QName.create(TOP, "second") + " are specified but other data from case "
            + FIRST + " were specified earlier. Data aren't from the same case.", ex.getMessage());
    }

    @Test
    void addUnknownChild() {
        final var top = newTop();
        assertNull(top.addChild(topContext(), QName.create(TOP, "unknown"), ChildReusePolicy.NOOP));
        assertEquals(0, top.childSizeHint());
    }

    @Test
    void addChoiceName() {
        // A choice has no element of its own, so its name is not a child, at any depth
        final var top = newTop();
        assertNull(top.addChild(topContext(), OUTER, ChildReusePolicy.NOOP));
        assertNull(top.addChild(topContext(), INNER, ChildReusePolicy.NOOP));
        assertEquals(0, top.childSizeHint());
    }

    @Test
    void emptyInferenceIsTheRoot() {
        assertSame(TREE.getRoot(), TREE.childByInference(SchemaInferenceStack.of(MODEL_CONTEXT).toInference()));
    }

    @Test
    void dataTreeInferenceIsShared() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterDataTree(TOP);
        assertSame(topContext(), TREE.childByInference(stack.toInference()));
    }

    @Test
    void inferenceThroughChoiceAndCase() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(TOP);
        stack.enterChoice(OUTER);
        // A choice holds no data nodes of its own
        assertNull(TREE.childByInference(stack.toInference()));

        stack.enterSchemaTree(FIRST);
        stack.enterSchemaTree(FIRST_LEAF);
        final var leaf = TREE.childByInference(stack.toInference());
        assertNotNull(leaf);
        assertEquals(FIRST_LEAF, leaf.dataSchemaNode().getQName());
    }

    @Test
    void inferenceEndingAtCase() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(TOP);
        stack.enterChoice(OUTER);
        stack.enterSchemaTree(FIRST);
        final var first = assertInstanceOf(Composite.class, TREE.childByInference(stack.toInference()));
        assertEquals(FIRST, first.dataSchemaNode().getQName());
        // A case is not part of the tree, so each call creates its context anew
        assertNotSame(first, TREE.childByInference(stack.toInference()));

        // Its own children and those of a choice inside it are known, but not those of other cases
        final var firstLeaf = first.childByQName(FIRST_LEAF);
        assertNotNull(firstLeaf);
        assertEquals(FIRST_LEAF, firstLeaf.dataSchemaNode().getQName());
        assertInstanceOf(DataSchemaContext.Choice.class, first.childByQName(INNER_LEAF));
        assertNull(first.childByQName(SECOND_LEAF));

        // Adding a child does not add the choice and case where the data starts
        final var data = CompositeNodeDataWithSchema.of(first.dataSchemaNode());
        final var child = data.addChild(first, FIRST_LEAF, ChildReusePolicy.NOOP);
        assertNotNull(child);
        assertInstanceOf(LeafNodeDataWithSchema.class, child.data());
        assertNull(data.addChild(first, SECOND_LEAF, ChildReusePolicy.NOOP));
        assertEquals(1, data.childSizeHint());
    }

    @Test
    void rpcInferenceIsNotShared() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(THUD);
        final var rpc = assertInstanceOf(Composite.class, TREE.childByInference(stack.toInference()));
        assertNotSame(rpc, TREE.childByInference(stack.toInference()));

        stack.enterSchemaTree(INPUT);
        final var input = TREE.childByInference(stack.toInference());
        assertNotNull(input);
        assertEquals(INPUT, input.dataSchemaNode().getQName());
    }

    @Test
    void foreignInferenceIsRejected() {
        // Parsing the same model again gives a different model context
        final var other = YangParserTestUtils.parseYangResourceDirectory("/yt1899/yang");
        final var inference = SchemaInferenceStack.of(other).toInference();
        assertThrows(IllegalArgumentException.class, () -> TREE.childByInference(inference));
    }

    private static CompositeNodeDataWithSchema<?> newTop() {
        return CompositeNodeDataWithSchema.of(topContext().dataSchemaNode());
    }

    private static Composite topContext() {
        return assertInstanceOf(Composite.class, TREE.getRoot().childByQName(TOP));
    }

    private static CompositeNodeDataWithSchema.Child assertAdded(final CompositeNodeDataWithSchema<?> top,
            final QName qname) {
        final var child = top.addChild(topContext(), qname, ChildReusePolicy.NOOP);
        assertNotNull(child);
        assertEquals(qname, child.data().getSchema().getQName());
        assertSame(child.data().getSchema(), child.context().dataSchemaNode());
        return child;
    }
}
