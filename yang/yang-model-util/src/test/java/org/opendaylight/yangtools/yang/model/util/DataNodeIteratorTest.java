/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

import java.util.Collections;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class DataNodeIteratorTest {

    private DataNodeIterator dataNodeIterator;

    @Before
    public void setUp() {
        DataNodeContainer dataNodeContainer = mock(DataNodeContainer.class);
        this.dataNodeIterator = new DataNodeIterator(dataNodeContainer);
    }

    @Test(expected=IllegalArgumentException.class)
    public void createDataNodeIteratorWithNullArgument() {
        new DataNodeIterator(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void removeFromEmptyDataNodeContainer() {
        dataNodeIterator.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void tryNextOnEmptyDataContainer() {
        dataNodeIterator.next();
    }

    @Test
    public void createDataNodeIteratorWith() {
        assertFalse("Has no next", dataNodeIterator.hasNext());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allChoices());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allContainers());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allTypedefs());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allGroupings());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allLists());
    }
}