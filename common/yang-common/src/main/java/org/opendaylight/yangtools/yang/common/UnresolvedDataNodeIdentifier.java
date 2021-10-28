/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;

/**
 * @author nite
 *
 */
public final class UnresolvedDataNodeIdentifier extends AbstractDataTreePath<UnresolvedDataNodeIdentifier.Step> {
    public static final class Step extends DataTreeStep<UnresolvedQName> {
        Step(final UnresolvedQName name, final ImmutableMap<UnresolvedQName, Object> predicates) {
            super(name, predicates);
        }

        public static @NonNull Step of(final UnresolvedQName name) {
            return of(name, ImmutableMap.of());
        }

        public static @NonNull Step of(final UnresolvedQName name, final UnresolvedQName predicateName,
                final Object predicateValue) {
            return of(name, ImmutableMap.of(predicateName, predicateValue));
        }

        public static @NonNull Step of(final UnresolvedQName name,
                final Map<UnresolvedQName, Object> predicates) {
            return of(name, ImmutableMap.copyOf(predicates));
        }

        public static @NonNull Step of(final UnresolvedQName name,
                final ImmutableMap<UnresolvedQName, Object> predicates) {
            return new Step(name, predicates);
        }
    }

    private UnresolvedDataNodeIdentifier(final ImmutableList<Step> steps) {
        super(steps);
    }

    public static @NonNull UnresolvedDataNodeIdentifier of(final Step step) {
        return of(ImmutableList.of(step));
    }

    public static @NonNull UnresolvedDataNodeIdentifier of(final Step... steps) {
        return of(ImmutableList.copyOf(steps));
    }

    public static @NonNull UnresolvedDataNodeIdentifier of(final List<Step> steps) {
        return of(ImmutableList.copyOf(steps));
    }

    public static @NonNull UnresolvedDataNodeIdentifier of(final ImmutableList<Step> steps) {
        return new UnresolvedDataNodeIdentifier(steps);
    }
}
