/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Stopwatch;
import com.google.common.base.VerifyException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.UniqueConstraintException;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
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

    static ModificationApplyOperation of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig,
            final ModificationApplyOperation delegate) {
        final ImmutableList<UniqueValidator<?>> validators = validatorsOf(schema, treeConfig);
        return validators.isEmpty() ? delegate : new UniqueValidation(delegate, validators);
    }

    static ImmutableList<UniqueValidator<?>> validatorsOf(final ListSchemaNode schema,
            final DataTreeConfiguration treeConfig) {
        final var uniques = schema.getUniqueConstraints();
        if (!treeConfig.isUniqueIndexEnabled() || uniques.isEmpty()) {
            return ImmutableList.of();
        }

        final var sw = Stopwatch.createStarted();
        final var paths = new HashMap<Descendant, List<NodeIdentifier>>();
        final var validators = uniques.stream()
            .map(unique -> UniqueValidator.of(unique.argument().stream()
                .map(descendant -> paths.computeIfAbsent(descendant, key -> toDescendantPath(schema, key)))
                .collect(ImmutableList.toImmutableList())))
            .collect(ImmutableList.<UniqueValidator<?>>toImmutableList());
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
        final var sw = Stopwatch.createStarted();
        if (!(data instanceof NormalizedNodeContainer<?> dataContainer)) {
            throw new VerifyException("Unexpected data " + data.prettyTree());
        }

        final var children = dataContainer.body();
        final var collected = HashMultimap.<UniqueValidator<?>, Object>create(validators.size(), children.size());
        for (var child : children) {
            if (!(child instanceof DataContainerNode cont)) {
                throw new VerifyException("Unexpected child " + child);
            }

            final var valueCache = new HashMap<List<NodeIdentifier>, Object>();
            for (var validator : validators) {
                final Object values = validator.extractValues(valueCache, cont);
                final Object masked = BinaryValue.wrap(values);
                if (!collected.put(validator, masked)) {
                    final var index = validator.indexValues(values);
                    throw exceptionSupplier.get(cont.name()
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
        final var qnames = descendant.getNodeIdentifiers();
        final var builder = ImmutableList.<NodeIdentifier>builderWithExpectedSize(qnames.size());
        final var it = descendant.getNodeIdentifiers().iterator();
        DataNodeContainer current = parent;
        while (true) {
            final var qname = it.next();
            final var next = current.findDataChildByName(qname)
                .orElseThrow(() -> new IllegalStateException("Cannot find component " + qname + " of " + descendant));
            builder.add(NodeIdentifier.create(qname));
            if (!it.hasNext()) {
                checkState(next instanceof TypedDataSchemaNode, "Unexpected schema %s for %s", next, descendant);
                final var ret = builder.build();
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
