/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Set;
import javax.xml.transform.dom.DOMSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
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

@ExtendWith(MockitoExtension.class)
class NormalizedNodeWriterTest {
    private static final NodeIdentifier NODE_ID = new NodeIdentifier(QName.create("test", "nodeId"));
    private static final QName QNAME1 = QName.create("test", "qname1");
    private static final QName QNAME2 = QName.create("test", "qname2");

    @Mock
    private DOMSourceAnyxmlNode anyxml;
    @Mock
    private ChoiceNode choice;
    @Mock
    private ContainerNode container;
    @Mock
    private LeafNode<String> leaf;
    @Mock
    private LeafSetEntryNode<String> leafSetEntry;
    @Mock
    private MapEntryNode mapEntry;
    @Mock
    private SystemLeafSetNode<String> systemLeafSet;
    @Mock
    private SystemMapNode systemMap;
    @Mock
    private UnkeyedListEntryNode unkeyedListEntry;
    @Mock
    private UnkeyedListNode unkeyedList;
    @Mock
    private UserLeafSetNode<String> userLeafSet;
    @Mock
    private UserMapNode userMap;

    @Test
    void testNormalizedNodeWriter() throws IOException {
        final var loggingWriter = new LoggingNormalizedNodeStreamWriter();
        try (var writer = NormalizedNodeWriter.forStreamWriter(loggingWriter, true)) {

            assertEquals(loggingWriter, writer.getWriter());

            doReturn(new NodeWithValue<>(QNAME1, "leaflist-value-1")).when(leafSetEntry).name();
            doReturn("leaflist-value-1").when(leafSetEntry).body();
            assertSame(writer, writer.write(leafSetEntry));

            doReturn("leaf-value-1").when(leaf).body();
            doReturn(NODE_ID).when(leaf).name();
            assertSame(writer, writer.write(leaf));

            doCallRealMethod().when(anyxml).bodyObjectModel();
            doReturn(NODE_ID).when(anyxml).name();
            doReturn(new DOMSource()).when(anyxml).body();
            assertSame(writer, writer.write(anyxml));

            assertSame(writer, writer.write(container));

            doReturn(NodeIdentifierWithPredicates.of(QNAME1, QNAME2, "list-key-value-1")).when(mapEntry).name();
            doReturn(null).when(mapEntry).childByArg(any(NodeIdentifier.class));
            assertSame(writer, writer.write(mapEntry));

            assertSame(writer, writer.write(unkeyedListEntry));

            assertSame(writer, writer.write(choice));

            final var value = Set.of(unkeyedListEntry);
            doReturn(value).when(unkeyedList).body();
            assertSame(writer, writer.write(unkeyedList));

            assertSame(writer, writer.write(userMap));

            assertSame(writer, writer.write(systemMap));

            assertSame(writer, writer.write(userLeafSet));

            assertSame(writer, writer.write(systemLeafSet));

            writer.flush();
        }

        try (var writer = NormalizedNodeWriter.forStreamWriter(loggingWriter, false)) {
            assertSame(writer, writer.write(mapEntry));
        }
    }
}
