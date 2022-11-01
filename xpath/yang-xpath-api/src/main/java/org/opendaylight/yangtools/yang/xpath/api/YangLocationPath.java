/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;

@Beta
public abstract sealed class YangLocationPath implements YangExpr {
    public abstract static sealed class Step implements Serializable, YangPredicateAware {
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
            final Set<YangExpr> predicates = getPredicates();
            if (!predicates.isEmpty()) {
                helper.add("predicates", predicates);
            }
            return helper;
        }
    }

    public static sealed class AxisStep extends Step {
        private static final long serialVersionUID = 1L;

        AxisStep(final YangXPathAxis axis) {
            super(axis);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(getAxis(), getPredicates());
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            return this == obj || obj instanceof AxisStep other && getAxis().equals(other.getAxis())
                && getPredicates().equals(other.getPredicates());
        }

        @SuppressFBWarnings(value = "SE_PRIVATE_READ_RESOLVE_NOT_INHERITED",
                justification = "We have only one subclass, and that does not want to inherit this")
        private Object readResolve() {
            return getAxis().asStep();
        }
    }

    static final class AxisStepWithPredicates extends AxisStep {
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
    }

    // match a particular namespace
    public static final class NamespaceStep extends Step {
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
        public boolean equals(@Nullable final Object obj) {
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
        private static final long serialVersionUID = 1L;

        QNameStep(final YangXPathAxis axis) {
            super(axis);
        }
    }

    private abstract static sealed class AbstractQNameStep<T extends AbstractQName> extends QNameStep {
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
        @SuppressFBWarnings(value = "EQ_UNUSUAL", justification = "Polymorphic via equalityClass()")
        public final boolean equals(final @Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            final Class<? extends AbstractQNameStep<?>> eq = equalityClass();
            if (!equalityClass().isInstance(obj)) {
                return false;
            }

            final AbstractQNameStep<?> other = eq.cast(obj);
            return getAxis().equals(other.getAxis()) && qname.equals(other.qname)
                    && getPredicates().equals(other.getPredicates());
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("qname", qname);
        }

        abstract Class<? extends AbstractQNameStep<?>> equalityClass();
    }

    public static sealed class ResolvedQNameStep extends AbstractQNameStep<QName> implements ResolvedQNameReferent {
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
        final Class<ResolvedQNameStep> equalityClass() {
            return ResolvedQNameStep.class;
        }
    }

    private static final class ResolvedQNameStepWithPredicates extends ResolvedQNameStep {
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
        final Class<UnresolvedQNameStep> equalityClass() {
            return UnresolvedQNameStep.class;
        }
    }

    private static final class UnresolvedQNameStepWithPredicates extends UnresolvedQNameStep {
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
        public boolean equals(@Nullable final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !getClass().equals(obj.getClass())) {
                return false;
            }
            final NodeTypeStep other = (NodeTypeStep) obj;
            return nodeType.equals(other.nodeType) && getAxis().equals(other.getAxis())
                    && getPredicates().equals(other.getPredicates());
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("nodeType", nodeType);
        }
    }

    @SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS",
            justification = "https://github.com/spotbugs/spotbugs/issues/511")
    static final class NodeTypeStepWithPredicates extends NodeTypeStep {
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
        private static final long serialVersionUID = 1L;

        Absolute(final ImmutableList<Step> steps) {
            super(steps);
        }

        @Override
        public boolean isAbsolute() {
            return true;
        }
    }

    public static final class Relative extends YangLocationPath {
        private static final long serialVersionUID = 1L;

        Relative(final ImmutableList<Step> steps) {
            super(steps);
        }

        @Override
        public boolean isAbsolute() {
            return false;
        }
    }

    private static final long serialVersionUID = 1L;
    private static final Absolute ROOT = new Absolute(ImmutableList.of());
    private static final Relative SELF = new Relative(ImmutableList.of());

    private final ImmutableList<Step> steps;

    private YangLocationPath(final ImmutableList<Step> steps) {
        this.steps = requireNonNull(steps);
    }

    public static final Absolute absolute(final Step... steps) {
        return absolute(Arrays.asList(steps));
    }

    public static final Absolute absolute(final Collection<Step> steps) {
        return steps.isEmpty() ? ROOT : new Absolute(ImmutableList.copyOf(steps));
    }

    public static final Relative relative(final Step... steps) {
        return relative(Arrays.asList(steps));
    }

    public static final Relative relative(final Collection<Step> steps) {
        return steps.isEmpty() ? SELF : new Relative(ImmutableList.copyOf(steps));
    }

    /**
     * The conceptual {@code root} {@link YangLocationPath}. This path is an absolute path and has no steps.
     *
     * @return Empty absolute {@link YangLocationPath}
     */
    public static final Absolute root() {
        return ROOT;
    }

    /**
     * The conceptual {@code same} {@link YangLocationPath}. This path is a relative path and has no steps and is
     * equivalent to a step along {@link YangXPathAxis#SELF}.
     *
     * @return Empty relative {@link YangLocationPath}
     */
    public static final Relative self() {
        return SELF;
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
        final ToStringHelper helper = MoreObjects.toStringHelper(YangLocationPath.class).add("absolute", isAbsolute());
        if (!steps.isEmpty()) {
            helper.add("steps", steps);
        }
        return helper.toString();
    }

    final Object readSolve() {
        return steps.isEmpty() ? isAbsolute() ? ROOT : SELF : this;
    }
}
