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
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.AnyNameStep;

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

    private static final Map<String, YangXPathAxis> STR_TO_AXIS = Maps.uniqueIndex(Arrays.asList(values()),
        YangXPathAxis::toString);

    private final AnyNameStep step;
    private final String str;

    YangXPathAxis(final String str) {
        this.str = requireNonNull(str);
        step = new AnyNameStepBuilder().axis(this).build();
    }

    /**
     * Return the name-independent {@link AnyNameStep} along this axis. XPath defines following axis
     * {@link AnyNameStep}s:
     * <ul>
     *     <li>{@link #SELF} axis this equals to the "." step</li>
     *     <li>{@link #PARENT} axis this equals to the ".." step</li>
     *     <li>{@link #DESCENDANT_OR_SELF} axis this equals to the "//" separator</li>
     * </ul>
     * other axes have these defined as a courtesy.
     *
     * @return Name-independent AnyNameStep.
     */
    public AnyNameStep asStep() {
        return step;
    }

    @Override
    public String toString() {
        return str;
    }

    public static Optional<YangXPathAxis> forString(final String str) {
        return Optional.ofNullable(STR_TO_AXIS.get(requireNonNull(str)));
    }
}
