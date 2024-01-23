/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.VerifyException;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A validator for a single {@code unique} constraint. This class is further specialized for single- and
 * multiple-constraint implementations.
 *
 * <p>
 * The basic idea is that for each list entry there is a corresponding value vector of one or more values, each
 * corresponding to one component of the {@code unique} constraint.
 */
abstract class UniqueValidator<T> implements Immutable {
    private static final class One extends UniqueValidator<Object> {
        One(final List<NodeIdentifier> path) {
            super(encodePath(path));
        }

        @Override
        Object extractValues(final Map<List<NodeIdentifier>, Object> valueCache, final DataContainerNode data) {
            return extractValue(valueCache, data, decodePath(descendants));
        }

        @Override
        Map<Descendant, @Nullable Object> indexValues(final Object values) {
            return Collections.singletonMap(decodeDescendant(descendants), values);
        }
    }

    private static final class Many extends UniqueValidator<Set<Object>> {
        Many(final List<List<NodeIdentifier>> descendantPaths) {
            super(descendantPaths.stream().map(UniqueValidator::encodePath).collect(ImmutableSet.toImmutableSet()));
        }

        @Override
        UniqueValues extractValues(final Map<List<NodeIdentifier>, Object> valueCache, final DataContainerNode data) {
            return descendants.stream()
                .map(obj -> extractValue(valueCache, data, decodePath(obj)))
                .collect(UniqueValues.COLLECTOR);
        }

        @Override
        Map<Descendant, @Nullable Object> indexValues(final Object values) {
            final var index = Maps.<Descendant, @Nullable Object>newHashMapWithExpectedSize(descendants.size());
            final var it = ((UniqueValues) values).iterator();
            for (var obj : descendants) {
                final var prev = index.put(decodeDescendant(obj), it.next());
                if (prev != null) {
                    throw new VerifyException("Unexpected collision with " + prev);
                }
            }
            return index;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(UniqueValidator.class);

    final @NonNull T descendants;

    UniqueValidator(final T descendants) {
        this.descendants = requireNonNull(descendants);
    }

    static UniqueValidator<?> of(final List<List<NodeIdentifier>> descendants) {
        return descendants.size() == 1 ? new One(descendants.get(0)) : new Many(descendants);
    }

    /**
     * Extract a value vector from a particular child.
     *
     * @param valueCache Cache of descendants already looked up
     * @param data Root data node
     * @return Value vector
     */
    abstract @Nullable Object extractValues(Map<List<NodeIdentifier>, Object> valueCache, DataContainerNode data);

    /**
     * Index a value vector by associating each value with its corresponding {@link Descendant}.
     *
     * @param values Value vector
     * @return Map of Descandant/value relations
     */
    abstract Map<Descendant, @Nullable Object> indexValues(Object values);

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("paths", descendants).toString();
    }

    /**
     * Encode a path for storage. Single-element paths are squashed to their only element. The inverse operation is
     * {@link #decodePath(Object)}.
     *
     * @param path Path to encode
     * @return Encoded path.
     */
    private static Object encodePath(final List<NodeIdentifier> path) {
        return path.size() == 1 ? path.get(0) : ImmutableList.copyOf(path);
    }

    /**
     * Decode a path from storage. This is the inverse operation to {@link #encodePath(List)}.
     *
     * @param obj Encoded path
     * @return Decoded path
     */
    private static @NonNull ImmutableList<NodeIdentifier> decodePath(final Object obj) {
        return obj instanceof NodeIdentifier nid ? ImmutableList.of(nid) : (ImmutableList<NodeIdentifier>) obj;
    }

    private static @NonNull Descendant decodeDescendant(final Object obj) {
        return Descendant.of(Collections2.transform(decodePath(obj), NodeIdentifier::getNodeType));
    }

    /**
     * Extract the value for a single descendant.
     *
     * @param valueCache Cache of descendants already looked up
     * @param data Root data node
     * @param path Descendant path
     * @return Value for the descendant
     */
    private static @Nullable Object extractValue(final Map<List<NodeIdentifier>, Object> valueCache,
            final DataContainerNode data, final List<NodeIdentifier> path) {
        return valueCache.computeIfAbsent(path, key -> extractValue(data, key));
    }

    /**
     * Extract the value for a single descendant.
     *
     * @param data Root data node
     * @param path Descendant path
     * @return Value for the descendant
     */
    private static @Nullable Object extractValue(final DataContainerNode data, final List<NodeIdentifier> path) {
        var current = data;
        final var it = path.iterator();
        while (true) {
            final var step = it.next();
            final var next = current.childByArg(step);
            if (next == null) {
                return null;
            }

            if (!it.hasNext()) {
                if (next instanceof LeafNode<?> leaf) {
                    final var value = leaf.body();
                    LOG.trace("Resolved {} to value {}", path, value);
                    return value;
                }
                throw new IllegalStateException("Unexpected non-leaf node " + next + " at " + path);
            }

            if (!(next instanceof DataContainerNode nextContainer)) {
                throw new IllegalStateException("Unexpected non-container node " + next + " at " + path);
            }
            current = nextContainer;
        }
    }
}
