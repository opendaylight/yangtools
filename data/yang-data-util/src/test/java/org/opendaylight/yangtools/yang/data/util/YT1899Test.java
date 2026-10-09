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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema.ChildReusePolicy;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Tests for looking up children through {@link DataSchemaContext}s, as parsers do: adding a child with
 * {@link CompositeNodeDataWithSchema#addChild(Composite, QName, ChildReusePolicy)}.
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
