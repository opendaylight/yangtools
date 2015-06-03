/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class StackedPathArgumentsTest {

    static final QName NODE_1_QNAME = QName.create("test", "2014-5-28", "node1");
    static final QName NODE_2_QNAME = QName.create("test", "2014-5-28", "node2");
    static final QName NODE_3_QNAME = QName.create("test", "2014-5-28", "node3");
    static final QName NODE_4_QNAME = QName.create("test", "2014-5-28", "node4");
    static final QName KEY_1_QNAME = QName.create("test", "2014-5-28", "key1");
    static final QName KEY_2_QNAME = QName.create("test", "2014-5-28", "key2");
    static final QName KEY_3_QNAME = QName.create("test", "2014-5-28", "key3");

    @Test
    public void testStackedInstanceIdentifier() {
        final YangInstanceIdentifier stacked = YangInstanceIdentifier.EMPTY
                .node(new NodeIdentifier(NODE_1_QNAME))
                .node(new NodeIdentifier(NODE_2_QNAME))
                .node(new NodeIdentifierWithPredicates(NODE_3_QNAME, KEY_1_QNAME, "test"))
                .node(new AugmentationIdentifier(ImmutableSet.of(KEY_2_QNAME, KEY_3_QNAME)));


        final List<PathArgument> materializedStack = stacked.getPathArguments();

        final StackedPathArguments weirdStacked = new StackedPathArguments(stacked, Collections.<PathArgument>emptyList());
        assertTrue(weirdStacked.iterator().hasNext());
        assertTrue(Iterables.elementsEqual(materializedStack, weirdStacked));
        assertTrue(Iterators.elementsEqual(materializedStack.iterator(), weirdStacked.iterator()));
    }

}
