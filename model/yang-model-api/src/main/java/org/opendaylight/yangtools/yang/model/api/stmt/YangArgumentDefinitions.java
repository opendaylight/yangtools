/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

@NonNullByDefault
final class YangArgumentDefinitions {
    /**
     * The definition of RFC7950 {@code condition} statement argument bound to a {@link QualifiedBound}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<QualifiedBound> CONDITION_AS_QUALIFIED_BOUND =
        ArgumentDefinition.of(QualifiedBound.class, YangConstants.RFC6020_YIN_MODULE, "condition");

    /**
     * The definition of RFC7950 {@code date} statement argument bound to a {@link Revision}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<Revision> DATE_AS_REVISION = ArgumentDefinition.of(Revision.class,
        YangConstants.RFC6020_YIN_MODULE, "date");


    /**
     * The definition of RFC7950 {@code module} statement argument bound to a {@link Unqualified}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<Unqualified> MODULE_AS_UNQUALIFIED = ArgumentDefinition.of(Unqualified.class,
        YangConstants.RFC6020_YIN_MODULE, "module");

    /**
     * The definition of RFC7950 {@code name} statement argument bound to a {@link QName}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<QName> NAME_AS_QNAME = ArgumentDefinition.of(QName.class,
        YangConstants.RFC6020_YIN_MODULE, "name");

    /**
     * The definition of RFC7950 {@code name} statement argument bound to a {@link String}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<String> NAME_AS_STRING = ArgumentDefinition.of(String.class,
        YangConstants.RFC6020_YIN_MODULE, "name");

    /**
     * The definition of RFC7950 {@code name} statement argument bound to a {@link Unqualified}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<Unqualified> NAME_AS_UNQUALIFIED = ArgumentDefinition.of(Unqualified.class,
        YangConstants.RFC6020_YIN_MODULE, "name");

    /**
     * The definition of RFC7950 {@code text} statement argument bound to a {@link String}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<String> TEXT_AS_STRING = ArgumentDefinition.of(String.class,
        YangConstants.RFC6020_YIN_MODULE, "text", true);

    /**
     * The definition of RFC7950 {@code value} statement argument bound to a {@link Boolean}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<Boolean> VALUE_AS_BOOLEAN = ArgumentDefinition.of(Boolean.class,
        YangConstants.RFC6020_YIN_MODULE, "value");

    /**
     * The definition of RFC7950 {@code value} statement argument bound to a {@link Integer}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<Integer> VALUE_AS_INTEGER = ArgumentDefinition.of(Integer.class,
        YangConstants.RFC6020_YIN_MODULE, "value");

    /**
     * The definition of RFC7950 {@code value} statement argument bound to a {@link String}.
     *
     * @since 15.0.0
     */
    static final ArgumentDefinition<String> VALUE_AS_STRING = ArgumentDefinition.of(String.class,
        YangConstants.RFC6020_YIN_MODULE, "value");

    /**
     * The definition of RFC7950 {@code value} statement argument bound to a {@link ValueRanges}.
     *
     * @since 15.0.0
     */
    public static final ArgumentDefinition<ValueRanges> VALUE_AS_VALUE_RANGES = ArgumentDefinition.of(ValueRanges.class,
        YangConstants.RFC6020_YIN_MODULE, "value");

    private YangArgumentDefinitions() {
        // Hidden on purpose
    }
}
