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
import org.junit.jupiter.api.Test;

class LazyCollectionsTest {
    @Test
    void testLazyAddMethod() {
        final var list = new ArrayList<Integer>();
        var anotherList = LazyCollections.lazyAdd(list, 5);
        assertEquals(1, anotherList.size());

        anotherList = LazyCollections.lazyAdd(anotherList, 4);
        assertEquals(2, anotherList.size());

        anotherList = LazyCollections.lazyAdd(anotherList, 3);
        assertEquals(3, anotherList.size());
    }
}
