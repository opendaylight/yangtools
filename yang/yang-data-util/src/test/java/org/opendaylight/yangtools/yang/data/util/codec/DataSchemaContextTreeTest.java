/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class DataSchemaContextTreeTest {
    private static final QNameModule MODULE = QNameModule.create(URI.create("dataschemacontext"));
    private static final QName FOO = QName.create(MODULE, "foo");
    private static final QName BAR = QName.create(MODULE, "bar");
    private static final QName BAZ = QName.create(MODULE, "baz");
    private static DataSchemaContextTree CONTEXT;

    @BeforeClass
    public static void init() {
        CONTEXT = DataSchemaContextTree.from(YangParserTestUtils.parseYangResource("/dataschemacontext.yang"));
    }

    @AfterClass
    public static void cleanup() {
        CONTEXT = null;
    }

    @Test
    public void testCorrectInput() {
        assertTrue(CONTEXT.findChild(YangInstanceIdentifier.of(FOO)).isPresent());
        assertTrue(CONTEXT.findChild(YangInstanceIdentifier.of(FOO).node(BAR)).isPresent());
        assertTrue(CONTEXT.findChild(YangInstanceIdentifier.of(FOO).node(BAR).node(BAZ)).isPresent());
    }

    @Test
    public void testSimpleBad() {
        assertEquals(Optional.empty(), CONTEXT.findChild(YangInstanceIdentifier.of(BAR)));
    }

    @Test
    public void testNestedBad() {
        assertEquals(Optional.empty(), CONTEXT.findChild(YangInstanceIdentifier.of(BAR).node(BAZ)));
    }
}
