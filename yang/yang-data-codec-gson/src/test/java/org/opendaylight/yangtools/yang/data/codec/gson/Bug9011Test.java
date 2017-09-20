/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug9011Test {
    private static final String NS = "foo";
    private static final String REV = "2015-01-05";
    private static final QName SIMPLE_CONTAINER_QNAME = qN("simple-container");
    private static final QName COMPLEX_AUGMENT_CONTAINER_QNAME = qN("complex-augment-container");

    private static List<QName> schemaPathArguments(final @Nonnull YangInstanceIdentifier yangId,
            final int removeLastX) {
        final List<QName> all = yangId.getPathArguments().stream()
                // predicate/augmentations ones must be filtered out
                .filter(pathArgument -> !(pathArgument instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates
                        || pathArgument instanceof YangInstanceIdentifier.AugmentationIdentifier))
                .map(YangInstanceIdentifier.PathArgument::getNodeType).collect(Collectors.toList());
        if (removeLastX == 0) {
            return all;
        }
        return all.stream().limit(all.size() - removeLastX).collect(Collectors.toList());
    }

    @Test
    public void testWriteAugmentationWithLeaf() throws IOException, ReactorException, URISyntaxException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSources("/bug9011/");
        final QName leafQname = QName.create(SIMPLE_CONTAINER_QNAME, "leaf-under-aug");
        final ImmutableSet<QName> childNodes = ImmutableSet.of(COMPLEX_AUGMENT_CONTAINER_QNAME, leafQname);
        final YangInstanceIdentifier.AugmentationIdentifier augNodeIdentifier = new YangInstanceIdentifier.AugmentationIdentifier(
                childNodes);
        final YangInstanceIdentifier id = YangInstanceIdentifier.builder().node(SIMPLE_CONTAINER_QNAME)
                .node(augNodeIdentifier).build();

        final AugmentationNode augData = Builders.augmentationBuilder().withNodeIdentifier(augNodeIdentifier).withChild(
                Builders.leafBuilder().withNodeIdentifier(NodeIdentifier.create(leafQname)).withValue("val").build())
                .build();
        final String result = writeJsonNode(SchemaPath.create(schemaPathArguments(id, 0), true), augData, schemaContext,
                new StringWriter());
        assertEquals("{\"foo:leaf-under-aug\":\"val\"}", result);
    }

    @Test
    public void testWriteAugmentationWithContainer() throws IOException, ReactorException, URISyntaxException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSources("/bug9011/");
        final QName leafQname = QName.create(SIMPLE_CONTAINER_QNAME, "leaf-under-aug");
        final QName underAugContainerQname = COMPLEX_AUGMENT_CONTAINER_QNAME;
        final ImmutableSet<QName> childNodes = ImmutableSet.of(underAugContainerQname, leafQname);
        final YangInstanceIdentifier.AugmentationIdentifier augNodeIdentifier = new YangInstanceIdentifier.AugmentationIdentifier(
                childNodes);
        final YangInstanceIdentifier id = YangInstanceIdentifier.builder().node(SIMPLE_CONTAINER_QNAME)
                .node(augNodeIdentifier).build();

        final AugmentationNode augData = Builders.augmentationBuilder().withNodeIdentifier(augNodeIdentifier).withChild(
                Builders.containerBuilder().withNodeIdentifier(NodeIdentifier.create(underAugContainerQname)).build())
                .build();

        final String result = writeJsonNode(SchemaPath.create(schemaPathArguments(id, 0), true), augData, schemaContext,
                new StringWriter());
        assertEquals("{\"foo:complex-augment-container\":{}}", result);
    }

    @Test
    public void testWriteContainerWithAugData() throws IOException, ReactorException, URISyntaxException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSources("/bug9011/");
        final QName leafQname = QName.create(SIMPLE_CONTAINER_QNAME, "leaf-under-aug");
        final QName underAugContainerQname = COMPLEX_AUGMENT_CONTAINER_QNAME;
        final ImmutableSet<QName> childNodes = ImmutableSet.of(underAugContainerQname, leafQname);

        final YangInstanceIdentifier.AugmentationIdentifier augNodeIdentifier = new YangInstanceIdentifier.AugmentationIdentifier(
                childNodes);
        final YangInstanceIdentifier id = YangInstanceIdentifier.EMPTY;
        final AugmentationNode augData = Builders.augmentationBuilder().withNodeIdentifier(augNodeIdentifier).withChild(
                Builders.containerBuilder().withNodeIdentifier(NodeIdentifier.create(underAugContainerQname)).build())
                .build();
        final NormalizedNode<?, ?> data = Builders.containerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(SIMPLE_CONTAINER_QNAME)).withChild(augData).build();

        final String result = writeJsonNode(SchemaPath.create(schemaPathArguments(id, 0), true), data, schemaContext,
                new StringWriter());
        assertEquals("{\"foo:simple-container\":{\"complex-augment-container\":{}}}", result);
    }

    private static QName qN(final String localName) {
        return QName.create(NS, REV, localName);
    }

    private static String writeJsonNode(final SchemaPath path, final NormalizedNode<?, ?> inputStructure,
            final SchemaContext schemaContext, final Writer writer) throws IOException {
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
                JSONCodecFactory.getShared(schemaContext), path, null, JsonWriterFactory.createJsonWriter(writer, 0));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(inputStructure);

        nodeWriter.close();
        return writer.toString();
    }
}
