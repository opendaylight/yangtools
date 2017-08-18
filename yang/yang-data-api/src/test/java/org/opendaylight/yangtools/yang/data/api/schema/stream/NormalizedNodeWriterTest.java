/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;

public class NormalizedNodeWriterTest {

    private QNameModule bazModule;

    private QName myKeyedList;
    private QName myKeyLeaf;
    private QName myLeafList;

    @Before
    public void setUp() throws URISyntaxException, ParseException, UnsupportedEncodingException {
        bazModule = QNameModule.create(new URI("baz-namespace"), SimpleDateFormatUtil.getRevisionFormat()
                .parse("1970-01-01"));

        myKeyedList = QName.create(bazModule, "my-keyed-list");
        myKeyLeaf = QName.create(bazModule, "my-key-leaf");
        myLeafList = QName.create(bazModule, "my-leaf-list");
    }

    @Test
    public void testNormalizedNodeWriter() throws IOException {
        final NormalizedNodeStreamWriter loggingNormalizedNodeStreamWriter = new LoggingNormalizedNodeStreamWriter();
        final NormalizedNodeWriter orderedNormalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                loggingNormalizedNodeStreamWriter);

        assertEquals(loggingNormalizedNodeStreamWriter, orderedNormalizedNodeWriter.getWriter());

        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
        try {
            orderedNormalizedNodeWriter.write(mockedNormalizedNode);
            fail("An IllegalStateException should have been thrown!");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().startsWith("It wasn't possible to serialize node"));
        }

        final NormalizedNode<?, ?> mockedLeafSetEntryNode = mock(LeafSetEntryNode.class);
        doReturn(new NodeWithValue<>(myLeafList, "leaflist-value-1")).when(mockedLeafSetEntryNode).getIdentifier();
        doReturn("leaflist-value-1").when(mockedLeafSetEntryNode).getValue();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedLeafSetEntryNode));

        final NormalizedNode<?, ?> mockedLeafNode = mock(LeafNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedLeafNode));
        doReturn("leaf-value-1").when(mockedLeafNode).getValue();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedLeafNode));

        final NormalizedNode<?, ?> mockedAnyXmlNode = mock(AnyXmlNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedAnyXmlNode));

        final NormalizedNode<?, ?> mockedContainerNode = mock(ContainerNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedContainerNode));

        final NormalizedNode<?, ?> mockedYangModeledAnyXmlNode = mock(YangModeledAnyXmlNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedYangModeledAnyXmlNode));

        final MapEntryNode mockedMapEntryNode = mock(MapEntryNode.class);
        doReturn(new NodeIdentifierWithPredicates(myKeyedList, myKeyLeaf, "list-key-value-1"))
                .when(mockedMapEntryNode).getIdentifier();
        doReturn(Optional.empty()).when(mockedMapEntryNode).getChild(any(NodeIdentifier.class));
        assertNotNull(orderedNormalizedNodeWriter.write(mockedMapEntryNode));

        final UnkeyedListEntryNode mockedUnkeyedListEntryNode = mock(UnkeyedListEntryNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedUnkeyedListEntryNode));

        final ChoiceNode mockedChoiceNode = mock(ChoiceNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedChoiceNode));

        final AugmentationNode mockedAugmentationNode = mock(AugmentationNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedAugmentationNode));

        final UnkeyedListNode mockedUnkeyedListNode = mock(UnkeyedListNode.class);
        final Set<?> value = ImmutableSet.builder().add(mockedUnkeyedListEntryNode).build();
        doReturn(value).when(mockedUnkeyedListNode).getValue();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedUnkeyedListNode));

        final OrderedMapNode mockedOrderedMapNode = mock(OrderedMapNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedOrderedMapNode));

        final MapNode mockedMapNode = mock(MapNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedMapNode));

        final OrderedLeafSetNode<?> mockedOrderedLeafSetNode = mock(OrderedLeafSetNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedOrderedLeafSetNode));

        final LeafSetNode<?> mockedLeafSetNode = mock(LeafSetNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedLeafSetNode));

        orderedNormalizedNodeWriter.flush();
        orderedNormalizedNodeWriter.close();

        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                loggingNormalizedNodeStreamWriter, false);

        assertNotNull(normalizedNodeWriter.write(mockedMapEntryNode));

        normalizedNodeWriter.flush();
        normalizedNodeWriter.close();
    }
}
