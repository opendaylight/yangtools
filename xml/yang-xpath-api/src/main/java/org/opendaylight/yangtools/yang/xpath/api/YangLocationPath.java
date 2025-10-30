/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;

public abstract sealed class YangLocationPath implements YangExpr {
    public abstract static sealed class Step implements Serializable, YangPredicateAware {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final YangXPathAxis axis;

        Step(final YangXPathAxis axis) {
            this.axis = requireNonNull(axis);
        }

        public final YangXPathAxis getAxis() {
            return axis;
        }

        @Override
        public abstract int hashCode();

        @Override
        public abstract boolean equals(@Nullable Object obj);

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(Step.class)).toString();
        }

        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            helper.add("axis", axis);
            final var predicates = getPredicates();
            if (!predicates.isEmpty()) {
                helper.add("predicates", predicates);
            }
            return helper;
        }
    }

    public static sealed class AxisStep extends Step {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        AxisStep(final YangXPathAxis axis) {
            super(axis);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(getAxis(), getPredicates());
        }

        @Override
        public final boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof AxisStep other && getAxis().equals(other.getAxis())
                && getPredicates().equals(other.getPredicates());
        }

        Object readResolve() {
            return getAxis().asStep();
        }
    }

    static final class AxisStepWithPredicates extends AxisStep {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final ImmutableSet<YangExpr> predicates;

        AxisStepWithPredicates(final YangXPathAxis axis, final ImmutableSet<YangExpr> predicates) {
            super(axis);
            this.predicates = requireNonNull(predicates);
        }

        @Override
        public ImmutableSet<YangExpr> getPredicates() {
            return predicates;
        }

        @Override
        Object readResolve() {
            return this;
        }
    }

    // match a particular namespace
    public static final class NamespaceStep extends Step {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final QNameModule namespace;

        NamespaceStep(final YangXPathAxis axis, final QNameModule namespace) {
            super(axis);
            this.namespace = requireNonNull(namespace);
        }

        public QNameModule getNamespace() {
            return namespace;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAxis(), namespace, getPredicates());
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof NamespaceStep other && getAxis().equals(other.getAxis())
                && namespace.equals(other.namespace) && getPredicates().equals(other.getPredicates());
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("namespace", namespace);
        }
    }

    /**
     * A step along an axis. This may be either a {@link ResolvedQNameStep} or a {@link UnresolvedQNameStep}.
     *
     * @author Robert Varga
     */
    public abstract static sealed class QNameStep extends Step implements QNameReferent {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        QNameStep(final YangXPathAxis axis) {
            super(axis);
        }
    }

    private abstract static sealed class AbstractQNameStep<T extends AbstractQName> extends QNameStep {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final T qname;

        AbstractQNameStep(final YangXPathAxis axis, final T qname) {
            super(axis);
            this.qname = requireNonNull(qname);
        }

        @Override
        public final @NonNull T getQName() {
            return qname;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(getAxis(), qname, getPredicates());
        }

        @Override
        public final boolean equals(final @Nullable Object obj) {
            return this == obj || equalsImpl(obj);
        }

        abstract boolean equalsImpl(@Nullable Object obj);

        final boolean equalsImpl(final AbstractQNameStep<?> other) {
            return getAxis().equals(other.getAxis()) && qname.equals(other.qname)
                    && getPredicates().equals(other.getPredicates());
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("qname", qname);
        }
    }

    public static sealed class ResolvedQNameStep extends AbstractQNameStep<QName> implements ResolvedQNameReferent {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        ResolvedQNameStep(final YangXPathAxis axis, final QName qname) {
            super(axis, qname);
        }

        static ResolvedQNameStep of(final YangXPathAxis axis, final QName qname,
                final Collection<YangExpr> predicates) {
            return predicates.isEmpty() ? new ResolvedQNameStep(axis, qname)
                    : new ResolvedQNameStepWithPredicates(axis, qname, ImmutableSet.copyOf(predicates));
        }

        @Override
        boolean equalsImpl(final @Nullable Object obj) {
            return obj instanceof ResolvedQNameStep other && equalsImpl(other);
        }
    }

    private static final class ResolvedQNameStepWithPredicates extends ResolvedQNameStep {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final ImmutableSet<YangExpr> predicates;

        ResolvedQNameStepWithPredicates(final YangXPathAxis axis, final QName qname,
                final ImmutableSet<YangExpr> predicates) {
            super(axis, qname);
            this.predicates = requireNonNull(predicates);
        }

        @Override
        public ImmutableSet<YangExpr> getPredicates() {
            return predicates;
        }
    }

    public static sealed class UnresolvedQNameStep extends AbstractQNameStep<UnresolvedQName>
            implements UnresolvedQNameReferent {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        UnresolvedQNameStep(final YangXPathAxis axis, final UnresolvedQName qname) {
            super(axis, qname);
        }

        static UnresolvedQNameStep of(final YangXPathAxis axis, final UnresolvedQName qname,
                final Collection<YangExpr> predicates) {
            return predicates.isEmpty() ? new UnresolvedQNameStep(axis, qname)
                    : new UnresolvedQNameStepWithPredicates(axis, qname, ImmutableSet.copyOf(predicates));
        }

        @Override
        boolean equalsImpl(final @Nullable Object obj) {
            return obj instanceof UnresolvedQNameStep other && equalsImpl(other);
        }
    }

    private static final class UnresolvedQNameStepWithPredicates extends UnresolvedQNameStep {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final ImmutableSet<YangExpr> predicates;

        UnresolvedQNameStepWithPredicates(final YangXPathAxis axis, final UnresolvedQName qname,
                final ImmutableSet<YangExpr> predicates) {
            super(axis, qname);
            this.predicates = requireNonNull(predicates);
        }

        @Override
        public ImmutableSet<YangExpr> getPredicates() {
            return predicates;
        }
    }

    public static sealed class NodeTypeStep extends Step {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final YangXPathNodeType nodeType;

        NodeTypeStep(final YangXPathAxis axis, final YangXPathNodeType nodeType) {
            super(axis);
            this.nodeType = requireNonNull(nodeType);
        }

        public final YangXPathNodeType getNodeType() {
            return nodeType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAxis(), nodeType, getPredicates());
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !getClass().equals(obj.getClass())) {
                return false;
            }
            final var other = (NodeTypeStep) obj;
            return nodeType.equals(other.nodeType) && getAxis().equals(other.getAxis())
                    && getPredicates().equals(other.getPredicates());
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("nodeType", nodeType);
        }
    }

    static final class NodeTypeStepWithPredicates extends NodeTypeStep {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final ImmutableSet<YangExpr> predicates;

        NodeTypeStepWithPredicates(final YangXPathAxis axis, final YangXPathNodeType type,
                final ImmutableSet<YangExpr> predicates) {
            super(axis, type);
            this.predicates = requireNonNull(predicates);
        }

        @Override
        public ImmutableSet<YangExpr> getPredicates() {
            return predicates;
        }
    }

    public static sealed class ProcessingInstructionStep extends NodeTypeStep {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final String name;

        ProcessingInstructionStep(final YangXPathAxis axis, final String name) {
            super(axis, YangXPathNodeType.PROCESSING_INSTRUCTION);
            this.name = requireNonNull(name);
        }

        public final String getName() {
            return name;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(getAxis(), getNodeType(), name, getPredicates());
        }

        @Override
        public final boolean equals(final @Nullable Object obj) {
            return obj == this || super.equals(obj) && name.equals(((ProcessingInstructionStep) obj).name);
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("name", name);
        }
    }

    static final class ProcessingInstructionStepWithPredicates extends ProcessingInstructionStep {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final ImmutableSet<YangExpr> predicates;

        ProcessingInstructionStepWithPredicates(final YangXPathAxis axis, final String name,
                final ImmutableSet<YangExpr> predicates) {
            super(axis, name);
            this.predicates = requireNonNull(predicates);
        }

        @Override
        public ImmutableSet<YangExpr> getPredicates() {
            return predicates;
        }
    }

    public static final class Absolute extends YangLocationPath {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
        private static final Absolute EMPTY = new Absolute(ImmutableList.of());

        Absolute(final ImmutableList<Step> steps) {
            super(steps);
        }

        @Override
        public boolean isAbsolute() {
            return true;
        }
    }

    public static final class Relative extends YangLocationPath {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
        private static final Relative EMPTY = new Relative(ImmutableList.of());

        Relative(final ImmutableList<Step> steps) {
            super(steps);
        }

        @Override
        public boolean isAbsolute() {
            return false;
        }
    }

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final ImmutableList<Step> steps;

    private YangLocationPath(final ImmutableList<Step> steps) {
        this.steps = requireNonNull(steps);
    }

    public static final Absolute absolute(final Step... steps) {
        return absolute(Arrays.asList(steps));
    }

    public static final Absolute absolute(final Collection<Step> steps) {
        return steps.isEmpty() ? Absolute.EMPTY : new Absolute(ImmutableList.copyOf(steps));
    }

    public static final Relative relative(final Step... steps) {
        return relative(Arrays.asList(steps));
    }

    public static final Relative relative(final Collection<Step> steps) {
        return steps.isEmpty() ? Relative.EMPTY : new Relative(ImmutableList.copyOf(steps));
    }

    /**
     * The conceptual {@code root} {@link YangLocationPath}. This path is an absolute path and has no steps.
     *
     * @return Empty absolute {@link YangLocationPath}
     */
    public static final Absolute root() {
        return Absolute.EMPTY;
    }

    /**
     * The conceptual {@code same} {@link YangLocationPath}. This path is a relative path and has no steps and is
     * equivalent to a step along {@link YangXPathAxis#SELF}.
     *
     * @return Empty relative {@link YangLocationPath}
     */
    public static final Relative self() {
        return Relative.EMPTY;
    }

    public final ImmutableList<Step> getSteps() {
        return steps;
    }

    public abstract boolean isAbsolute();

    @Override
    public final int hashCode() {
        return Boolean.hashCode(isAbsolute()) * 31 + steps.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangLocationPath other && isAbsolute() == other.isAbsolute()
            && steps.equals(other.steps);
    }

    @Override
    public final String toString() {
        final var helper = MoreObjects.toStringHelper(YangLocationPath.class).add("absolute", isAbsolute());
        if (!steps.isEmpty()) {
            helper.add("steps", steps);
        }
        return helper.toString();
    }

    final Object readSolve() {
        return steps.isEmpty() ? isAbsolute() ? Absolute.EMPTY : Relative.EMPTY : this;
    }
}
