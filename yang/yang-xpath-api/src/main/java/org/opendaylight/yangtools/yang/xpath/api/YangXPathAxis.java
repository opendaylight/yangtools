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
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.AxisStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.AxisStepWithPredicates;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.NodeTypeStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.NodeTypeStepWithPredicates;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.ProcessingInstructionStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.ProcessingInstructionStepWithPredicates;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStepWithPredicates;

/**
 * XPath evaluation axis, as defined in <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#axes">XPath 1.0</a>.
 *
 * @author Robert Varga
 */
@Beta
public enum YangXPathAxis {
    /**
     * The {@code child} axis.
     */
    CHILD("child"),
    /**
     * The {@code descendant} axis.
     */
    DESCENDANT("descendant"),
    /**
     * The {@code parent} axis.
     */
    PARENT("parent"),
    /**
     * The {@code ancestor} axis.
     */
    ANCESTOR("ancestor"),
    /**
     * The {@code following-sibling} axis.
     */
    FOLLOWING_SIBLING("following-sibling"),
    /**
     * The {@code preceding-sibling} axis.
     */
    PRECEDING_SIBLING("preceding-sibling"),
    /**
     * The {@code following} axis.
     */
    FOLLOWING("following"),
    /**
     * The {@code preceding} axis.
     */
    PRECEDING("preceding"),
    /**
     * The {@code attribute} axis.
     */
    ATTRIBUTE("attribute"),
    /**
     * The {@code namespace} axis.
     */
    NAMESPACE("namespace"),
    /**
     * The {@code self} axis.
     */
    SELF("self"),
    /**
     * The {@code descendant-or-self} axis.
     */
    DESCENDANT_OR_SELF("descendant-or-self"),
    /**
     * The {@code ancestor-or-self} axis.
     */
    ANCESTOR_OR_SELF("ancestor-or-self");

    private final AxisStep step = new AxisStep(this);
    private final String str;

    YangXPathAxis(final String str) {
        this.str = requireNonNull(str);
    }

    /**
     * Return the name-independent {@link AxisStep} along this axis. XPath defines following axis {@code AxisStep}s:
     * <ul>
     *     <li>{@link #SELF} axis this equals to the "." step</li>
     *     <li>{@link #PARENT} axis this equals to the ".." step</li>
     *     <li>{@link #DESCENDANT_OR_SELF} axis this equals to the "//" separator</li>
     * </ul>
     * other axes have these defined as a courtesy.
     *
     * @return Name-independent AnyNameStep.
     */
    public final AxisStep asStep() {
        return step;
    }

    public final AxisStep asStep(final Collection<YangExpr> predicates) {
        final ImmutableSet<YangExpr> set = ImmutableSet.copyOf(predicates);
        return set.isEmpty() ? step : new AxisStepWithPredicates(this, set);
    }

    public final QNameStep asStep(final QName qname) {
        return new QNameStep(this, qname);
    }

    public final QNameStep asStep(final QName qname, final Collection<YangExpr> predicates) {
        final ImmutableSet<YangExpr> set = ImmutableSet.copyOf(predicates);
        return set.isEmpty() ? asStep(qname) : new QNameStepWithPredicates(this, qname, set);
    }

    public final NodeTypeStep asStep(final YangXPathNodeType type) {
        return new NodeTypeStep(this, type);
    }

    public final NodeTypeStep asStep(final YangXPathNodeType type, final Collection<YangExpr> predicates) {
        final ImmutableSet<YangExpr> set = ImmutableSet.copyOf(predicates);
        return set.isEmpty() ? asStep(type) : new NodeTypeStepWithPredicates(this, type, set);
    }

    public final ProcessingInstructionStep asStep(final String name) {
        return new ProcessingInstructionStep(this, name);
    }

    public final ProcessingInstructionStep asStep(final String name, final Collection<YangExpr> predicates) {
        final ImmutableSet<YangExpr> set = ImmutableSet.copyOf(predicates);
        return set.isEmpty() ? asStep(name) : new ProcessingInstructionStepWithPredicates(this, name, set);
    }

    @Override
    public String toString() {
        return str;
    }
}
