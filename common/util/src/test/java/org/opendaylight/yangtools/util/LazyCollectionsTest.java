/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class LazyCollectionsTest {

    @Test
    public void testLazyAddMethod() {
        final List<Integer> list = new ArrayList<>();
        List<Integer> anotherList = LazyCollections.lazyAdd(list, 5);
        assertEquals(1, anotherList.size());

        anotherList = LazyCollections.lazyAdd(anotherList, 4);
        assertEquals(2, anotherList.size());

        anotherList = LazyCollections.lazyAdd(anotherList, 3);
        assertEquals(3, anotherList.size());
    }
}
