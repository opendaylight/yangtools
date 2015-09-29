/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertTrue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.test.mock.Nodes;

public class DataObjectReadingUtilTest {
    @Mock private InstanceIdentifier<? extends DataObject> pathNull;
    @Mock private Map.Entry<InstanceIdentifier<? extends DataObject>, DataObject> entryNull;
    @Mock private DataObject mockedDataObject;
    private InstanceIdentifier<? extends DataObject> path;
    private Map.Entry<InstanceIdentifier<? extends DataObject>, DataObject> entry;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        path = InstanceIdentifier.builder(Nodes.class).build();
        ImmutableMap<InstanceIdentifier<? extends DataObject>, DataObject> map =
                ImmutableMap.<InstanceIdentifier<? extends DataObject>, DataObject>builder()
                .put(path, mockedDataObject).build();

        ImmutableSet<Entry<InstanceIdentifier<? extends DataObject>, DataObject>> entries = map.entrySet();
        UnmodifiableIterator<Entry<InstanceIdentifier<? extends DataObject>, DataObject>> it = entries.iterator();
        while(it.hasNext()) {
            entry = it.next();
        }
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testReadDataParentNull() {
        DataObjectReadingUtil.readData(entryNull.getValue(), (InstanceIdentifier<DataObject>) entryNull.getKey(), pathNull);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testReadDataParentPathNull() {
        DataObjectReadingUtil.readData(entry.getValue(), (InstanceIdentifier<DataObject>) entryNull.getKey(), pathNull);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadDataWithThreeParams() {
        assertTrue("Check if contains key",
                DataObjectReadingUtil.readData(entry.getValue(),
                        (InstanceIdentifier<DataObject>) entry.getKey(), path).containsKey(entry.getKey()));

        assertTrue("Check if contains value",
                DataObjectReadingUtil.readData(entry.getValue(),
                        (InstanceIdentifier<DataObject>) entry.getKey(), path).containsValue(entry.getValue()));
    }

    @Test(expected = NullPointerException.class)
    public void testReadDataWithTwoParams() {
        DataObjectReadingUtil.readData(mockedDataObject, DataObject.class);
    }
}