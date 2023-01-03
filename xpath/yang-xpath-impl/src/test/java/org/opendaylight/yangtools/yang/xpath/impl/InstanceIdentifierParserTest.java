/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import com.google.common.collect.ImmutableBiMap;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.BiMapYangNamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Absolute;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;

class InstanceIdentifierParserTest {
    private static final QNameModule DEFNS = QNameModule.create(XMLNamespace.of("defaultns"));
    private static final YangNamespaceContext CONTEXT = new BiMapYangNamespaceContext(ImmutableBiMap.of(
        "def", DEFNS,
        "foo", QNameModule.create(XMLNamespace.of("foo")),
        "bar", QNameModule.create(XMLNamespace.of("bar"))));

    private static final InstanceIdentifierParser PARSER =
        new InstanceIdentifierParser.Qualified(YangXPathMathMode.IEEE754, CONTEXT);

    @Test
    void smokeTests() throws Exception {
        assertPath("/foo:foo");
        assertPath("/foo:foo[.='abcd']");
        assertPath("/foo:foo[.=\"abcd\"]");
    }

    private static Absolute assertPath(final String literal) throws Exception {
        return PARSER.interpretAsInstanceIdentifier(YangLiteralExpr.of(literal));
    }
}
