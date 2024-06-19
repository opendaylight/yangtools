/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChangedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.EventInstantAware;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

public class NotificationProcessingTest extends AbstractBindingCodecTest {
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
    public void testNotificationToNormalized() {
        final var dom = codecContext.toNormalizedNodeNotification(createTestBindingData());
        assertEquals(createTestDomData(), dom);
    }

    @Test
    public void testNormalizedToNotification() {
        final var bindingDeserialized = codecContext.fromNormalizedNodeNotification(
            Absolute.of(TwoLevelListChanged.QNAME), createTestDomData());
        assertTrue(bindingDeserialized instanceof TwoLevelListChanged);
        assertEquals(createTestBindingData(), bindingDeserialized);
    }

    @Test
    public void testNormalizedToNotificationWithInstant() {
        final var instant = Instant.now();
        final var bindingDeserialized = codecContext.fromNormalizedNodeNotification(
            Absolute.of(TwoLevelListChanged.QNAME), createTestDomData(), instant);
        assertTrue(bindingDeserialized instanceof TwoLevelListChanged);
        assertEquals(createTestBindingData(), bindingDeserialized);
        assertTrue(bindingDeserialized instanceof EventInstantAware);
        assertSame(instant, ((EventInstantAware) bindingDeserialized).eventInstant());
    }

    @Test
    public void testNormalizedToNotificationWithNull() {
        final var bindingDeserialized = codecContext.fromNormalizedNodeNotification(
            Absolute.of(TwoLevelListChanged.QNAME), createTestDomData(), null);
        assertTrue(bindingDeserialized instanceof TwoLevelListChanged);
        assertEquals(createTestBindingData(), bindingDeserialized);
    }
}
