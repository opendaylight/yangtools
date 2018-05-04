/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.xpath;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Functions known to a YANG XPath.
 *
 * @author Robert Varga
 */
@Beta
public enum YangFunction implements Identifiable<QName> {
    // XPath 1.0 functions
    BOOLEAN("boolean"),
    CEILING("ceiling"),
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

    // RFC7950 functions
    BIT_IS_SET("bit-is-set"),
    CURRENT("current"),
    DEREF("deref"),
    DERIVED_FROM("derived-from"),
    DERIVED_FROM_OR_SELF("derived-from-or-self"),
    ENUM_VALUE("enum-value"),
    RE_MATCH("re-match");

    private static final Map<QName, YangFunction> QNAME_TO_FUNCTION = Maps.uniqueIndex(Arrays.asList(values()),
        YangFunction::getIdentifier);

    private final QName identifier;

    private YangFunction(final String localName) {
        identifier = QName.create(YangConstants.RFC6020_YIN_MODULE, localName).intern();
    }

    @Override
    public QName getIdentifier() {
        return identifier;
    }

    public static Optional<YangFunction> forQName(final QName qname) {
        return Optional.ofNullable(QNAME_TO_FUNCTION.get(requireNonNull(qname)));
    }
}
