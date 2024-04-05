/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1570Test {
    private static final QName INPUT = QName.create("foo", "input");
    private static final QName OUTPUT = QName.create("foo", "output");
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName BAZ = QName.create("foo", "baz");
    private static final QName UINT = QName.create("foo", "uint");

    private static final EffectiveModelContext MODEL_CONTEXT = YangParserTestUtils.parseYang("""
        module foo {
          namespace foo;
          prefix foo;
          yang-version 1.1;

          rpc foo {
            input {
              leaf uint {
                type uint8;
              }
            }
            output {
              leaf uint {
                type uint64;
              }
            }
          }

          container bar {
            action baz {
              input {
                leaf uint {
                  type uint8;
                }
              }
              output {
                leaf uint {
                  type uint64;
                }
              }
            }
          }
        }""");

    @Test
    void testRpcInput() {
        assertEquals("""
            <input xmlns="foo"><uint>1</uint></input>""",
            serialize(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(INPUT))
                .withChild(ImmutableNodes.leafNode(UINT, Uint8.ONE))
                .build(), FOO));
    }

    @Test
    void testRpcOutput() {
        assertEquals("""
            <output xmlns="foo"><uint>1</uint></output>""",
            serialize(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(OUTPUT))
                .withChild(ImmutableNodes.leafNode(UINT, Uint64.ONE))
                .build(), FOO));
    }

    @Test
    void testActionInput() {
        assertEquals("""
            <input xmlns="foo"><uint>2</uint></input>""",
            serialize(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(INPUT))
                .withChild(ImmutableNodes.leafNode(UINT, Uint8.TWO))
                .build(), BAR, BAZ));
    }

    @Test
    void testActionOutput() {
        assertEquals("""
            <output xmlns="foo"><uint>2</uint></output>""",
            serialize(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(OUTPUT))
                .withChild(ImmutableNodes.leafNode(UINT, Uint64.TWO))
                .build(), BAR, BAZ));
    }

    private static String serialize(final ContainerNode container, final QName... nodeIdentifiers) {
        final var writer = new StringWriter();
        try {
            final var xmlStream = XMLStreamNormalizedNodeStreamWriter.create(
                TestFactories.DEFAULT_OUTPUT_FACTORY.createXMLStreamWriter(writer),
                SchemaInferenceStack.of(MODEL_CONTEXT, Absolute.of(nodeIdentifiers)).toInference());
            try (var nodeWriter = NormalizedNodeWriter.forStreamWriter(xmlStream)) {
                nodeWriter.write(container);
            }
        } catch (IOException | XMLStreamException e) {
            throw new AssertionError(e);
        }
        return writer.toString();
    }
}
