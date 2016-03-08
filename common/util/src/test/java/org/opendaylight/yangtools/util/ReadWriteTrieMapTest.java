/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.romix.scala.collection.concurrent.TrieMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;

public class ReadWriteTrieMapTest {

    @Test
    public void testMethodsOfReadWriteTrieMap() {
        final TrieMap<String, String> trieMap = new TrieMap<>();
        trieMap.put("0", "zero");
        trieMap.put("1", "one");

        final ReadWriteTrieMap<String, String> readWriteTrieMap = new ReadWriteTrieMap<String, String>(trieMap, 5);

        assertNotNull("Object readOnlyTrieMap shouldn't be 'null'.", readWriteTrieMap);

        assertEquals("Size of readOnlyTrieMap should be '5'.", 5, readWriteTrieMap.size());
        assertFalse("Object readOnlyTrieMap shouldn't be empty.", readWriteTrieMap.isEmpty());

        assertTrue("Object readOnlyTrieMap should have key '0'.", readWriteTrieMap.containsKey("0"));
        assertTrue("Object readOnlyTrieMap should have value 'zero'.", readWriteTrieMap.containsValue("zero"));
        assertEquals("Object readOnlyTrieMap should have value 'zero'.", "zero", readWriteTrieMap.get("0"));

        final Map<String, String> rwMap = readWriteTrieMap;
        rwMap.put("2", "two");
        rwMap.put("3", "three");

        assertEquals("Removed value from readOnlyTrieMap should be 'one'.", "one", rwMap.remove("1"));

        final Set<String> trieMapKeySet = readWriteTrieMap.keySet();
        assertEquals("Size of keySet should be '3'.", 3, Iterables.size(trieMapKeySet));

        final Collection<String> trieMapValues = readWriteTrieMap.values();
        assertEquals("Size of values should be '3'.", 3, Iterables.size(trieMapValues));

        assertEquals("Entry set of readWriteTrieMap and trieMap should by equals.", convertSetEntryToMap(readWriteTrieMap.entrySet()), trieMap);

        trieMap.put("2", "two");
        final ReadWriteTrieMap<String, String> readWriteTrieMap2 = new ReadWriteTrieMap<String, String>(trieMap, 4);

        assertFalse("Objects readWriteTrieMap and readOnlyTrieMap2 should be different.", readWriteTrieMap.equals(readWriteTrieMap2));
        assertFalse("Hash codes of object readWriteTrieMap and readOnelyTrieMap2 should be different.", readWriteTrieMap.hashCode() == readWriteTrieMap2.hashCode());

        final Map<String, String> readOnlyTrieMap = readWriteTrieMap.toReadOnly();

        readWriteTrieMap.clear();
        assertEquals("Size of readOnlyTrieMap should be '0'.", 0, readWriteTrieMap.size());
    }

    public Map<String, String> convertSetEntryToMap(Set<Entry<String, String>> input) {
        Map<String, String> resultMap = Maps.newHashMap();
        for (Entry<String, String> entry : input) {
            resultMap.put(entry.getKey(), entry.getValue());
        }
        return resultMap;
    }
}
