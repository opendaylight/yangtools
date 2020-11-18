/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.UniqueConstraintException;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link AbstractValidation} which ensures a particular {@code list} node complies with its {@code unique}
 * constraints.
 */
final class UniqueValidation extends AbstractValidation {
    /**
     * A validator for a single {@code unique} constraint. This class is further specialized for single- and
     * multiple-constraint implementations.
     *
     * <p>
     * The basic idea is that for each list entry there is a corresponding value vector of one or more values, each
     * corresponding to one component of the {@code unique} constraint.
     */
    private abstract static class UniqueValidator<T> implements Immutable {
        final @NonNull T descendants;

        UniqueValidator(final T descendants) {
            this.descendants = requireNonNull(descendants);
        }

        static UniqueValidator<?> of(final List<List<NodeIdentifier>> descendants) {
            return descendants.size() == 1 ? new SingleUniqueValidator(descendants.get(0))
                : new MultiUniqueValidator(descendants);
        }

        /**
         * Extract a value vector from a particular child.
         *
         * @param valueCache Cache of descendants already looked up
         * @param data Root data node
         * @return Value vector
         */
        abstract @Nullable Object extractValues(Map<List<NodeIdentifier>, Object> valueCache,
            DataContainerNode<?> data);

        // FIXME: document this
        // FIXME: format values to value vector
        abstract Map<Descendant, @Nullable Object> indexValues(Object values);

        /**
         * Encode a path for storage. Single-element paths are squashed to their only element. The inverse operation is
         * {@link #decodePath(Object)}.
         *
         * @param path Path to encode
         * @return Encoded path.
         */
        static final Object encodePath(final List<NodeIdentifier> path) {
            return path.size() == 1 ? path.get(0) : ImmutableList.copyOf(path);
        }

        /**
         * Decode a path from storage. This is the inverse operation to {@link #encodePath(List)}.
         *
         * @param obj Encoded path
         * @return Decoded path
         */
        static final @NonNull ImmutableList<NodeIdentifier> decodePath(final Object obj) {
            return obj instanceof NodeIdentifier ? ImmutableList.of((NodeIdentifier) obj)
                : (ImmutableList<NodeIdentifier>) obj;
        }

        static final @NonNull Descendant decodeDescendant(final Object obj) {
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
        static final @Nullable Object extractValue(final Map<List<NodeIdentifier>, Object> valueCache,
                final DataContainerNode<?> data, final List<NodeIdentifier> path) {
            return valueCache.computeIfAbsent(path, key -> extractValue(data, key));
        }

        /**
         * Extract the value for a single descendant.
         *
         * @param data Root data node
         * @param path Descendant path
         * @return Value for the descendant
         */
        private static @Nullable Object extractValue(final DataContainerNode<?> data, final List<NodeIdentifier> path) {
            DataContainerNode<?> current = data;
            final Iterator<NodeIdentifier> it = path.iterator();
            while (true) {
                final NodeIdentifier step = it.next();
                final Optional<DataContainerChild<?, ?>> optNext = current.getChild(step);
                if (optNext.isEmpty()) {
                    return null;
                }

                final DataContainerChild<?, ?> next = optNext.orElseThrow();
                if (!it.hasNext()) {
                    checkState(next instanceof LeafNode, "Unexpected node %s at %s", next, path);
                    final Object value = next.getValue();
                    LOG.trace("Resolved {} to value {}", path, value);
                    return value;
                }

                checkState(next instanceof DataContainerNode, "Unexpected node %s in %s", next, path);
                current = (DataContainerNode<?>) next;
            }
        }

        @Override
        public final String toString() {
            return MoreObjects.toStringHelper(this).add("paths", descendants).toString();
        }
    }

    private static final class SingleUniqueValidator extends UniqueValidator<Object> {
        SingleUniqueValidator(final List<NodeIdentifier> path) {
            super(encodePath(path));
        }

        @Override
        Object extractValues(final Map<List<NodeIdentifier>, Object> valueCache, final DataContainerNode<?> data) {
            return extractValue(valueCache, data, decodePath(descendants));
        }

        @Override
        Map<Descendant, @Nullable Object> indexValues(final Object values) {
            return Collections.singletonMap(decodeDescendant(descendants), values);
        }
    }

    private static final class MultiUniqueValidator extends UniqueValidator<Set<Object>> {
        MultiUniqueValidator(final List<List<NodeIdentifier>> descendantPaths) {
            super(descendantPaths.stream().map(UniqueValidator::encodePath).collect(ImmutableSet.toImmutableSet()));
        }

        @Override
        List<Object> extractValues(final Map<List<NodeIdentifier>, Object> valueCache,
                final DataContainerNode<?> data) {
            return descendants.stream()
                .map(UniqueValidator::decodePath)
                .map(path -> extractValue(valueCache, data, path))
                // FIXME: we want hashCode() caching in this list
                .collect(Collectors.toList());
        }

        @Override
        Map<Descendant, @Nullable Object> indexValues(final Object values) {
            final Map<Descendant, @Nullable Object> index = Maps.newHashMapWithExpectedSize(descendants.size());
            final Iterator<?> it = ((List<?>) values).iterator();
            for (Object obj : descendants) {
                verify(index.put(decodeDescendant(obj), it.next()) == null);
            }
            return index;
        }
    }

    @FunctionalInterface
    @NonNullByDefault
    interface ExceptionSupplier<T extends Exception> {
        T get(String message, UniqueValidator<?> validator, @Nullable Object values);
    }

    private static final Logger LOG = LoggerFactory.getLogger(UniqueValidation.class);

    private final @NonNull ImmutableList<UniqueValidator<?>> validators;

    private UniqueValidation(final ModificationApplyOperation delegate, final List<UniqueValidator<?>> validators) {
        super(delegate);
        this.validators = ImmutableList.copyOf(validators);
    }

    static ModificationApplyOperation of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig,
            final ModificationApplyOperation delegate) {
        final Collection<? extends @NonNull UniqueEffectiveStatement> uniques = schema.getUniqueConstraints();
        if (!treeConfig.isUniqueIndexEnabled() || uniques.isEmpty()) {
            return delegate;
        }

        final Stopwatch sw = Stopwatch.createStarted();
        final Map<Descendant, List<NodeIdentifier>> paths = new HashMap<>();
        final List<UniqueValidator<?>> validators = uniques.stream()
            .map(unique -> UniqueValidator.of(unique.argument().stream()
                .map(descendant -> paths.computeIfAbsent(descendant, key -> toDescendantPath(schema, key)))
                .collect(ImmutableList.toImmutableList())))
            .collect(ImmutableList.toImmutableList());
        LOG.debug("Constructed {} validators in {}", validators.size(), sw);

        return validators.isEmpty() ? delegate : new UniqueValidation(delegate, validators);
    }

    @Override
    void enforceOnData(final NormalizedNode<?, ?> data) {
        enforceOnData(data, (message, validator, values) -> new IllegalArgumentException(message));
    }

    @Override
    void enforceOnData(final ModificationPath path, final NormalizedNode<?, ?> data)
            throws UniqueConstraintException {
        enforceOnData(data, (message, validator, values) -> new UniqueConstraintException(path.toInstanceIdentifier(),
            validator.indexValues(values), message));
    }

    private <T extends @NonNull Exception> void enforceOnData(final NormalizedNode<?, ?> data,
            final ExceptionSupplier<T> exceptionSupplier) throws T {
        final Stopwatch sw = Stopwatch.createStarted();
        verify(data instanceof NormalizedNodeContainer, "Unexpected data %s", data);
        final var children = ((NormalizedNodeContainer<?, ?, ?>) data).getValue();
        final var collected = HashMultimap.<UniqueValidator<?>, Object>create(validators.size(), children.size());
        for (NormalizedNode<?, ?> child : children) {
            verify(child instanceof DataContainerNode, "Unexpected child %s", child);
            final DataContainerNode<?> cont = (DataContainerNode<?>) child;

            final Map<List<NodeIdentifier>, Object> valueCache = new HashMap<>();
            for (UniqueValidator<?> validator : validators) {
                final Object values = validator.extractValues(valueCache, cont);
                if (!collected.put(validator, values)) {
                    throw exceptionSupplier.get("Unique constraint violation on " + values, validator, values);
                }
            }
        }

        LOG.trace("Enforced {} validators in {}", validators.size(), sw);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("validators", validators));
    }

    private static ImmutableList<NodeIdentifier> toDescendantPath(final ListSchemaNode parent,
            final Descendant descendant) {
        final List<QName> qnames = descendant.getNodeIdentifiers();
        final ImmutableList.Builder<NodeIdentifier> builder = ImmutableList.builderWithExpectedSize(qnames.size());
        final Iterator<QName> it = descendant.getNodeIdentifiers().iterator();
        DataNodeContainer current = parent;
        while (true) {
            final QName qname = it.next();
            final DataSchemaNode next = current.findDataChildByName(qname)
                .orElseThrow(() -> new IllegalStateException("Cannot find component " + qname + " of " + descendant));
            builder.add(NodeIdentifier.create(qname));
            if (!it.hasNext()) {
                checkState(next instanceof TypedDataSchemaNode, "Unexpected schema %s for %s", next, descendant);
                final ImmutableList<NodeIdentifier> ret = builder.build();
                LOG.trace("Resolved {} to {}", descendant, ret);
                return ret;
            }

            checkState(next instanceof DataNodeContainer, "Unexpected non-container %s for %s", next, descendant);
            current = (DataNodeContainer) next;
        }
    }
}
