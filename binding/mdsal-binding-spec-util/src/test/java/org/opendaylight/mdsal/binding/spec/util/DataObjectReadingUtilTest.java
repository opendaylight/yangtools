/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.opendaylight.mdsal.binding.spec.util.DataObjectReadingUtil.readData;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Deprecated(since = "11.0.3", forRemoval = true)
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DataObjectReadingUtilTest {
    @Mock
    public InstanceIdentifier<? extends DataObject> pathNull;
    @Mock
    public Map.Entry<InstanceIdentifier<? extends DataObject>, DataObject> entryNull;
    @Mock
    public DataObject mockedDataObject;

    public InstanceIdentifier<? extends DataObject> path;
    public Map.Entry<InstanceIdentifier<? extends DataObject>, DataObject> entry;

    @Before
    public void setup() {
        path = InstanceIdentifier.builder(Nodes.class).build();
        final ImmutableMap<InstanceIdentifier<? extends DataObject>, DataObject> map =
                ImmutableMap.<InstanceIdentifier<? extends DataObject>, DataObject>builder()
                .put(path, mockedDataObject).build();

        final ImmutableSet<Entry<InstanceIdentifier<? extends DataObject>, DataObject>> entries = map.entrySet();
        final UnmodifiableIterator<Entry<InstanceIdentifier<? extends DataObject>, DataObject>> it = entries.iterator();
        while (it.hasNext()) {
            entry = it.next();
        }
    }

    @Test
    public void testReadData() throws Exception {
        final Nodes nodes = mock(Nodes.class);
        doReturn(Nodes.class).when(nodes).implementedInterface();
        doReturn(null).when(nodes).getNode();
        entry = ImmutableMap.<InstanceIdentifier<? extends DataObject>, DataObject>builder()
                .put(path, nodes).build().entrySet().iterator().next();
        path = InstanceIdentifier.builder(Nodes.class).child(Node.class).build();
        assertTrue(DataObjectReadingUtil.readData(entry.getValue(),
                (InstanceIdentifier<DataObject>) entry.getKey(), path).isEmpty());

        final Iterable<Identifiable> iterable = ImmutableList.of();
        doReturn(iterable).when(nodes).getNode();
        assertTrue(DataObjectReadingUtil.readData(entry.getValue(),
                (InstanceIdentifier<DataObject>) entry.getKey(), path).isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testReadDataParentNull() {
        readData(entryNull.getValue(), (InstanceIdentifier<DataObject>) entryNull.getKey(), pathNull);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testReadDataParentPathNull() {
        readData(entry.getValue(), (InstanceIdentifier<DataObject>) entryNull.getKey(), pathNull);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadDataWithThreeParams() {
        assertTrue("Check if contains key",
                readData(entry.getValue(),
                        (InstanceIdentifier<DataObject>) entry.getKey(), path).containsKey(entry.getKey()));

        assertTrue("Check if contains value",
                readData(entry.getValue(),
                        (InstanceIdentifier<DataObject>) entry.getKey(), path).containsValue(entry.getValue()));
    }

    @Test(expected = NullPointerException.class)
    public void testReadDataWithTwoParams() {
        readData(mockedDataObject, DataObject.class);
    }
}