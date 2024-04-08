/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@ExtendWith(MockitoExtension.class)
public class YangInstanceIdentifierWriterTest {
    private static EffectiveModelContext CONTEXT;

    @Mock
    private ContainerNode containerNode;
    @Mock
    private LeafNode<String> leafNode;
    @Mock
    private LeafSetEntryNode<String> leafSetEntry1;
    @Mock
    private LeafSetEntryNode<String> leafSetEntry2;

    @BeforeAll
    public static void beforeAll() {
        CONTEXT = YangParserTestUtils.parseYang("""
            module test {
              namespace test;
              prefix test;

              container container-1 {
                container container-2 {
                  container container-3 {
                    container payload-container {
                      leaf payload-leaf {
                        type string;
                      }
                    }
                  }
                }
              }

              choice choice-node {
                container container-in-case;
              }

              list list-1 {
                key list-1-key;
                leaf list-1-key {
                  type string;
                }
                container container-1;
              }

              leaf-list list-list {
                type string;
              }
            }""", """
            module augment {
              namespace augment-namespace;
              prefix aug;

              import test {
                prefix test;
              }

              augment /test:container-1 {
                container augmented-container {
                  container container-2;
                }
              }
            }""");
    }

    @Test
    public void testYangInstanceIdentifierWriter() throws IOException {
        final FormattingNormalizedNodeStreamWriter streamWriter = new FormattingNormalizedNodeStreamWriter();

        final YangInstanceIdentifier path = YangInstanceIdentifier.builder()
            .node(QName.create("test", "container-1"))
            .node(QName.create("test", "container-2"))
            .node(QName.create("test", "container-3"))
            .build();

        try (var iidWriter = YangInstanceIdentifierWriter.open(streamWriter, CONTEXT, path)) {
            try (var nnWriter = new NormalizedNodeWriter(streamWriter)) {
                nnWriter.write(mockedPayload());
            }
        }

        assertEquals("""
            (test)container-1(container)
              (test)container-2(container)
                (test)container-3(container)
                  (test)payload-container(container)
                    (test)payload-leaf(leaf)
                      (String)=leaf-value
                    (end)
                  (end)
                (end)
              (end)
            (end)
            """, streamWriter.result());
    }

    @Test
    public void testAugmentationIdentifier() throws IOException {
        final FormattingNormalizedNodeStreamWriter streamWriter = new FormattingNormalizedNodeStreamWriter();

        final QName augmented = QName.create("augment-namespace", "augmented-container");

        final YangInstanceIdentifier path = YangInstanceIdentifier.builder()
            .node(QName.create("test", "container-1"))
            .node(augmented)
            .node(QName.create(augmented, "container-2"))
            .build();

        try (var iidWriter = YangInstanceIdentifierWriter.open(streamWriter, CONTEXT, path)) {
            try (var nnWriter = new NormalizedNodeWriter(streamWriter)) {
                nnWriter.write(mockedPayload());
            }
        }

        assertEquals("""
            (test)container-1(container)
              (augment-namespace)augmented-container(container)
                (augment-namespace)container-2(container)
                  (test)payload-container(container)
                    (test)payload-leaf(leaf)
                      (String)=leaf-value
                    (end)
                  (end)
                (end)
              (end)
            (end)
            """, streamWriter.result());
    }

    @Test
    public void testMapIdentifier() throws IOException {
        final FormattingNormalizedNodeStreamWriter streamWriter = new FormattingNormalizedNodeStreamWriter();

        final QName listQname = QName.create("test", "list-1");

        final YangInstanceIdentifier path = YangInstanceIdentifier.builder()
            .node(listQname)
            .nodeWithKey(listQname, QName.create("test", "list-1-key"), "test-list-entry")
            .node(QName.create("test", "container-1"))
            .build();

        try (var iidWriter = YangInstanceIdentifierWriter.open(streamWriter, CONTEXT, path)) {
            try (var nnWriter = new NormalizedNodeWriter(streamWriter)) {
                nnWriter.write(mockedPayload());
            }
        }

        assertEquals("""
            (test)list-1(key)
              (test)list-1[{(test)list-1-key=test-list-entry}][](key)
                (test)container-1(container)
                  (test)payload-container(container)
                    (test)payload-leaf(leaf)
                      (String)=leaf-value
                    (end)
                  (end)
                (end)
              (end)
            (end)
            """, streamWriter.result());
    }

    @Test
    public void testChoiceIdentifier() throws IOException {
        final FormattingNormalizedNodeStreamWriter streamWriter = new FormattingNormalizedNodeStreamWriter();

        final YangInstanceIdentifier path = YangInstanceIdentifier.builder()
            .node(QName.create("test", "choice-node"))
            .node(QName.create("test", "container-in-case"))
            .build();

        try (var iidWriter = YangInstanceIdentifierWriter.open(streamWriter, CONTEXT, path)) {
            try (var nnWriter = new NormalizedNodeWriter(streamWriter)) {
                nnWriter.write(mockedPayload());
            }
        }

        assertEquals("""
            (test)choice-node(choice)
              (test)container-in-case(container)
                (test)payload-container(container)
                  (test)payload-leaf(leaf)
                    (String)=leaf-value
                  (end)
                (end)
              (end)
            (end)
            """, streamWriter.result());
    }

    @Test
    public void testLeafSetIdentifier() throws IOException {
        final FormattingNormalizedNodeStreamWriter streamWriter = new FormattingNormalizedNodeStreamWriter();

        final YangInstanceIdentifier path = YangInstanceIdentifier.builder()
            .node(QName.create("test", "list-list"))
            .build();

        try (var iidWriter = YangInstanceIdentifierWriter.open(streamWriter, CONTEXT, path)) {
            try (var nnWriter = new NormalizedNodeWriter(streamWriter)) {
                final var leafQname = QName.create("test", "leaf");

                doReturn(new NodeWithValue<>(leafQname, "test-value")).when(leafSetEntry1).name();
                doReturn("test-value").when(leafSetEntry1).body();
                nnWriter.write(leafSetEntry1);

                doReturn(new NodeWithValue<>(leafQname, "test-value-2")).when(leafSetEntry2).name();
                doReturn("test-value-2").when(leafSetEntry2).body();
                nnWriter.write(leafSetEntry2);
            }
        }

        assertEquals("""
            (test)list-list(leaf-list)
              (test)leaf(entry)
                (String)=test-value
              (end)
              (test)leaf(entry)
                (String)=test-value-2
              (end)
            (end)
            """, streamWriter.result());
    }

    private ContainerNode mockedPayload() {
        doReturn(new NodeIdentifier(QName.create("test", "payload-container"))).when(containerNode).name();
        doReturn(Set.of(leafNode)).when(containerNode).body();
        doReturn(new NodeIdentifier(QName.create("test", "payload-leaf"))).when(leafNode).name();
        doReturn("leaf-value").when(leafNode).body();
        return containerNode;
    }
}
