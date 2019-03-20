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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

@Beta
public class YangLocationPath implements YangExpr {
    public abstract static class Step implements Serializable, YangPredicateAware {
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

    public static class AxisStep extends Step {
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
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AxisStep)) {
                return false;
            }
            final AxisStep other = (AxisStep) obj;
            return getAxis().equals(other.getAxis()) && getPredicates().equals(other.getPredicates());
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
    public static class NamespaceStep extends Step {
        private static final long serialVersionUID = 1L;

        private final QNameModule namespace;

        NamespaceStep(final YangXPathAxis axis, final QNameModule namespace) {
            super(axis);
            this.namespace = requireNonNull(namespace);
        }

        public final QNameModule getNamespace() {
            return namespace;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(getAxis(), namespace, getPredicates());
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof NamespaceStep)) {
                return false;
            }
            final NamespaceStep other = (NamespaceStep) obj;
            return getAxis().equals(other.getAxis()) && namespace.equals(other.namespace)
                    && getPredicates().equals(other.getPredicates());
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("namespace", namespace);
        }
    }

    public static class QNameStep extends Step {
        private static final long serialVersionUID = 1L;

        private final QName qname;

        QNameStep(final YangXPathAxis axis, final QName qname) {
            super(axis);
            this.qname = requireNonNull(qname);
        }

        public final QName getQName() {
            return qname;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(getAxis(), qname, getPredicates());
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof QNameStep)) {
                return false;
            }
            final QNameStep other = (QNameStep) obj;
            return getAxis().equals(other.getAxis()) && qname.equals(other.qname)
                    && getPredicates().equals(other.getPredicates());
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("qname", qname);
        }
    }

    static final class QNameStepWithPredicates extends QNameStep {
        private static final long serialVersionUID = 1L;

        private final ImmutableSet<YangExpr> predicates;

        QNameStepWithPredicates(final YangXPathAxis axis, final QName qname, final ImmutableSet<YangExpr> predicates) {
            super(axis, qname);
            this.predicates = requireNonNull(predicates);
        }

        @Override
        public ImmutableSet<YangExpr> getPredicates() {
            return predicates;
        }
    }

    public static class NodeTypeStep extends Step {
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

    public static class ProcessingInstructionStep extends NodeTypeStep {
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

    private static final long serialVersionUID = 1L;
    private static final YangLocationPath ROOT = new Absolute(ImmutableList.of());
    private static final YangLocationPath SELF = new YangLocationPath(ImmutableList.of());

    private final ImmutableList<Step> steps;

    YangLocationPath(final ImmutableList<Step> steps) {
        this.steps = requireNonNull(steps);
    }

    public static final YangLocationPath of(final boolean absolute) {
        return absolute ? ROOT : SELF;
    }

    public static final YangLocationPath of(final boolean absolute, final Step... steps) {
        return of(absolute, Arrays.asList(steps));
    }

    public static final YangLocationPath of(final boolean absolute, final Collection<Step> steps) {
        if (steps.isEmpty()) {
            return of(absolute);
        }

        final ImmutableList<Step> copy = ImmutableList.copyOf(steps);
        return absolute ? new Absolute(copy) : new YangLocationPath(copy);
    }

    /**
     * The conceptual {@code root} {@link YangLocationPath}. This path is an absolute path and has no steps.
     *
     * @return Empty absolute {@link YangLocationPath}
     */
    public static final YangLocationPath root() {
        return ROOT;
    }

    /**
     * The conceptual {@code same} {@link YangLocationPath}. This path is a relative path and has no steps and is
     * equivalent to a step along {@link YangXPathAxis#SELF}.
     *
     * @return Empty relative {@link YangLocationPath}
     */
    public static YangLocationPath self() {
        return SELF;
    }

    public boolean isAbsolute() {
        return false;
    }

    public final ImmutableList<Step> getSteps() {
        return steps;
    }

    @Override
    public final int hashCode() {
        return Boolean.hashCode(isAbsolute()) * 31 + steps.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof YangLocationPath)) {
            return false;
        }
        final YangLocationPath other = (YangLocationPath) obj;
        return isAbsolute() == other.isAbsolute() && steps.equals(other.steps);
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
        return steps.isEmpty() ? of(isAbsolute()) : this;
    }
 }
