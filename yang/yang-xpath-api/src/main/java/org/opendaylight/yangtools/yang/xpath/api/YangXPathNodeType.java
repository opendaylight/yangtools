/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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

/**
 * XPath node type as defined in <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-NodeType">XPath 1.0</a>.
 *
 * @author Robert Varga
 */
@Beta
public enum YangXPathNodeType {
    /**
     * A {@code comment}.
     */
    COMMENT("comment"),
    /**
     * A {@code text}.
     */
    TEXT("text"),
    /**
     * A {@code processing-instruction}.
     */
    PROCESSING_INSTRUCTION("processing-instruction"),
    /**
     * A {@code node}.
     */
    NODE("node");

    private static final Map<String, YangXPathNodeType> STR_TO_TYPE = Maps.uniqueIndex(Arrays.asList(values()),
        YangXPathNodeType::toString);

    private final String str;

    YangXPathNodeType(final String str) {
        this.str = requireNonNull(str);
    }

    @Override
    public String toString() {
        return str;
    }

    public static Optional<YangXPathNodeType> forString(final String str) {
        return Optional.ofNullable(STR_TO_TYPE.get(requireNonNull(str)));
    }
}
