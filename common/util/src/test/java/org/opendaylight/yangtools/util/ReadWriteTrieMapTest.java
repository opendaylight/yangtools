/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;
import tech.pantheon.triemap.MutableTrieMap;
import tech.pantheon.triemap.TrieMap;

public class ReadWriteTrieMapTest {

    @Test
    public void testMethodsOfReadWriteTrieMap() {
        final MutableTrieMap<String, String> trieMap = TrieMap.create();
        trieMap.put("0", "zero");
        trieMap.put("1", "one");

        final ReadWriteTrieMap<String, String> readWriteTrieMap = new ReadWriteTrieMap<>(trieMap, 5);

        assertNotNull(readWriteTrieMap, "Object readOnlyTrieMap shouldn't be 'null'.");

        assertEquals(5, readWriteTrieMap.size(), "Size of readOnlyTrieMap should be '5'.");
        assertFalse(readWriteTrieMap.isEmpty(), "Object readOnlyTrieMap shouldn't be empty.");

        assertTrue(readWriteTrieMap.containsKey("0"), "Object readOnlyTrieMap should have key '0'.");
        assertTrue(readWriteTrieMap.containsValue("zero"), "Object readOnlyTrieMap should have value 'zero'.");
        assertEquals("zero", readWriteTrieMap.get("0"), "Object readOnlyTrieMap should have value 'zero'.");

        final Map<String, String> rwMap = readWriteTrieMap;
        rwMap.put("2", "two");
        rwMap.put("3", "three");

        assertEquals("one", rwMap.remove("1"), "Removed value from readOnlyTrieMap should be 'one'.");

        final Set<String> trieMapKeySet = readWriteTrieMap.keySet();
        assertEquals(3, trieMapKeySet.size(), "Size of keySet should be '3'.");

        final Collection<String> trieMapValues = readWriteTrieMap.values();
        assertEquals(3, trieMapValues.size(), "Size of values should be '3'.");

        assertEquals(convertSetEntryToMap(readWriteTrieMap.entrySet()), trieMap);

        trieMap.put("2", "two");
        final ReadWriteTrieMap<String, String> readWriteTrieMap2 = new ReadWriteTrieMap<>(trieMap, 4);

        assertNotEquals(readWriteTrieMap, readWriteTrieMap2);
        assertEquals(readWriteTrieMap.hashCode(), readWriteTrieMap2.hashCode());

        final Map<String, String> readOnlyTrieMap = readWriteTrieMap.toReadOnly();
        readWriteTrieMap.clear();
        assertEquals(0, readWriteTrieMap.size());
        assertEquals(6, readOnlyTrieMap.size());
    }

    private static Map<String, String> convertSetEntryToMap(final Set<Entry<String, String>> input) {
        Map<String, String> resultMap = new HashMap<>();
        for (Entry<String, String> entry : input) {
            resultMap.put(entry.getKey(), entry.getValue());
        }
        return resultMap;
    }
}
