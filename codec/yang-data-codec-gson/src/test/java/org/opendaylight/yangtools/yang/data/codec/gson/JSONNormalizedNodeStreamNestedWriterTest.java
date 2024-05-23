/*
 * Copyright (c) 2024 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonWriter;
import java.io.StringWriter;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class JSONNormalizedNodeStreamNestedWriterTest {

    static EffectiveModelContext schemaContext;
    static JSONCodecFactory lhotkaCodecFactory;

    @BeforeAll
    static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/yt1575");
        lhotkaCodecFactory = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext);
    }

    @AfterAll
    static void afterClass() {
        lhotkaCodecFactory = null;
        schemaContext = null;
    }

    @Test
    void immutableMapNodeStreamWriterDemonstration() throws Exception {
        var configId = new YangInstanceIdentifier.NodeIdentifier(QName.create(
            "http://example.com/basic-model", "config"));
        var interfacesQName = QName.create("http://example.com/basic-model", "interfaces");
        var interfaceQName = QName.create("http://example.com/basic-model", "interface");
        var nameQName = QName.create("http://example.com/basic-model", "name");

        var mtu = ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "mtu"), 1500);
        var name = ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "name"), "admin");
        var loopbackMode = ImmutableNodes.leafNode(QName.create(
            "http://example.com/basic-model", "loopback-mode"), false);
        var enabled = ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "enabled"), false);
        var type = ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "type"), "IF_ETHERNET");

        var interfaceIdWithPredicates = YangInstanceIdentifier.NodeIdentifierWithPredicates.of(
            interfaceQName, nameQName, "eth3");

        var path = YangInstanceIdentifier.builder()
            .node(interfacesQName)
            .node(interfaceQName)
            .nodeWithKey(interfaceQName, interfaceIdWithPredicates.asMap())
            .build();

        var configNodeBuilder = ImmutableNodes.newContainerBuilder().withNodeIdentifier(configId);
        configNodeBuilder.withChild(mtu).withChild(name).withChild(loopbackMode).withChild(enabled).withChild(type);

        var interfaceNode = ImmutableNodes.newMapEntryBuilder().withNodeIdentifier(interfaceIdWithPredicates)
            .withChild(configNodeBuilder.build())
            .withChild(ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "name"), "eth3"))
            .build();

        EffectiveModelContext context = YangParserTestUtils.parseYangResourceDirectory("/yt1575");

        var inference = DataSchemaContextTree.from(context)
            .enterPath(Objects.requireNonNull(path.getParent()))
            .orElseThrow()
            .stack()
            .toInference();

        var writer = new StringWriter();
        var jsonWriter = new JsonWriter(writer);
        var namespace = interfaceNode.name().getNodeType().getNamespace();
        var nodeWriter = JSONNormalizedNodeStreamWriter
            .createNestedWriter(lhotkaCodecFactory, inference, namespace, jsonWriter);
        var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(nodeWriter);
        normalizedNodeWriter.write(interfaceNode);
    }
}
