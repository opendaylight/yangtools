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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import javax.xml.transform.dom.DOMSource;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;

public class NormalizedNodeWriterTest {

    private QNameModule bazModule;

    private QName myKeyedList;
    private QName myKeyLeaf;
    private QName myLeafList;

    @Before
    public void setUp() {
        bazModule = QNameModule.create(URI.create("baz-namespace"), Revision.of("1970-01-01"));
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

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> orderedNormalizedNodeWriter.write(mock(NormalizedNode.class)));
        assertTrue(ex.getMessage().startsWith("It wasn't possible to serialize node"));

        final LeafSetEntryNode<?> mockedLeafSetEntryNode = mock(LeafSetEntryNode.class);
        doReturn(new NodeWithValue<>(myLeafList, "leaflist-value-1")).when(mockedLeafSetEntryNode).getIdentifier();
        doReturn("leaflist-value-1").when(mockedLeafSetEntryNode).body();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedLeafSetEntryNode));

        final LeafNode<?> mockedLeafNode = mock(LeafNode.class);
        doReturn("leaf-value-1").when(mockedLeafNode).body();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedLeafNode));

        final DOMSourceAnyxmlNode mockedAnyXmlNode = mock(DOMSourceAnyxmlNode.class);
        doCallRealMethod().when(mockedAnyXmlNode).bodyObjectModel();
        doReturn(new DOMSource()).when(mockedAnyXmlNode).body();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedAnyXmlNode));

        final NormalizedNode mockedContainerNode = mock(ContainerNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedContainerNode));

        final NormalizedNode mockedYangModeledAnyXmlNode = mock(YangModeledAnyXmlNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedYangModeledAnyXmlNode));

        final MapEntryNode mockedMapEntryNode = mock(MapEntryNode.class);
        doReturn(NodeIdentifierWithPredicates.of(myKeyedList, myKeyLeaf, "list-key-value-1"))
                .when(mockedMapEntryNode).getIdentifier();
        doReturn(null).when(mockedMapEntryNode).childByArg(any(NodeIdentifier.class));
        assertNotNull(orderedNormalizedNodeWriter.write(mockedMapEntryNode));

        final UnkeyedListEntryNode mockedUnkeyedListEntryNode = mock(UnkeyedListEntryNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedUnkeyedListEntryNode));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(ChoiceNode.class)));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(AugmentationNode.class)));

        final UnkeyedListNode mockedUnkeyedListNode = mock(UnkeyedListNode.class);
        final Set<?> value = Set.of(mockedUnkeyedListEntryNode);
        doReturn(value).when(mockedUnkeyedListNode).body();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedUnkeyedListNode));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(UserMapNode.class)));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(SystemMapNode.class)));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(UserLeafSetNode.class)));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(SystemLeafSetNode.class)));

        orderedNormalizedNodeWriter.flush();
        orderedNormalizedNodeWriter.close();

        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                loggingNormalizedNodeStreamWriter, false);

        assertNotNull(normalizedNodeWriter.write(mockedMapEntryNode));

        normalizedNodeWriter.flush();
        normalizedNodeWriter.close();
    }
}
