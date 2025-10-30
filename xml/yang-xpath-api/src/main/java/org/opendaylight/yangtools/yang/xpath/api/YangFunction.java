/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;

/**
 * Functions known to a YANG XPath.
 *
 * @author Robert Varga
 */
public enum YangFunction implements Identifiable<QName> {
    // XPath 1.0 functions
    BOOLEAN("boolean"),
    CEILING("ceiling"),
    CONCAT("concat"),
    CONTAINS("contains"),
    COUNT("count"),
    FALSE("false"),
    FLOOR("floor"),
    ID("id"),
    LANG("lang"),
    LAST("last"),
    LOCAL_NAME("local-name"),
    NAME("name"),
    NUMBER("number"),
    NAMESPACE_URI("namespace-uri"),
    NORMALIZE_SPACE("normalize-space"),
    NOT("not"),
    POSITION("position"),
    ROUND("round"),
    STARTS_WITH("starts-with"),
    STRING("string"),
    STRING_LENGTH("string-length"),
    SUM("sum"),
    SUBSTRING("substring"),
    SUBSTRING_AFTER("substring-after"),
    SUBSTRING_BEFORE("substring-before"),
    TRANSLATE("translate"),
    TRUE("true"),

    // RFC6020 functions
    CURRENT("current"),

    // RFC7950 functions
    BIT_IS_SET("bit-is-set", YangVersion.VERSION_1_1),
    DEREF("deref", YangVersion.VERSION_1_1),
    DERIVED_FROM("derived-from", YangVersion.VERSION_1_1),
    DERIVED_FROM_OR_SELF("derived-from-or-self", YangVersion.VERSION_1_1),
    ENUM_VALUE("enum-value", YangVersion.VERSION_1_1),
    RE_MATCH("re-match", YangVersion.VERSION_1_1);

    private final QName identifier;
    private final YangVersion yangVersion;

    YangFunction(final String localName, final YangVersion yangVersion) {
        identifier = QName.create(YangConstants.RFC6020_YIN_MODULE, localName).intern();
        this.yangVersion = requireNonNull(yangVersion);
    }

    YangFunction(final String localName) {
        this(localName, YangVersion.VERSION_1);
    }

    @Override
    public QName getIdentifier() {
        return identifier;
    }

    /**
     * Return the minimum YANG version where this function is supported.
     *
     * @return First YANG version where this function appeared.
     */
    public YangVersion getYangVersion() {
        return yangVersion;
    }
}
