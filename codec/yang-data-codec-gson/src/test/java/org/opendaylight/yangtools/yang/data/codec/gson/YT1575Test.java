/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonWriter;
import java.io.StringWriter;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1575Test {

    @Test
    void immutableMapNodeStreamWriterDemonstration() throws Exception {
        final var schemaContext = YangParserTestUtils.parseYangResource("/yt1575.yang");
        final var lhotkaCodecFactory = JSONCodecFactorySupplier
            .DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext);

        final var interfacesQName = QName.create("http://example.com/yt1575", "testContainer");
        final var interfaceQName = QName.create("http://example.com/yt1575", "testList");
        final var nameQName = QName.create("http://example.com/yt1575", "name");

        final var interfaceIdWithPredicates = YangInstanceIdentifier.NodeIdentifierWithPredicates.of(
            interfaceQName, nameQName, "testName");

        final var path = YangInstanceIdentifier.builder()
            .node(interfacesQName)
            .node(interfaceQName)
            .nodeWithKey(interfaceQName, interfaceIdWithPredicates.asMap())
            .build();

        final var interfaceNode = ImmutableNodes.newMapEntryBuilder().withNodeIdentifier(interfaceIdWithPredicates)
            .withChild(ImmutableNodes.leafNode(QName.create("http://example.com/yt1575", "name"), "testName"))
            .build();

        final var inference = DataSchemaContextTree.from(schemaContext)
            .enterPath(Objects.requireNonNull(path.getParent()))
            .orElseThrow()
            .stack()
            .toInference();

        final var jsonWriter = new JsonWriter(new StringWriter());
        final var namespace = interfaceNode.name().getNodeType().getNamespace();
        final var nodeWriter = JSONNormalizedNodeStreamWriter
            .createNestedWriter(lhotkaCodecFactory, inference, namespace, jsonWriter);
        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(nodeWriter);
        normalizedNodeWriter.write(interfaceNode);
    }
}
