/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1297Test {
    private static final QNameModule RESTCONF =
        QNameModule.create(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-restconf"), Revision.of("2017-01-26"));
    private static final QNameModule BAD_MODULE =
        QNameModule.create(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-restconf"), Revision.of("2018-01-26"));

    private static EffectiveModelContext context;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(context);

    @BeforeClass
    public static void beforeClass() {
        context = YangParserTestUtils.parseYangResource("/ietf-restconf.yang");
    }

    @Test
    public void testEnterYangData() {
        assertNotNull(stack.enterYangData(new YangDataName(RESTCONF, "yang-api")));
        assertNotNull(stack.enterDataTree(QName.create(RESTCONF, "restconf")));
    }

    @Test
    public void testEnterYangDataNegative() {
        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> stack.enterYangData(new YangDataName(RESTCONF, "bad-name")));
        assertEquals("yang-data bad-name not present in " + RESTCONF, ex.getMessage());
        ex = assertThrows(IllegalArgumentException.class,
            () -> stack.enterYangData(new YangDataName(BAD_MODULE, "whatever")));
        assertEquals("Module for " + BAD_MODULE + " not found", ex.getMessage());

        assertNotNull(stack.enterGrouping(QName.create(RESTCONF, "errors")));
        ex = assertThrows(IllegalStateException.class,
            () -> stack.enterYangData(new YangDataName(RESTCONF, "yang-api")));
        assertEquals("Cannot lookup yang-data in a non-empty stack", ex.getMessage());
    }
}
