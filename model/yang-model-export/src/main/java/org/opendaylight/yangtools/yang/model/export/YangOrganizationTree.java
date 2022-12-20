/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public class YangOrganizationTree implements Immutable, Iterable<@NonNull String> {

    private final Set<@NonNull StatementDefinition> ignoredStatements;
    private final StatementPrefixResolver resolver;
    private final DeclaredStatement<?> statement;
    private final int depth;

    YangOrganizationTree(final DeclaredStatement<?> statement, final StatementPrefixResolver resolver,
            final Set<@NonNull StatementDefinition> ignoredStatements, final int depth) {
        this.statement = requireNonNull(statement);
        this.resolver = requireNonNull(resolver);
        this.ignoredStatements = requireNonNull(ignoredStatements);
        this.depth = depth;
    }

    @Override
    public Iterator<@NonNull String> iterator() {
        return new YangOrganizationTreeIterator(statement, resolver, ignoredStatements, depth);
    }

    @Override
    public Spliterator<@NonNull String> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(),
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
    }

    public Stream<@NonNull String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }


    public String toString() {
        return stream().collect(Collectors.joining());
    }
}
