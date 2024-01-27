/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Set;
import javax.xml.transform.dom.DOMSource;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;

class NormalizedNodeWriterTest {
    private final QNameModule bazModule = QNameModule.of("baz-namespace", "1970-01-01");
    private final QName myKeyedList = QName.create(bazModule, "my-keyed-list");
    private final QName myKeyLeaf = QName.create(bazModule, "my-key-leaf");
    private final QName myLeafList = QName.create(bazModule, "my-leaf-list");

    @Test
    void testNormalizedNodeWriter() throws IOException {
        final var loggingNormalizedNodeStreamWriter = new LoggingNormalizedNodeStreamWriter();
        final var orderedNormalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                loggingNormalizedNodeStreamWriter);

        assertEquals(loggingNormalizedNodeStreamWriter, orderedNormalizedNodeWriter.getWriter());

        final var mockedLeafSetEntryNode = mock(LeafSetEntryNode.class);
        doReturn(new NodeWithValue<>(myLeafList, "leaflist-value-1")).when(mockedLeafSetEntryNode).name();
        doReturn("leaflist-value-1").when(mockedLeafSetEntryNode).body();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedLeafSetEntryNode));

        final var mockedLeafNode = mock(LeafNode.class);
        doReturn("leaf-value-1").when(mockedLeafNode).body();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedLeafNode));

        final var mockedAnyXmlNode = mock(DOMSourceAnyxmlNode.class);
        doCallRealMethod().when(mockedAnyXmlNode).bodyObjectModel();
        doReturn(new DOMSource()).when(mockedAnyXmlNode).body();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedAnyXmlNode));

        final var mockedContainerNode = mock(ContainerNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedContainerNode));

        final var mockedMapEntryNode = mock(MapEntryNode.class);
        doReturn(NodeIdentifierWithPredicates.of(myKeyedList, myKeyLeaf, "list-key-value-1"))
                .when(mockedMapEntryNode).name();
        doReturn(null).when(mockedMapEntryNode).childByArg(any(NodeIdentifier.class));
        assertNotNull(orderedNormalizedNodeWriter.write(mockedMapEntryNode));

        final var mockedUnkeyedListEntryNode = mock(UnkeyedListEntryNode.class);
        assertNotNull(orderedNormalizedNodeWriter.write(mockedUnkeyedListEntryNode));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(ChoiceNode.class)));

        final var mockedUnkeyedListNode = mock(UnkeyedListNode.class);
        final var value = Set.of(mockedUnkeyedListEntryNode);
        doReturn(value).when(mockedUnkeyedListNode).body();
        assertNotNull(orderedNormalizedNodeWriter.write(mockedUnkeyedListNode));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(UserMapNode.class)));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(SystemMapNode.class)));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(UserLeafSetNode.class)));

        assertNotNull(orderedNormalizedNodeWriter.write(mock(SystemLeafSetNode.class)));

        orderedNormalizedNodeWriter.flush();
        orderedNormalizedNodeWriter.close();

        try (var nnWriter = NormalizedNodeWriter.forStreamWriter(loggingNormalizedNodeStreamWriter, false)) {
            assertNotNull(nnWriter.write(mockedMapEntryNode));
        }
    }
}
