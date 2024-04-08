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

import java.io.IOException;
import java.util.Set;
import javax.xml.transform.dom.DOMSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class NormalizedNodeWriterTest {
    private final QNameModule bazModule = QNameModule.of("baz-namespace", "1970-01-01");
    private final QName myKeyedList = QName.create(bazModule, "my-keyed-list");
    private final QName myKeyLeaf = QName.create(bazModule, "my-key-leaf");
    private final QName myLeafList = QName.create(bazModule, "my-leaf-list");

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

            doReturn(new NodeWithValue<>(myLeafList, "leaflist-value-1")).when(leafSetEntry).name();
            doReturn("leaflist-value-1").when(leafSetEntry).body();
            assertNotNull(writer.write(leafSetEntry));

            doReturn("leaf-value-1").when(leaf).body();
            assertNotNull(writer.write(leaf));

            doCallRealMethod().when(anyxml).bodyObjectModel();
            doReturn(new DOMSource()).when(anyxml).body();
            assertNotNull(writer.write(anyxml));

            assertNotNull(writer.write(container));

            doReturn(NodeIdentifierWithPredicates.of(myKeyedList, myKeyLeaf, "list-key-value-1"))
                .when(mapEntry).name();
            doReturn(null).when(mapEntry).childByArg(any(NodeIdentifier.class));
            assertNotNull(writer.write(mapEntry));

            assertNotNull(writer.write(unkeyedListEntry));

            assertNotNull(writer.write(choice));

            final var value = Set.of(unkeyedListEntry);
            doReturn(value).when(unkeyedList).body();
            assertNotNull(writer.write(unkeyedList));

            assertNotNull(writer.write(userMap));

            assertNotNull(writer.write(systemMap));

            assertNotNull(writer.write(userLeafSet));

            assertNotNull(writer.write(systemLeafSet));

            writer.flush();
        }

        try (var writer = NormalizedNodeWriter.forStreamWriter(loggingWriter, false)) {
            assertNotNull(writer.write(mapEntry));
        }
    }
}
