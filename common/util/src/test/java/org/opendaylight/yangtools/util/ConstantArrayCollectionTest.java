/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConstantArrayCollectionTest {
    private static final String[] ARRAY = new String[] { "a", "bb", "ccc" };
    private static final Collection<String> REF = List.of(ARRAY);

    private static Collection<String> create() {
        return new ConstantArrayCollection<>(ARRAY.clone());
    }

    @Test
    void testToString() {
        // Empty
        assertEquals(Set.of().toString(), new ConstantArrayCollection<>(new Object[0]).toString());

        // Normal
        assertEquals(REF.toString(), create().toString());
    }

    @Test
    void testEquals() {
        final var c = create();

        assertTrue(c.containsAll(REF));
        assertTrue(REF.containsAll(c));
        assertTrue(Iterables.elementsEqual(REF, c));
    }

    @Test
    @SuppressWarnings("CollectionIncompatibleType")
    void testSimpleOperations() {
        final var c = create();

        assertEquals(ARRAY.length, c.size());
        assertFalse(c.isEmpty());
        assertTrue(c.contains("ccc"));
        assertFalse(c.contains(""));
        assertFalse(c.contains(1));

        assertTrue(c.containsAll(List.of()));
        assertFalse(c.containsAll(Set.of("")));
        assertFalse(c.containsAll(Set.of(1)));
    }

    @Test
    void testProtection() {
        final var c = create();

        assertThrows(UnsupportedOperationException.class, () -> c.add(null));
        assertThrows(UnsupportedOperationException.class, () -> c.remove(null));
        assertThrows(UnsupportedOperationException.class, () -> c.addAll(null));
        assertThrows(UnsupportedOperationException.class, () -> c.removeAll(null));
        assertThrows(UnsupportedOperationException.class, () -> c.retainAll(null));
        assertThrows(UnsupportedOperationException.class, () -> c.clear());
    }
}
