/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

@RunWith(Parameterized.class)
public class MapEntrySerializationTest extends AbstractSerializationTest {
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Collections.singletonList(
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM, 96, 110, 125, 3_927 });
    }

    @Parameter(1)
    public int emptySize;
    @Parameter(2)
    public int oneSize;
    @Parameter(3)
    public int twoSize;
    @Parameter(4)
    public int size256;

    @Test
    public void testEmptyIdentifier() {
        assertEquals(createEntry(0), emptySize);
    }

    @Test
    public void testOneIdentifier() {
        assertEquals(createEntry(1), oneSize);
    }

    @Test
    public void testTwoIdentifiers() {
        assertEquals(createEntry(2), twoSize);
    }

    @Test
    public void test256() {
        assertEquals(createEntry(256), size256);
    }

    private static MapEntryNode createEntry(final int size) {
        final DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> builder = Builders.mapEntryBuilder();
        final Map<QName, Object> predicates = Maps.newHashMapWithExpectedSize(size);
        for (QName qname : generateQNames(size)) {
            builder.withChild(ImmutableNodes.leafNode(qname, "a"));
            predicates.put(qname, "a");
        }

        return builder.withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.TEST_QNAME, predicates)).build();
    }
}
