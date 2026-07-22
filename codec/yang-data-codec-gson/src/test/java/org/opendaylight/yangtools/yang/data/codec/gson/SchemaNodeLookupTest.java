/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Tests for {@link SchemaNodeLookup}, {@link SchemaLookupCache} and the way {@link JSONCodecFactory} hands that cache
 * around. These cover the contracts {@link JsonParserStream} relies on but cannot check for itself.
 */
class SchemaNodeLookupTest {
    private static final QName TOP = QName.create("yt1899", "top");
    private static final QName FOO = QName.create(TOP, "foo");
    private static final QName BAR = QName.create(TOP, "bar");
    private static final QName BAZ = QName.create(TOP, "baz");
    private static final QName BAZ_LEAF = QName.create(TOP, "baz-leaf");
    private static final XMLNamespace NAMESPACE = TOP.getNamespace();

    private static final EffectiveModelContext MODEL_CONTEXT =
        YangParserTestUtils.parseYangResourceDirectory("/yt1899/yang");
    private static final ListSchemaNode FOO_LIST = fooList();

    // Constructed directly rather than through SchemaLookupCache, so that each test method starts with an empty cache.
    private final SchemaNodeLookup lookup = new SchemaNodeLookup(FOO_LIST);

    @Test
    void findSchemaNodeReturnsTheChoicePath() {
        assertEquals(List.of(BAR, BAZ, BAZ_LEAF), qnames(lookup.findSchemaNode("baz-leaf", NAMESPACE)));
    }

    @Test
    void findSchemaNodeHandsBackAFreshDeque() {
        // CompositeNodeDataWithSchema.addChild() drains the deque it is given, so the next caller must not see the
        // leftovers of the previous one.
        final var first = lookup.findSchemaNode("baz-leaf", NAMESPACE);
        first.clear();

        final var second = lookup.findSchemaNode("baz-leaf", NAMESPACE);
        assertNotSame(first, second);
        assertEquals(List.of(BAR, BAZ, BAZ_LEAF), qnames(second));
    }

    @Test
    void unknownChildIsNotCached() {
        // Keys come straight from JSON input, so caching misses would let a crafted document grow the cache without
        // bound.
        assertTrue(lookup.findSchemaNode("no-such-leaf", NAMESPACE).isEmpty());
        assertTrue(lookup.findSchemaNode("no-such-leaf", NAMESPACE).isEmpty());
        assertEquals(0, lookup.cachedPathCount());

        assertTrue(lookup.potentialNamespaces("no-such-leaf").isEmpty());
        assertTrue(lookup.potentialNamespaces("no-such-leaf").isEmpty());
        assertEquals(0, lookup.cachedNamespaceCount());

        // A child which does exist, on the other hand, is cached.
        assertFalse(lookup.findSchemaNode("baz-leaf", NAMESPACE).isEmpty());
        assertEquals(1, lookup.cachedPathCount());
        assertFalse(lookup.potentialNamespaces("baz-leaf").isEmpty());
        assertEquals(1, lookup.cachedNamespaceCount());
    }

    @Test
    void nonContainerParentHasNoChildren() {
        // A malformed document can drive JsonParserStream into a JSON object nested under a leaf.
        final var leafLookup = new SchemaNodeLookup(
            assertInstanceOf(LeafSchemaNode.class, FOO_LIST.dataChildByName(QName.create(TOP, "name"))));

        assertTrue(leafLookup.findSchemaNode("baz-leaf", NAMESPACE).isEmpty());
        assertTrue(leafLookup.potentialNamespaces("baz-leaf").isEmpty());
        assertEquals(0, leafLookup.cachedPathCount());
        assertEquals(0, leafLookup.cachedNamespaceCount());
    }

    @Test
    void potentialNamespacesLooksThroughChoices() {
        final var first = lookup.potentialNamespaces("baz-leaf");
        assertEquals(List.of(NAMESPACE), first.asList());
        // Everyone shares the single cached set.
        assertSame(first, lookup.potentialNamespaces("baz-leaf"));
    }

    @Test
    void lookupsArePerSchemaNode() {
        final var cache = new SchemaLookupCache();

        final var fooLookup = cache.lookupFor(FOO_LIST);
        assertSame(fooLookup, cache.lookupFor(FOO_LIST));
        assertNotSame(fooLookup, cache.lookupFor(MODEL_CONTEXT));
    }

    @Test
    void rebasedFactoryGetsItsOwnCache() {
        // A rebased factory is bound to another model context -- a mount point's or an anydata's. Handing it this
        // factory's cache would let one cache hold schema nodes of two model contexts, so it creates an empty one.
        final var factory = JSONCodecFactorySupplier.RFC7951.createLazy(MODEL_CONTEXT);
        // The cache holds its lookups through soft references, so keep a strong one until the assertions below run.
        final var warmed = factory.schemaLookups().lookupFor(FOO_LIST);

        final var rebased = factory.rebaseTo(MODEL_CONTEXT);
        assertNotSame(factory.schemaLookups(), rebased.schemaLookups());
        assertNotSame(warmed, rebased.schemaLookups().lookupFor(FOO_LIST));
    }

    private static List<QName> qnames(final Collection<DataSchemaNode> nodes) {
        return nodes.stream().map(DataSchemaNode::getQName).toList();
    }

    private static ListSchemaNode fooList() {
        final var top = assertInstanceOf(ContainerSchemaNode.class, MODEL_CONTEXT.dataChildByName(TOP));
        return assertInstanceOf(ListSchemaNode.class, top.dataChildByName(FOO));
    }
}
