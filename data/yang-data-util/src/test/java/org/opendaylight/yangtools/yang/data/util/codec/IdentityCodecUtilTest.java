/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class IdentityCodecUtilTest {
    private static final QNameModule MODULE = QNameModule.of("yangtools846");
    private static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeAll
    static void init() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module yangtools846 {
              namespace "yangtools846";
              prefix yt846;

              identity foo;
            }""");
    }

    @AfterAll
    static void cleanup() {
        SCHEMA_CONTEXT = null;
    }

    @Test
    void testCorrectInput() {
        assertNotNull(IdentityCodecUtil.parseIdentity("yt846:foo", SCHEMA_CONTEXT,
            IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    void testNonExistent() {
        assertThrows(IllegalArgumentException.class,
            () -> IdentityCodecUtil.parseIdentity("yt846:bar", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    void testEmptyLocalName() {
        assertThrows(IllegalArgumentException.class,
            () -> IdentityCodecUtil.parseIdentity("yt846:", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    void testEmptyString() {
        assertThrows(IllegalStateException.class,
            () -> IdentityCodecUtil.parseIdentity("", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    void testNoPrefix() {
        assertThrows(IllegalStateException.class,
            () -> IdentityCodecUtil.parseIdentity("foo", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    void testEmptyPrefix() {
        assertThrows(IllegalStateException.class,
            () -> IdentityCodecUtil.parseIdentity(":foo", SCHEMA_CONTEXT, IdentityCodecUtilTest::resolvePrefix));
    }

    @Test
    void testColon() {
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
