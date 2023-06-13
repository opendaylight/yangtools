/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;

import java.io.StringReader;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Tests addressing notification support by xml parser.
 */
class NotificationTest {
    private static final XMLNamespace NS = XMLNamespace.of("test:notification");

    private static EffectiveModelContext schemaContext;

    @BeforeAll
    public static void beforeAll() {
        schemaContext = YangParserTestUtils.parseYangResource("/notifications.yang");
    }

    @AfterAll
    static void afterAll() {
        schemaContext = null;
    }

    @ParameterizedTest(name = "Parse notification: {0}")
    @MethodSource("testArgs")
    void parseNotification(final String testDesc, final String xml, final NormalizedNode expected) throws Exception {

        final var reader = UntrustedXML.createXMLStreamReader(new StringReader(xml));
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            DefaultSchemaTreeInference.of(schemaContext, Absolute.of(expected.name().getNodeType())));
        xmlParser.parse(reader);

        assertEquals(expected, result.getResult().data());
    }

    private static Stream<Arguments> testArgs() {
        return Stream.of(
            Arguments.of(
                "root level", """
                    <root-notification xmlns="test:notification">
                        <root-message>test 0</root-message>
                    </root-notification>
                    """,
                containerNode(QName.create(NS, "root-notification"),
                    leafNode(QName.create(NS, "root-message"), "test 0"))
            ),
            Arguments.of(
                "inline", """
                    <container-one xmlns="test:notification">
                        <inline-notification>
                            <inline-message>test 1</inline-message>
                        </inline-notification>
                    </container-one>
                    """,
                containerNode(QName.create(NS, "container-one"),
                    containerNode(QName.create(NS, "inline-notification"),
                        leafNode(QName.create(NS, "inline-message"), "test 1")))
            ),
            Arguments.of(
                "from grouping", """
                    <container-two xmlns="test:notification">
                        <group-notification>
                            <group-message>test 2</group-message>
                        </group-notification>
                    </container-two>
                    """,
                containerNode(QName.create(NS, "container-two"),
                    containerNode(QName.create(NS, "group-notification"),
                        leafNode(QName.create(NS, "inline-message"), "test 2")))
            )
        );
    }

    private static ContainerNode containerNode(final QName qname, final DataContainerChild child) {
        return containerBuilder().withNodeIdentifier(new NodeIdentifier(qname)).addChild(child).build();
    }

    private static LeafNode<?> leafNode(final QName qname, final Object value) {
        return Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(qname)).withValue(value).build();
    }
}