/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.AbstractMap.SimpleImmutableEntry;
import org.junit.Test;

public class YangNamesTest {
    @Test
    public void testParseFileName() {
        assertEquals(new SimpleImmutableEntry<>("foo", null), YangNames.parseFilename("foo"));
        assertEquals(new SimpleImmutableEntry<>("foo", "bar"), YangNames.parseFilename("foo@bar"));
        assertEquals(new SimpleImmutableEntry<>("foo@bar", "baz"), YangNames.parseFilename("foo@bar@baz"));
    }

    @Test
    public void testParseFileNameNull() {
        assertThrows(NullPointerException.class, () -> YangNames.parseFilename(null));
    }
}
