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
import com.google.common.base.MoreObjects.ToStringHelper;
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
        private final @NonNull T qname;

        AbstractStep(final T qname) {
            this.qname = requireNonNull(qname);
        }

        public final @NonNull T qname() {
            return qname;
        }

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
        }

        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("qname", qname);
        }
    }

    /**
     * Represents the path to a particular node in the data tree. This concept is defined as {@code instance-identifier}
     * in <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.13">RFC7950, section 9.13</a>, bound to a
     * particular {@link QNameModule}.
     */
    @Beta
    public static final class Resolved extends DataNodeIdentifier<Resolved.Step> {
        @Beta
        public static class Step extends AbstractStep<QName> {
            Step(final QName qname) {
                super(qname);
            }
        }

        @Beta
        public static final class LeafListStep extends Step {
            private final @NonNull Object value;

            LeafListStep(final QName qname, final Object value) {
                super(qname);
                this.value = requireNonNull(value);
            }

            public @NonNull Object value() {
                return value;
            }

            @Override ToStringHelper addToStringAttributes(final ToStringHelper helper) {
                return super.addToStringAttributes(helper).add("value", value);
            }
        }

        @Beta
        public static final class ListKeyStep extends Step {
            private final @NonNull ImmutableMap<QName, Object> keys;

            ListKeyStep(final QName qname, final ImmutableMap<QName, Object> keys) {
                super(qname);
                this.keys = requireNonNull(keys);
            }

            public @NonNull ImmutableMap<QName, Object> keys() {
                return keys;
            }

            @Override
            ToStringHelper addToStringAttributes(final ToStringHelper helper) {
                return super.addToStringAttributes(helper).add("keys", keys);
            }
        }

        @Beta
        public static final class ListPosStep extends Step {
            private final int position;

            ListPosStep(final QName qname, final int position) {
                super(qname);
                this.position = position;
            }

            public int position() {
                return position;
            }

            @Override ToStringHelper addToStringAttributes(final ToStringHelper helper) {
                return super.addToStringAttributes(helper).add("position", position);
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
        @Beta
        public static class Step extends AbstractStep<UnresolvedQName> {
            Step(final UnresolvedQName qname) {
                super(qname);
            }
        }

        @Beta
        public static final class LeafListStep extends Step {
            private final @NonNull String value;

            LeafListStep(final UnresolvedQName qname, final String value) {
                super(qname);
                this.value = requireNonNull(value);
            }

            public @NonNull String value() {
                return value;
            }

            @Override
            ToStringHelper addToStringAttributes(final ToStringHelper helper) {
                return super.addToStringAttributes(helper).add("value", value);
            }
        }

        @Beta
        public static final class ListKeyStep extends Step {
            private final @NonNull ImmutableMap<UnresolvedQName, String> keys;

            ListKeyStep(final UnresolvedQName qname, final ImmutableMap<UnresolvedQName, String> keys) {
                super(qname);
                this.keys = requireNonNull(keys);
            }

            public @NonNull ImmutableMap<UnresolvedQName, String> keys() {
                return keys;
            }

            @Override ToStringHelper addToStringAttributes(final ToStringHelper helper) {
                return super.addToStringAttributes(helper).add("keys", keys);
            }
        }

        @Beta
        public static final class ListPosStep extends Step {
            private final int position;

            ListPosStep(final UnresolvedQName qname, final int position) {
                super(qname);
                this.position = position;
            }

            public int position() {
                return position;
            }

            @Override
            ToStringHelper addToStringAttributes(final ToStringHelper helper) {
                return super.addToStringAttributes(helper).add("position", position);
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

        public static @NonNull Step stepOf(final UnresolvedQName qname) {
            return new Step(qname);
        }

        public static @NonNull ListPosStep stepOf(final UnresolvedQName qname, final int position) {
            return new ListPosStep(qname, position);
        }

        public static @NonNull LeafListStep stepOf(final UnresolvedQName qname, final String value) {
            return new LeafListStep(qname, value);
        }

        public static @NonNull ListKeyStep stepOf(final UnresolvedQName qname, final UnresolvedQName key,
                final String value) {
            return stepOf(qname, ImmutableMap.of(key, value));
        }

        public static @NonNull ListKeyStep stepOf(final UnresolvedQName qname,
                final Map<UnresolvedQName, String> keys) {
            return stepOf(qname, ImmutableMap.copyOf(keys));
        }

        public static @NonNull ListKeyStep stepOf(final UnresolvedQName qname,
                final ImmutableMap<UnresolvedQName, String> keys) {
            return new ListKeyStep(qname, keys);
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
