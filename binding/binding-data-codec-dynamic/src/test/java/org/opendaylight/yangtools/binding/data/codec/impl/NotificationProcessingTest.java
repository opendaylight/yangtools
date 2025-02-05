/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChangedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.EventInstantAware;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

class NotificationProcessingTest extends AbstractBindingCodecTest {
    private static final QName NAME = QName.create(TopLevelList.QNAME, "name");

    private static TwoLevelListChanged createTestBindingData() {
        return new TwoLevelListChangedBuilder()
            .setTopLevelList(BindingMap.of(new TopLevelListBuilder().withKey(new TopLevelListKey("test")).build()))
            .build();
    }

    private static ContainerNode createTestDomData() {
        return ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(TwoLevelListChanged.QNAME))
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(TopLevelList.QNAME))
                    .withChild(ImmutableNodes.newMapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(TopLevelList.QNAME, NAME, "test"))
                        .build())
                    .build())
                .build();
    }


    @Test
    void testNotificationToNormalized() {
        final var dom = codecContext.toNormalizedNodeNotification(createTestBindingData());
        assertEquals(createTestDomData(), dom);
    }

    @Test
    void testNormalizedToNotification() {
        final var bindingDeserialized = assertInstanceOf(TwoLevelListChanged.class,
            codecContext.fromNormalizedNodeNotification(Absolute.of(TwoLevelListChanged.QNAME), createTestDomData()));
        assertEquals(createTestBindingData(), bindingDeserialized);
    }

    @Test
    void testNormalizedToNotificationWithInstant() {
        final var instant = Instant.now();
        final var bindingDeserialized = assertInstanceOf(TwoLevelListChanged.class,
            codecContext.fromNormalizedNodeNotification(
                Absolute.of(TwoLevelListChanged.QNAME), createTestDomData(), instant));
        assertEquals(createTestBindingData(), bindingDeserialized);
        assertSame(instant, assertInstanceOf(EventInstantAware.class, bindingDeserialized).eventInstant());
    }

    @Test
    void testNormalizedToNotificationWithNull() {
        final var bindingDeserialized = assertInstanceOf(TwoLevelListChanged.class,
            codecContext.fromNormalizedNodeNotification(
                Absolute.of(TwoLevelListChanged.QNAME), createTestDomData(), null));
        assertEquals(createTestBindingData(), bindingDeserialized);
    }
}
