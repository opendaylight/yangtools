/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree.Step;
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
    private static final QName SECOND = QName.create(TOP, "second");
    private static final QName SECOND_LEAF = QName.create(TOP, "second-leaf");
    private static final QName SECOND_LIST = QName.create(TOP, "second-list");
    private static final QName SECOND_LIST_ID = QName.create(TOP, "id");
    private static final QName THUD = QName.create(TOP, "thud");
    private static final QName INPUT = QName.create(TOP, "input");
    private static final QName GRP = QName.create(TOP, "grp");
    private static final QName GRP_CONT = QName.create(TOP, "grp-cont");
    private static final QName GRP_LEAF = QName.create(TOP, "grp-leaf");
    private static final QName GRP_LIST = QName.create(TOP, "grp-list");
    private static final QName GRP_LIST_ID = QName.create(TOP, "grp-list-id");
    private static final QName GRP_ACT = QName.create(TOP, "grp-act");
    private static final QName GRP_ACT_IN = QName.create(TOP, "grp-act-in");
    private static final QName GRP_NOTIF = QName.create(TOP, "grp-notif");
    private static final QName GRP_NOTIF_LEAF = QName.create(TOP, "grp-notif-leaf");
    private static final QName GRP_CHOICE = QName.create(TOP, "grp-choice");
    private static final QName GRP_CASE = QName.create(TOP, "grp-case");
    private static final QName GRP_CASE_LEAF = QName.create(TOP, "grp-case-leaf");
    private static final QName TOP_GRP = QName.create(TOP, "top-grp");
    private static final QName TOP_GRP_CONT = QName.create(TOP, "top-grp-cont");
    private static final QName TOP_GRP_LEAF = QName.create(TOP, "top-grp-leaf");

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
        assertEquals("Data from case " + SECOND + " are specified but other data from case "
            + FIRST + " were specified earlier. Data aren't from the same case.", ex.getMessage());
    }

    @Test
    void addUnknownChild() {
        final var top = newTop();
        assertNull(top.addChild(topContext(), QName.create(TOP, "unknown"), ChildReusePolicy.NOOP));
        assertEquals(0, top.childSizeHint());
    }

    @Test
    void addListInsideChoice() {
        // Asked for its own name, a list steps on to its entry, so adding a list has to stop at the list itself
        final var top = newTop();
        final var child = assertAdded(top, SECOND_LIST);
        assertInstanceOf(ListNodeDataWithSchema.class, child.data());
        assertInstanceOf(DataSchemaContext.PathMixin.class, child.context());
        assertEquals(1, top.childSizeHint());
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
    void stepThroughChoice() {
        // Adding a child inside a choice relies on this: one step to the choice, then one into the right case
        final var toChoice = assertInstanceOf(Step.Simple.class, topContext().stepTowards(FIRST_LEAF));
        final var choice = assertInstanceOf(DataSchemaContext.PathMixin.class, toChoice.child());
        assertEquals(OUTER, choice.dataSchemaNode().getQName());

        final var intoCase = assertInstanceOf(Step.ThroughCase.class, choice.stepTowards(FIRST_LEAF));
        assertEquals(FIRST, intoCase.inCase().argument());
        assertEquals(FIRST_LEAF, intoCase.child().dataSchemaNode().getQName());

        // The choice's own name leads nowhere
        assertNull(choice.stepTowards(OUTER));
    }

    @Test
    void choiceNameReusesContext() {
        // Asking for a choice by its own name must not replace the choice context cached under its children's names
        final var choice = assertInstanceOf(Step.Simple.class, topContext().stepTowards(FIRST_LEAF)).child();
        assertSame(choice, topContext().childByQName(OUTER));
        assertSame(choice, topContext().childByQName(OUTER));
        assertSame(choice, assertInstanceOf(Step.Simple.class, topContext().stepTowards(FIRST_LEAF)).child());
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
    void inferenceToListInsideChoice() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(TOP);
        stack.enterChoice(OUTER);
        stack.enterSchemaTree(SECOND);
        stack.enterSchemaTree(SECOND_LIST);
        // A list gives the context of its entries
        final var entry = assertInstanceOf(Composite.class, TREE.childByInference(stack.toInference()));
        assertFalse(entry instanceof DataSchemaContext.PathMixin);
        assertEquals(SECOND_LIST, entry.dataSchemaNode().getQName());

        stack.enterSchemaTree(SECOND_LIST_ID);
        final var leaf = TREE.childByInference(stack.toInference());
        assertNotNull(leaf);
        assertEquals(SECOND_LIST_ID, leaf.dataSchemaNode().getQName());
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
    void groupingInferenceIsNotShared() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterGrouping(GRP);
        // A grouping holds no data nodes of its own
        assertNull(TREE.childByInference(stack.toInference()));

        stack.enterDataTree(GRP_CONT);
        final var cont = assertInstanceOf(Composite.class, TREE.childByInference(stack.toInference()));
        assertEquals(GRP_CONT, cont.dataSchemaNode().getQName());
        assertNotSame(cont, TREE.childByInference(stack.toInference()));
        assertNotNull(cont.childByQName(GRP_LEAF));

        stack.enterDataTree(GRP_LEAF);
        final var leaf = TREE.childByInference(stack.toInference());
        assertNotNull(leaf);
        assertEquals(GRP_LEAF, leaf.dataSchemaNode().getQName());
    }

    @Test
    void groupingStartingAtList() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterGrouping(GRP);
        stack.enterDataTree(GRP_LIST);
        final var entry = assertInstanceOf(Composite.class, TREE.childByInference(stack.toInference()));
        assertFalse(entry instanceof DataSchemaContext.PathMixin);
        assertEquals(GRP_LIST, entry.dataSchemaNode().getQName());

        stack.enterDataTree(GRP_LIST_ID);
        final var leaf = TREE.childByInference(stack.toInference());
        assertNotNull(leaf);
        assertEquals(GRP_LIST_ID, leaf.dataSchemaNode().getQName());
    }

    @Test
    void groupingStartingAtAction() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterGrouping(GRP);
        stack.enterSchemaTree(GRP_ACT);
        // An action is not a data node, but data can start there
        final var action = assertInstanceOf(Composite.class, TREE.childByInference(stack.toInference()));
        assertEquals(GRP_ACT, action.dataSchemaNode().getQName());

        stack.enterSchemaTree(INPUT);
        final var input = assertInstanceOf(Composite.class, TREE.childByInference(stack.toInference()));
        assertEquals(INPUT, input.dataSchemaNode().getQName());
        assertNotNull(input.childByQName(GRP_ACT_IN));
    }

    @Test
    void groupingStartingAtNotification() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterGrouping(GRP);
        stack.enterSchemaTree(GRP_NOTIF);
        final var notif = assertInstanceOf(Composite.class, TREE.childByInference(stack.toInference()));
        assertEquals(GRP_NOTIF, notif.dataSchemaNode().getQName());

        stack.enterSchemaTree(GRP_NOTIF_LEAF);
        final var leaf = TREE.childByInference(stack.toInference());
        assertNotNull(leaf);
        assertEquals(GRP_NOTIF_LEAF, leaf.dataSchemaNode().getQName());
    }

    @Test
    void groupingInsideContainer() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterDataTree(TOP);
        stack.enterGrouping(TOP_GRP);
        // Not the context of the container the grouping is declared in
        assertNull(TREE.childByInference(stack.toInference()));

        stack.enterDataTree(TOP_GRP_CONT);
        final var cont = assertInstanceOf(Composite.class, TREE.childByInference(stack.toInference()));
        assertEquals(TOP_GRP_CONT, cont.dataSchemaNode().getQName());
        assertNotNull(cont.childByQName(TOP_GRP_LEAF));
    }

    @Test
    void groupingThroughChoiceAndCase() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterGrouping(GRP);
        stack.enterChoice(GRP_CHOICE);
        assertNull(TREE.childByInference(stack.toInference()));
        stack.enterSchemaTree(GRP_CASE);
        assertNull(TREE.childByInference(stack.toInference()));

        stack.enterSchemaTree(GRP_CASE_LEAF);
        final var leaf = TREE.childByInference(stack.toInference());
        assertNotNull(leaf);
        assertEquals(GRP_CASE_LEAF, leaf.dataSchemaNode().getQName());
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
