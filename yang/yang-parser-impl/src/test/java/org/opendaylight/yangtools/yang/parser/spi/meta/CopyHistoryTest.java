/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CopyHistoryTest {

    @Test
    public void testSingleton() {
        final CopyHistory original = CopyHistory.original();

        assertEquals(CopyType.ORIGINAL, original.getLastOperation());
        assertTrue(original.contains(CopyType.ORIGINAL));
        assertFalse(original.contains(CopyType.ADDED_BY_USES));
        assertFalse(original.contains(CopyType.ADDED_BY_AUGMENTATION));
        assertFalse(original.contains(CopyType.ADDED_BY_USES_AUGMENTATION));

        assertSame(original, CopyHistory.original());
    }

    @Test
    public void testAppend() {
        final CopyHistory original = CopyHistory.original();
        assertSame(original, original.append(CopyType.ORIGINAL, original));

        final CopyHistory originalUA = original.append(CopyType.ADDED_BY_USES_AUGMENTATION, original);
        assertEquals(CopyType.ADDED_BY_USES_AUGMENTATION, originalUA.getLastOperation());
        assertTrue(originalUA.contains(CopyType.ORIGINAL));
        assertFalse(originalUA.contains(CopyType.ADDED_BY_USES));
        assertFalse(originalUA.contains(CopyType.ADDED_BY_AUGMENTATION));
        assertTrue(originalUA.contains(CopyType.ADDED_BY_USES_AUGMENTATION));

        assertSame(originalUA, original.append(CopyType.ADDED_BY_USES_AUGMENTATION, original));
        assertSame(originalUA, originalUA.append(CopyType.ADDED_BY_USES_AUGMENTATION, original));

        final CopyHistory originalU = original.append(CopyType.ADDED_BY_USES, original);
        assertEquals(CopyType.ADDED_BY_USES, originalU.getLastOperation());
        assertTrue(originalU.contains(CopyType.ORIGINAL));
        assertTrue(originalU.contains(CopyType.ADDED_BY_USES));
        assertFalse(originalU.contains(CopyType.ADDED_BY_AUGMENTATION));
        assertFalse(originalU.contains(CopyType.ADDED_BY_USES_AUGMENTATION));

        final CopyHistory uaU = originalUA.append(CopyType.ADDED_BY_USES, original);
        assertEquals(CopyType.ADDED_BY_USES, uaU.getLastOperation());
        assertTrue(uaU.contains(CopyType.ORIGINAL));
        assertTrue(uaU.contains(CopyType.ADDED_BY_USES));
        assertFalse(uaU.contains(CopyType.ADDED_BY_AUGMENTATION));
        assertTrue(uaU.contains(CopyType.ADDED_BY_USES_AUGMENTATION));

        assertSame(uaU, originalUA.append(CopyType.ADDED_BY_USES, original));

        final CopyHistory res = originalUA.append(CopyType.ADDED_BY_AUGMENTATION, originalU);
        assertEquals(CopyType.ADDED_BY_AUGMENTATION, res.getLastOperation());
        assertTrue(res.contains(CopyType.ORIGINAL));
        assertTrue(res.contains(CopyType.ADDED_BY_USES));
        assertTrue(res.contains(CopyType.ADDED_BY_AUGMENTATION));
        assertTrue(res.contains(CopyType.ADDED_BY_USES_AUGMENTATION));
    }

}
