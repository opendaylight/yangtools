/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.collect.Maps;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class MapEntrySerializationTest extends AbstractSerializationTest {
    @ParameterizedTest
    @MethodSource
    void testEmptyIdentifier(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createEntry(0), size);
    }

    static List<Arguments> testEmptyIdentifier() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 96));
    }

    @ParameterizedTest
    @MethodSource
    void testOneIdentifier(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createEntry(1), size);
    }

    static List<Arguments> testOneIdentifier() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 110));
    }

    @ParameterizedTest
    @MethodSource
    void testTwoIdentifiers(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createEntry(2), size);
    }

    static List<Arguments> testTwoIdentifiers() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 125));
    }

    @ParameterizedTest
    @MethodSource
    void test256(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createEntry(256), size);
    }

    static List<Arguments> test256() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 3_927));
    }

    private static MapEntryNode createEntry(final int size) {
        final var builder = Builders.mapEntryBuilder();
        final var predicates = Maps.<QName, Object>newHashMapWithExpectedSize(size);
        for (var qname : generateQNames(size)) {
            builder.withChild(ImmutableNodes.leafNode(qname, "a"));
            predicates.put(qname, "a");
        }

        return builder.withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.TEST_QNAME, predicates)).build();
    }
}
