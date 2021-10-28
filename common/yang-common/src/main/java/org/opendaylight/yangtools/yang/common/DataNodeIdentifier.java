/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;

@Beta
public final class DataNodeIdentifier extends AbstractDataTreePath<DataNodeIdentifier.Step> {
    public static final class Step extends DataTreeStep<QName> {
        Step(final QName name, final ImmutableMap<QName, Object> predicates) {
            super(name, predicates);
        }

        public static @NonNull Step of(final QName name) {
            return of(name, ImmutableMap.of());
        }

        public static @NonNull Step of(final QName name, final QName predicateName, final Object predicateValue) {
            return of(name, ImmutableMap.of(predicateName, predicateValue));
        }

        public static @NonNull Step of(final QName name, final Map<QName, Object> predicates) {
            return of(name, ImmutableMap.copyOf(predicates));
        }

        public static @NonNull Step of(final QName name, final ImmutableMap<QName, Object> predicates) {
            return new Step(name, predicates);
        }
    }

    private DataNodeIdentifier(final ImmutableList<Step> steps) {
        super(steps);
    }

    public static @NonNull DataNodeIdentifier of(final Step step) {
        return of(ImmutableList.of(step));
    }

    public static @NonNull DataNodeIdentifier of(final Step... steps) {
        return of(ImmutableList.copyOf(steps));
    }

    public static @NonNull DataNodeIdentifier of(final List<Step> steps) {
        return of(ImmutableList.copyOf(steps));
    }

    public static @NonNull DataNodeIdentifier of(final ImmutableList<Step> steps) {
        return new DataNodeIdentifier(steps);
    }
}
