/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;

final class UniqueValidation extends AbstractValidation {
    private abstract static class UniqueValidator implements Immutable {
        final @NonNull Object descendants;

        UniqueValidator(final Object descendants) {
            this.descendants = requireNonNull(descendants);
        }

        static UniqueValidator of(final List<List<PathArgument>> descendants) {
            return descendants.size() == 1 ? new SingleUniqueValidator(descendants.get(0))
                : new RegularUniqueValidator(descendants);
        }

        static final Object encodePath(final List<PathArgument> path) {
            return path.size() == 1 ? path.get(0) : ImmutableList.copyOf(path);
        }

        static final @NonNull List<PathArgument> decodePath(final Object obj) {
            return obj instanceof PathArgument ? List.of((PathArgument) obj) : (List<PathArgument>) obj;
        }

        static final Object extractValue(final Map<List<PathArgument>, Object> valueCache,
                final NormalizedNode<?, ?> data, final List<PathArgument> descendant) {
            return valueCache.computeIfAbsent(descendant, key -> extractValue(data, key));
        }

        private static Object extractValue(final NormalizedNode<?, ?> data, final List<PathArgument> descendant) {
            throw new UnsupportedOperationException();
        }

        abstract Object extractValues(Map<List<PathArgument>, Object> valueCache, NormalizedNode<?, ?> data);

        @Override
        public final String toString() {
            return MoreObjects.toStringHelper(this).add("paths", descendants).toString();
        }
    }

    private static final class SingleUniqueValidator extends UniqueValidator {
        SingleUniqueValidator(final List<PathArgument> path) {
            super(encodePath(path));
        }

        @Override
        Object extractValues(final Map<List<PathArgument>, Object> valueCache, final NormalizedNode<?, ?> data) {
            return extractValue(valueCache, data, decodePath(descendants));
        }
    }

    private static final class RegularUniqueValidator extends UniqueValidator {
        RegularUniqueValidator(final List<List<PathArgument>> descendantPaths) {
            super(descendantPaths.stream()
                .map(path -> path.size() == 1 ? path.get(0) : ImmutableList.copyOf(path))
                .collect(ImmutableSet.toImmutableSet()));
        }

        @Override
        List<Object> extractValues(final Map<List<PathArgument>, Object> valueCache, final NormalizedNode<?, ?> data) {
            return ((Set<Object>) descendants).stream()
                .map(UniqueValidator::decodePath)
                .map(path -> extractValue(valueCache, data, path))
                .collect(Collectors.toList());
        }
    }

    private final @NonNull ImmutableList<UniqueValidator> validators;

    private UniqueValidation(final ModificationApplyOperation delegate, final List<UniqueValidator> validators) {
        super(delegate);
        this.validators = ImmutableList.copyOf(validators);
    }

    static ModificationApplyOperation of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig,
            final ModificationApplyOperation delegate) {
        final Collection<? extends @NonNull UniqueEffectiveStatement> uniques = schema.getUniqueConstraints();
        if (!treeConfig.isUniqueIndexEnabled() || uniques.isEmpty()) {
            return delegate;
        }

        final Map<Descendant, List<PathArgument>> descendantPaths = new HashMap<>();
        final List<UniqueValidator> validators = uniques.stream()
            .map(UniqueEffectiveStatement::argument)
            .map(descendants -> descendants.stream()
                .map(descendant -> descendantPaths.computeIfAbsent(descendant, key -> toDescendantPath(schema, key)))
                .collect(ImmutableList.toImmutableList()))
            .map(UniqueValidator::of)
            .collect(ImmutableList.toImmutableList());

        return validators.isEmpty() ? delegate : new UniqueValidation(delegate, validators);
    }

    @Override
    Optional<? extends TreeNode> apply(final ModifiedNode modification, final Optional<? extends TreeNode> storeMeta,
            final Version version) {
        final Optional<? extends TreeNode> ret = delegate().apply(modification, storeMeta, version);
        ret.ifPresent(node ->  checkConstraints(node.getData()));
        return ret;
    }

    @Override
    void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<? extends TreeNode> current, final Version version) throws DataValidationFailedException {
        delegate().checkApplicable(path, modification, current, version);
    }

    @Override
    void fullVerifyStructure(final NormalizedNode<?, ?> modification) {
        delegate().fullVerifyStructure(modification);
        checkConstraints(modification);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("validators", validators));
    }

    private void checkConstraints(final NormalizedNode<?, ?> data) {
        try {
            validateConstraints(data);
        } catch (DataValidationFailedException e) {
            throw new IllegalArgumentException("Unique constraint violation", e);
        }
    }

    private void validateConstraints(final NormalizedNode<?, ?> data) throws DataValidationFailedException {
        verify(data instanceof NormalizedNodeContainer, "Unexpected data %s", data);
        final var children = ((NormalizedNodeContainer<?, ?, ?>) data).getValue();
        final var collected = HashMultimap.<UniqueValidator, Object>create(validators.size(), children.size());
        for (NormalizedNode<?, ?> child : children) {
            final Map<List<PathArgument>, Object> valueCache = new HashMap<>();
            for (UniqueValidator validator : validators) {
                final Object values = validator.extractValues(valueCache, child);
                DataValidationException.checkLegalData(!collected.put(validator, values),
                    "Validator %s found duplicate vector %s", validator, values);
            }
        }
    }

    private static List<PathArgument> toDescendantPath(final ListSchemaNode parent, final Descendant descendant) {
        // FIXME: perform something
       throw new UnsupportedOperationException();
    }
}
