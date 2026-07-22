/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema.ChildReusePolicy;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Caches child lookups for one parent schema node, so that a JSON object with many elements scans that parent's
 * children once instead of once per element. A schema node's children never change, so a cached answer stays correct
 * for as long as the node exists.
 *
 * <p>The parent node is held in a {@link WeakReference}, because {@link SchemaLookupCache} uses that same node as a
 * weak cache key: a normal (strong) reference from here would keep the node -- and therefore the cache entry -- alive
 * forever. That reference is only read when a lookup misses and has to be computed, so callers have to hold on to the
 * parent node for as long as they use this object.
 */
final class SchemaNodeLookup {
    /**
     * Identifies a child by the two things a JSON element name gives us: local name and namespace.
     */
    private record ChildLookupKey(String localName, XMLNamespace namespace) {
        ChildLookupKey {
            requireNonNull(localName);
            requireNonNull(namespace);
        }
    }

    // Only successful lookups go in: element names come from the JSON document, so caching misses would let a crafted
    // input grow these maps without bound. Concurrent because one shared JSONCodecFactory serves parsers on many
    // threads.
    private final ConcurrentHashMap<ChildLookupKey, ImmutableList<DataSchemaNode>> resolvedPaths =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ImmutableSet<XMLNamespace>> namespacesByName = new ConcurrentHashMap<>();
    private final WeakReference<DataSchemaNode> parent;

    SchemaNodeLookup(final DataSchemaNode parent) {
        this.parent = new WeakReference<>(requireNonNull(parent));
    }

    /**
     * Cached version of {@link ParserStreamUtils#findSchemaNodeByNameAndNamespace(DataSchemaNode, String,
     * XMLNamespace)} against the parent node this lookup describes.
     *
     * <p>The returned {@link Deque} is always a fresh copy, because
     * {@link CompositeNodeDataWithSchema#addChild(Deque, ChildReusePolicy)} empties the one it is handed.
     *
     * @param localName child local name
     * @param namespace child namespace
     * @return the path down to the child: {@code [child]} normally, or {@code [choice, case, child]} when it sits
     *         inside a choice. Empty if the parent has no such child.
     */
    Deque<DataSchemaNode> findSchemaNode(final String localName, final XMLNamespace namespace) {
        final var key = new ChildLookupKey(localName, namespace);
        final var cached = resolvedPaths.get(key);
        if (cached != null) {
            return new ArrayDeque<>(cached);
        }

        final var found = ParserStreamUtils.findSchemaNodeByNameAndNamespace(parent(), localName, namespace);
        if (!found.isEmpty()) {
            // Another thread may have won the race; its value is equal to ours, and each caller gets its own copy.
            resolvedPaths.putIfAbsent(key, ImmutableList.copyOf(found));
        }

        return found;
    }

    /**
     * Finds the namespaces in which the parent node this lookup describes has a child called {@code elementName}.
     * RFC 7951 lets an element name omit its module prefix, and this is how the namespace is inferred when it does.
     *
     * @param elementName element name without a module prefix
     * @return the namespaces holding such a child, empty if there is none
     */
    ImmutableSet<XMLNamespace> potentialNamespaces(final String elementName) {
        final var cached = namespacesByName.get(elementName);
        if (cached != null) {
            return cached;
        }

        final var computed = computePotentialNamespaces(parent(), elementName);
        if (computed.isEmpty()) {
            return computed;
        }

        // Another thread may have won the race; use its set so everybody shares one instance.
        final var raced = namespacesByName.putIfAbsent(elementName, computed);
        return raced != null ? raced : computed;
    }

    // Both counts exist so that SchemaNodeLookupTest can verify that failed lookups are not cached.
    @VisibleForTesting
    int cachedPathCount() {
        return resolvedPaths.size();
    }

    @VisibleForTesting
    int cachedNamespaceCount() {
        return namespacesByName.size();
    }

    private DataSchemaNode parent() {
        return verifyNotNull(parent.get(), "Parent schema node has already been garbage collected");
    }

    private static ImmutableSet<XMLNamespace> computePotentialNamespaces(final DataSchemaNode dataSchemaNode,
            final String elementName) {
        if (!(dataSchemaNode instanceof DataNodeContainer container)) {
            // Anything that is not a DataNodeContainer (a leaf, leaf-list, anyxml, anydata or choice) has no
            // children to search. The cases of a choice do get searched, just from collectPotentialNamespaces() below.
            return ImmutableSet.of();
        }

        final var potentialUris = new HashSet<XMLNamespace>();
        collectPotentialNamespaces(potentialUris, container, elementName);
        return ImmutableSet.copyOf(potentialUris);
    }

    private static void collectPotentialNamespaces(final Set<XMLNamespace> potentialUris,
            final DataNodeContainer container, final String elementName) {
        for (var childSchemaNode : container.getChildNodes()) {
            if (childSchemaNode instanceof ChoiceSchemaNode choice) {
                // A choice does not appear in JSON at all: the children of its cases show up directly inside the
                // parent object. A choice therefore never matches by name -- look inside its cases instead.
                //
                // Recursing right here is fine because we collect every matching namespace and the order we find them
                // in does not matter. ParserStreamUtils.findSchemaNodeByNameAndNamespace() cannot do the same: it
                // returns the first match, so it has to scan all direct children before descending into any choice.
                for (var concreteCase : choice.getCases()) {
                    collectPotentialNamespaces(potentialUris, concreteCase, elementName);
                }
            } else if (childSchemaNode.getQName().getLocalName().equals(elementName)) {
                potentialUris.add(childSchemaNode.getQName().getNamespace());
            }
        }
    }
}
