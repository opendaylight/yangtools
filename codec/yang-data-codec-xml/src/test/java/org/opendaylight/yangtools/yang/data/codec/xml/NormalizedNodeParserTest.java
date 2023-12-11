/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Qualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.util.codec.NormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.util.codec.NormalizedNodeParserException;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

// Note: this class has a mirror in yang-data-codec-gson. Every modification should be reflected there as well.
class NormalizedNodeParserTest {
    private static final EffectiveModelContext MODEL_CONTEXT = YangParserTestUtils.parseYang("""
            module foo {
              yang-version 1.1;
              prefix foo;
              namespace foo;

              container foo {
                leaf str {
                  type string {
                    length 3;
                  }
                }
              }

              container bar {
                leaf uint {
                  type uint32;
                }
              }

              list baz {
                key "one two";
                leaf one {
                  type boolean;
                }
                leaf two {
                  type string;
                }

                action qux {
                  input {
                    leaf str {
                      type string;
                    }
                  }
                }
              }

              rpc thud {
                input {
                  leaf uint {
                    type uint32;
                  }
                }
              }

              choice ch1 {
                choice ch2 {
                  leaf str {
                    type string;
                  }
                }
              }
            }""");
    private static final NormalizedNodeParser PARSER = XmlCodecFactory.create(MODEL_CONTEXT);
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName BAZ = QName.create("foo", "baz");
    private static final QName QUX = QName.create("foo", "qux");
    private static final QName THUD = QName.create("foo", "thud");
    private static final QName ONE = QName.create("foo", "one");
    private static final QName TWO = QName.create("foo", "two");
    private static final QName STR = QName.create("foo", "str");
    private static final QName UINT = QName.create("foo", "uint");

    private static final QNameModule RESTCONF_NS = QNameModule.create(
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-restconf"), Revision.of("2017-01-26"));
    private static final Qualified RESTCONF_DATA = Qualified.of("ietf-restconf", "data");
    private static final QName RESTCONF = RESTCONF_DATA.bindTo(RESTCONF_NS);

    @Test
    void parseDatastore() throws Exception {
        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(RESTCONF))
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(FOO))
                .withChild(ImmutableNodes.leafNode(STR, "str"))
                .build())
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAR))
                .withChild(ImmutableNodes.leafNode(UINT, Uint32.TWO))
                .build())
            .build(),
            PARSER.parseDatastore(RESTCONF_NS, RESTCONF_DATA, stream("""
                {
                  "ietf-restconf:data" : {
                    "foo:foo" : {
                      "str" : "str"
                    },
                    "foo:bar" : {
                      "uint" : 2
                    }
                  }
                }""")));
    }

    @Test
    void parseData() throws Exception {
        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafNode(STR, "str"))
            .build(),
            PARSER.parseData(Inference.ofDataTreePath(MODEL_CONTEXT, FOO), stream("""
                <foo xmlns="foo">
                  <str>str</str>
                </foo>""")));
    }

    @Test
    void parseDataBadType() throws Exception {
        final var error = assertError(() -> PARSER.parseData(Inference.ofDataTreePath(MODEL_CONTEXT, FOO), stream("""
            <foo xmlns="foo">
              <str>too long</str>
            </foo>""")));
        assertEquals(ErrorType.APPLICATION, error.type());
        assertEquals(ErrorTag.INVALID_VALUE, error.tag());
    }

    @Test
    void parseDataBadRootElement() throws Exception {
        assertMismatchedError("(foo)foo", "(foo)bar",
            () -> PARSER.parseData(Inference.ofDataTreePath(MODEL_CONTEXT, FOO), stream("""
                <bar xmlns="foo">
                  <uint>23</uint>
                </bar>""")));
    }

    @Test
    void parseDataBadInference() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(THUD);

        final var ex = assertThrows(IllegalArgumentException.class,
            () -> PARSER.parseData(stack.toInference(), stream("")));
        assertEquals("Invalid inference statement RpcEffectiveStatementImpl{argument=(foo)thud}", ex.getMessage());
    }

    @Test
    void parseDataEmptyInference() throws Exception {
        final var inference = Inference.of(MODEL_CONTEXT);

        final var ex = assertThrows(IllegalArgumentException.class, () -> PARSER.parseData(inference, stream("")));
        assertEquals("Inference must not be empty", ex.getMessage());
    }

    @Test
    void parseChildData() throws Exception {
        final var prefixAndNode = PARSER.parseChildData(Inference.of(MODEL_CONTEXT), stream("""
            <foo xmlns="foo">
              <str>str</str>
            </foo>"""));

        assertEquals(List.of(), prefixAndNode.prefix());
        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafNode(STR, "str"))
            .build(), prefixAndNode.data());
    }

    @Test
    void parseChildDataChoices() throws Exception {
        final var prefixAndNode = PARSER.parseChildData(Inference.of(MODEL_CONTEXT), stream("""
            <str xmlns="foo">str</str>"""));
        assertEquals(List.of(
            new NodeIdentifier(QName.create("foo", "ch1")),
            new NodeIdentifier(QName.create("foo", "ch2"))), prefixAndNode.prefix());
        assertEquals(ImmutableNodes.leafNode(STR, "str"), prefixAndNode.data());
    }

    @Test
    void parseChildDataListEntry() throws Exception {
        final var prefixAndNode = PARSER.parseChildData(Inference.of(MODEL_CONTEXT), stream("""
            <baz xmlns="foo">
              <one>true</one>
              <two>two</two>
            </baz>"""));
        assertEquals(List.of(new NodeIdentifier(BAZ)), prefixAndNode.prefix());
        assertEquals(Builders.mapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(BAZ, Map.of(ONE, Boolean.TRUE, TWO, "two")))
            .withChild(ImmutableNodes.leafNode(ONE, Boolean.TRUE))
            .withChild(ImmutableNodes.leafNode(TWO, "two"))
            .build(), prefixAndNode.data());
    }

    @Test
    void parseChildDataListEntryOnly() throws Exception {
        // FIXME: this needs to be rejected, as it is an illegal format for a list resource, as per:
        //
        //        https://www.rfc-editor.org/rfc/rfc8040#section-4.4.1:
        //
        //        The message-body is expected to contain the
        //        content of a child resource to create within the parent (target
        //        resource).  The message-body MUST contain exactly one instance of the
        //        expected data resource.  The data model for the child tree is the
        //        subtree, as defined by YANG for the child resource.
        //
        //        https://www.rfc-editor.org/rfc/rfc7951#section-5.4:
        //
        //        the following is a valid JSON-encoded instance:
        //
        //            "bar": [
        //              {
        //                "foo": 123,
        //                "baz": "zig"
        //              },
        //              {
        //                "baz": "zag",
        //                "foo": 0
        //              }
        //            ]
        final var prefixAndNode = PARSER.parseChildData(Inference.of(MODEL_CONTEXT), stream("""
            {
              "foo:baz" : {
                "one" : true,
                "two" : "two"
              }
            }"""));
        assertEquals(List.of(new NodeIdentifier(BAZ)), prefixAndNode.prefix());
        assertEquals(Builders.mapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(BAZ, Map.of(ONE, Boolean.TRUE, TWO, "two")))
            .withChild(ImmutableNodes.leafNode(ONE, Boolean.TRUE))
            .withChild(ImmutableNodes.leafNode(TWO, "two"))
            .build(), prefixAndNode.data());
    }

    @Test
    void parseChildDataListEntryNone() throws Exception {
        final var error = assertError(() -> PARSER.parseChildData(Inference.of(MODEL_CONTEXT), stream("""
            <baz xmlns="foo"/>""")));
        assertEquals(ErrorType.PROTOCOL, error.type());
        assertEquals(ErrorTag.MALFORMED_MESSAGE, error.tag());
        assertEquals("Exactly one instance of (foo)baz is required, 0 supplied", error.message());
    }

    @Test
    void parseChildDataListEntryTwo() throws Exception {
        final var error = assertError(() -> PARSER.parseChildData(Inference.of(MODEL_CONTEXT), stream("""
            {
              "foo:baz" : [
                {
                  "one" : false,
                  "two" : "two"
                },
                {
                  "one" : true,
                  "two" : "two"
                }
              ]
            }""")));
        assertEquals(ErrorType.PROTOCOL, error.type());
        assertEquals(ErrorTag.MALFORMED_MESSAGE, error.tag());
        assertEquals("Exactly one instance of (foo)baz is required, 2 supplied", error.message());
    }

    @Test
    void parseInputRpc() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(THUD);

        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create("foo", "input")))
            .withChild(ImmutableNodes.leafNode(UINT, Uint32.TWO))
            .build(),
            PARSER.parseInput(stack.toInference(), stream("""
                <input xmlns="foo">
                  <uint>2</uint>
                </input>""")));
    }

    @Test
    void parseInputRpcBadRootElement() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(THUD);

        assertMismatchedError("(foo)input", "(foo)output", () -> PARSER.parseInput(stack.toInference(), stream("""
            <output xmlns="foo"/>""")));
    }

    @Test
    void parseInputAction() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(BAZ);
        stack.enterSchemaTree(QUX);

        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create("foo", "input")))
            .withChild(ImmutableNodes.leafNode(STR, "str"))
            .build(),
            PARSER.parseInput(stack.toInference(), stream("""
                <input xmlns="foo">
                  <str>str</str>
                </input>""")));
    }

    @Test
    void parseInputBadInference() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(BAZ);

        final var ex = assertThrows(IllegalArgumentException.class,
            () -> PARSER.parseInput(stack.toInference(), stream("")));
        assertEquals("Invalid inference statement EmptyListEffectiveStatement{argument=(foo)baz}", ex.getMessage());
    }

    @Test
    void parseOutputRpc() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(THUD);

        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create("foo", "output")))
            .build(),
            PARSER.parseOutput(stack.toInference(), stream("""
                <output xmlns="foo"/>""")));
    }

    @Test
    void parseOutputRpcBadRootElement() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(THUD);

        assertMismatchedError("(foo)output", "(foo)input", () -> PARSER.parseOutput(stack.toInference(), stream("""
            <input xmlns="foo"/>""")));
    }

    @Test
    void parseOutputAction() throws Exception {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(BAZ);
        stack.enterSchemaTree(QUX);

        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create("foo", "output")))
            .build(),
            PARSER.parseOutput(stack.toInference(), stream("""
                <output xmlns="foo"/>""")));
    }

    @Test
    void parseOutputBadInference() {
        final var stack = SchemaInferenceStack.of(MODEL_CONTEXT);
        stack.enterSchemaTree(BAZ);

        final var ex = assertThrows(IllegalArgumentException.class,
            () -> PARSER.parseOutput(stack.toInference(), stream("")));
        assertEquals("Invalid inference statement EmptyListEffectiveStatement{argument=(foo)baz}", ex.getMessage());
    }

    private static @NonNull InputStream stream(final String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }

    private static void assertMismatchedError(final String expected, final String actual, final Executable executable) {
        final var error = assertError(executable);
        assertEquals(ErrorType.PROTOCOL, error.type());
        assertEquals(ErrorTag.MALFORMED_MESSAGE, error.tag());
        assertEquals("Payload name " + actual + " is different from identifier name " + expected, error.message());
    }

    private static YangNetconfError assertError(final Executable executable) {
        final var ex = assertThrows(NormalizedNodeParserException.class, executable);
        final var errors = ex.getNetconfErrors();
        assertEquals(1, errors.size());
        final var error = errors.get(0);
        assertEquals(ErrorSeverity.ERROR, error.severity());
        return error;
    }
}
