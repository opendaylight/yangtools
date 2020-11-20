/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.UniqueConstraintException;
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
    private static final Logger LOG = LoggerFactory.getLogger(UniqueValidation.class);

    private final @NonNull ImmutableList<UniqueValidator<?>> validators;

    private UniqueValidation(final ModificationApplyOperation delegate,
            final ImmutableList<UniqueValidator<?>> validators) {
        super(delegate);
        this.validators = requireNonNull(validators);
    }

    static ImmutableList<UniqueValidator<?>> validatorsOf(final ListSchemaNode schema,
            final DataTreeConfiguration treeConfig) {
        final Collection<? extends @NonNull UniqueEffectiveStatement> uniques = schema.getUniqueConstraints();
        if (!treeConfig.isUniqueIndexEnabled() || uniques.isEmpty()) {
            return ImmutableList.of();
        }

        final Stopwatch sw = Stopwatch.createStarted();
        final Map<Descendant, List<NodeIdentifier>> paths = new HashMap<>();
        final ImmutableList<UniqueValidator<?>> validators = uniques.stream()
            .map(unique -> UniqueValidator.of(unique.argument().stream()
                .map(descendant -> paths.computeIfAbsent(descendant, key -> toDescendantPath(schema, key)))
                .collect(ImmutableList.toImmutableList())))
            .collect(ImmutableList.toImmutableList());
        LOG.debug("Constructed {} validators in {}", validators.size(), sw);

        return validators;
    }

    @Override
    void enforceOnData(final NormalizedNode data) {
        enforceOnData(data, (message, values) -> new UniqueValidationFailedException(message));
    }

    @Override
    void enforceOnData(final ModificationPath path, final NormalizedNode data) throws UniqueConstraintException {
        enforceOnData(data, (message, values) -> new UniqueConstraintException(path.toInstanceIdentifier(), values,
            message));
    }

    private <T extends @NonNull Exception> void enforceOnData(final NormalizedNode data,
            final ExceptionSupplier<T> exceptionSupplier) throws T {
        final Stopwatch sw = Stopwatch.createStarted();
        verify(data instanceof NormalizedNodeContainer, "Unexpected data %s", data);
        final var children = ((NormalizedNodeContainer<?>) data).body();
        final var collected = HashMultimap.<UniqueValidator<?>, Object>create(validators.size(), children.size());
        for (NormalizedNode child : children) {
            verify(child instanceof DataContainerNode, "Unexpected child %s", child);
            final DataContainerNode cont = (DataContainerNode) child;

            final Map<List<NodeIdentifier>, Object> valueCache = new HashMap<>();
            for (UniqueValidator<?> validator : validators) {
                final Object values = validator.extractValues(valueCache, cont);
                final Object masked = BinaryValue.wrap(values);
                if (!collected.put(validator, masked)) {
                    final Map<Descendant, @Nullable Object> index = validator.indexValues(values);
                    throw exceptionSupplier.get(cont.getIdentifier()
                        + " violates unique constraint on " + masked + " of " + index.keySet(), index);
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

    @FunctionalInterface
    @NonNullByDefault
    interface ExceptionSupplier<T extends Exception> {
        T get(String message, Map<Descendant, @Nullable Object> values);
    }
}
