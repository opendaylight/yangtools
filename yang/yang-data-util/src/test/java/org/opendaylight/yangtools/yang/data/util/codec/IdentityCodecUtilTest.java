/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.net.URI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class IdentityCodecUtilTest {
    private static final QNameModule MODULE = QNameModule.create(URI.create("yangtools846"));
    private static SchemaContext SCHEMA_CONTEXT;

    @BeforeClass
    public static void init() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResource("/yangtools846.yang");
    }

    @AfterClass
    public static void cleanup() {
        SCHEMA_CONTEXT = null;
    }

    @Test
    public void testCorrectInput() {
        assertNotNull(IdentityCodecUtil.parseIdentity("yt846:foo", SCHEMA_CONTEXT,
            IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    public void testNonExistent() {
        assertThrows(IllegalArgumentException.class,
            () -> IdentityCodecUtil.parseIdentity("yt846:bar", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    public void testEmptyLocalName() {
        assertThrows(IllegalArgumentException.class,
            () -> IdentityCodecUtil.parseIdentity("yt846:", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    public void testEmptyString() {
        assertThrows(IllegalStateException.class,
            () -> IdentityCodecUtil.parseIdentity("", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    public void testNoPrefix() {
        assertThrows(IllegalStateException.class,
            () -> IdentityCodecUtil.parseIdentity("foo", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    public void testEmptyPrefix() {
        assertThrows(IllegalStateException.class,
            () -> IdentityCodecUtil.parseIdentity(":foo", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    public void testColon() {
        assertThrows(IllegalStateException.class,
            () -> IdentityCodecUtil.parseIdentity(":", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    private static QNameModule resolvePrefix(final String prefix) {
        // TODO: QNameCodecUtil should deal with some of the malformed stuff here by throwing IAE. We throw an ISE
        //       to discern what is happening.
        checkState("yt846".equals(prefix), "Unexpected prefix %s", prefix);
        return MODULE;
    }
}
