/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;

/**
 * Lookup context for dealing with namespace translation during execution of {@link AbstractCompositeGenerator}'s
 * createInternalRuntimeType(). It tracks which namespaces should be translated on account of crossing into source
 * {@code grouping} statement.
 */
final class ChildLookup implements Immutable {
    private final ImmutableSet<AugmentEffectiveStatement> validUsesAugments;
    private final ImmutableSet<QNameModule> squashNamespaces;
    private final QNameModule localNamespace;

    private ChildLookup(final ImmutableSet<AugmentEffectiveStatement> validUsesAugments,
            final ImmutableSet<QNameModule> squashNamespaces, final QNameModule localNamespace) {
        this.validUsesAugments = requireNonNull(validUsesAugments);
        this.squashNamespaces = requireNonNull(squashNamespaces);
        this.localNamespace = localNamespace;
        verify(localNamespace == null == squashNamespaces.isEmpty(), "Unexpected lookup state %s", this);
    }

    public static @NonNull ChildLookup of(final EffectiveStatement<?, ?> statement) {
        return new ChildLookup(streamUsesAugments(statement).collect(ImmutableSet.toImmutableSet()), ImmutableSet.of(),
            null);
    }

    @NonNull QName adjustQName(final @NonNull QName qname) {
        return squashNamespaces.contains(qname.getModule()) ? qname.bindTo(verifyNotNull(localNamespace)) : qname;
    }

    boolean contains(final AugmentEffectiveStatement augment) {
        return validUsesAugments.contains(augment);
    }

    @NonNull ChildLookup inStatement(final EffectiveStatement<?, ?> statememt) {
        return hasUsesAugments(statememt)
            ? new ChildLookup(concatUsesAugments(statememt), squashNamespaces, localNamespace) : this;
    }

    @NonNull ChildLookup inGrouping(final QName qname, final GroupingGenerator grouping) {
        final var statement = grouping.statement();
        final var grpNamespace = statement.argument().getModule();
        final var itemNamespace = qname.getModule();

        final ImmutableSet<QNameModule> newSquashNamespaces;
        if (squashNamespaces.contains(itemNamespace)) {
            newSquashNamespaces = squashNamespaces;
        } else {
            newSquashNamespaces = ImmutableSet.<QNameModule>builderWithExpectedSize(squashNamespaces.size() + 1)
                .addAll(squashNamespaces).add(itemNamespace).build();
        }

        return new ChildLookup(hasUsesAugments(statement) ? concatUsesAugments(statement) : validUsesAugments,
            newSquashNamespaces, grpNamespace);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("augments", validUsesAugments)
            .add("squash", squashNamespaces)
            .add("local", localNamespace)
            .toString();
    }

    private ImmutableSet<AugmentEffectiveStatement> concatUsesAugments(final EffectiveStatement<?, ?> stmt) {
        final var concat = ImmutableSet.<AugmentEffectiveStatement>builder().addAll(validUsesAugments);
        streamUsesAugments(stmt).forEach(concat::add);
        return concat.build();
    }

    private static boolean hasUsesAugments(final EffectiveStatement<?, ?> stmt) {
        return streamUsesAugments(stmt).findAny().isPresent();
    }

    private static Stream<AugmentEffectiveStatement> streamUsesAugments(final EffectiveStatement<?, ?> stmt) {
        return stmt.streamEffectiveSubstatements(UsesEffectiveStatement.class)
            .flatMap(uses -> uses.streamEffectiveSubstatements(AugmentEffectiveStatement.class));
    }
}
