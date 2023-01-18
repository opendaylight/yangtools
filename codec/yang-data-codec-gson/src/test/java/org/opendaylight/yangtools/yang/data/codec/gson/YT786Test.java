/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaTreeInference;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Tests addressing notification support by json parser.
 */
class YT786Test {
    private static final XMLNamespace NS = XMLNamespace.of("test:notification");

    private static EffectiveModelContext schemaContext;
    private static JSONCodecFactory jsonCodecFactory;

    @BeforeAll
    static void beforeAll() {
        schemaContext = YangParserTestUtils.parseYang("""
            module test-notification {
              yang-version 1.1;
              namespace "test:notification";
              prefix tn;
              notification root-notification {
                leaf root-message {
                  type string;
                }
              }
              container container-one {
                notification inline-notification {
                  leaf inline-message {
                    type string;
                  }
                }
              }
              container container-two {
                uses grp;
              }
              grouping grp {
                notification group-notification {
                  leaf group-message {
                    type string;
                  }
                }
              }
            }
            """);
        jsonCodecFactory = JSONCodecFactorySupplier.RFC7951.getShared(schemaContext);
    }

    @AfterAll
    static void afterAll() {
        schemaContext = null;
        jsonCodecFactory = null;
    }

    @ParameterizedTest(name = "Parsing notification: {0}")
    @MethodSource("testArgs")
    void parseNotification(final String testDesc, final Absolute path, final String json,
        final NormalizedNode expected) {
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, jsonCodecFactory, inferenceOf(path));
        jsonParser.parse(new JsonReader(new StringReader(json)));
        assertEquals(expected, result.getResult().data());
    }

    @ParameterizedTest(name = "Write notification: {0}")
    @MethodSource("testArgs")
    void writeNotification(final String testDesc, final Absolute path, final String expected,
        final NormalizedNode normalized) throws Exception {
        final var writer = new StringWriter();
        final var jsonStreamWriter = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            jsonCodecFactory, inferenceOf(path), NS, JsonWriterFactory.createJsonWriter(writer, 2));
        try (var nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStreamWriter)) {
            nodeWriter.write(normalized);
        }
        assertEquals(expected, writer.toString());
    }

    private static Stream<Arguments> testArgs() {
        return Stream.of(
            // test descriptor, path, notification data fragment as json, as normalized node
            Arguments.of(
                "root level",
                Absolute.of(QName.create(NS, "root-notification")),
                """
                    {
                      "root-message": "test"
                    }""",
                leafNode(QName.create(NS, "root-message"), "test")
            ),
            Arguments.of(
                "inline",
                Absolute.of(QName.create(NS, "container-one"),
                    QName.create(NS, "inline-notification")),
                """
                    {
                      "inline-message": "test 1"
                    }""",
                leafNode(QName.create(NS, "inline-message"), "test 1")
            ),
            Arguments.of(
                "from grouping",
                Absolute.of(QName.create(NS, "container-two"),
                    QName.create(NS, "group-notification")),
                """
                    {
                      "group-message": "test 2"
                    }""",
                leafNode(QName.create(NS, "group-message"), "test 2")
            )
        );
    }

    private static LeafNode<?> leafNode(final QName qname, final Object value) {
        return Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(qname)).withValue(value).build();
    }

    private static SchemaTreeInference inferenceOf(final Absolute path) {
        return DefaultSchemaTreeInference.of(schemaContext, path);
    }
}