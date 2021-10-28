/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;

/**
 * Represents the path to a particular node in the data tree. This concept is defined as {@code instance-identifier} in
 * <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.13">RFC7950, section 9.13</a>,
 */
@Beta
public abstract class DataNodeIdentifier<T extends DataNodeIdentifier.AbstractStep<?>> implements Immutable {
    abstract static class AbstractStep<T extends AbstractQName> implements Immutable {
        private final @NonNull ImmutableMap<T, Object> keyValues;
        private final @NonNull T qname;

        AbstractStep(final T qname, final ImmutableMap<T, Object> keyValues) {
            this.qname = requireNonNull(qname);
            this.keyValues = requireNonNull(keyValues);
        }

        public final @NonNull T qname() {
            return qname;
        }

        public final @NonNull ImmutableMap<T, Object> keyValues() {
            return keyValues;
        }

        @Override
        public final String toString() {
            final var helper = MoreObjects.toStringHelper(this).add("qname", qname);
            if (!keyValues.isEmpty()) {
                helper.add("keyValuss", keyValues);
            }
            return helper.toString();
        }
    }

    /**
     * Represents the path to a particular node in the data tree. This concept is defined as {@code instance-identifier}
     * in <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.13">RFC7950, section 9.13</a>, bound to a
     * particular {@link QNameModule}.
     */
    @Beta
    public static final class Resolved extends DataNodeIdentifier<Resolved.Step> {
        public static final class Step extends AbstractStep<QName> {
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

        private Resolved(final ImmutableList<Step> steps) {
            super(steps);
        }

        public static @NonNull Resolved of(final QName qname) {
            return of(ImmutableList.of(Step.of(qname)));
        }

        public static @NonNull Resolved of(final QName qname, final QName predicateName,
                final Object predicateValue) {
            return of(ImmutableList.of(Step.of(qname, predicateName, predicateValue)));
        }

        public static @NonNull Resolved of(final Step step) {
            return of(ImmutableList.of(step));
        }

        public static @NonNull Resolved of(final Step... steps) {
            return of(ImmutableList.copyOf(steps));
        }

        public static @NonNull Resolved of(final List<Step> steps) {
            return of(ImmutableList.copyOf(steps));
        }

        public static @NonNull Resolved of(final ImmutableList<Step> steps) {
            return new Resolved(steps);
        }
    }

    /**
     * Represents the path to a particular node in the data tree. This concept is defined as {@code instance-identifier}
     * in <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.13">RFC7950, section 9.13</a>, not bound to
     * any particular instantiation place.
     */
    @Beta
    public static final class Unresolved extends DataNodeIdentifier<Unresolved.Step> {
        public static final class Step extends AbstractStep<UnresolvedQName> {
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

        private Unresolved(final ImmutableList<Step> steps) {
            super(steps);
        }

        public static @NonNull Unresolved of(final UnresolvedQName qname) {
            return of(ImmutableList.of(Step.of(qname)));
        }

        public static @NonNull Unresolved of(final UnresolvedQName qname, final UnresolvedQName predicateName,
                final Object predicateValue) {
            return of(ImmutableList.of(Step.of(qname, predicateName, predicateValue)));
        }

        public static @NonNull Unresolved of(final Step step) {
            return of(ImmutableList.of(step));
        }

        public static @NonNull Unresolved of(final Step... steps) {
            return of(ImmutableList.copyOf(steps));
        }

        public static @NonNull Unresolved of(final List<Step> steps) {
            return of(ImmutableList.copyOf(steps));
        }

        public static @NonNull Unresolved of(final ImmutableList<Step> steps) {
            return new Unresolved(steps);
        }
    }

    private final ImmutableList<T> steps;

    DataNodeIdentifier(final ImmutableList<T> steps) {
        this.steps = requireNonNull(steps);
        checkArgument(!steps.isEmpty());
    }

    public final ImmutableList<T> steps() {
        return steps;
    }
}
