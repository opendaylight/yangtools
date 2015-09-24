/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.jaxen;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import com.google.common.base.Optional;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class TestUtils {

    private static final QName ROOT_QNAME = QName.create("urn:opendaylight.test2", "2015-08-08", "root");
    private static final QName LEAF_C_QNAME = QName.create(ROOT_QNAME, "leaf-c");
    private static final QName LIST_A_QNAME = QName.create(ROOT_QNAME, "list-a");
    private static final QName LIST_B_QNAME = QName.create(ROOT_QNAME, "list-b");
    private static final QName LEAF_A_QNAME = QName.create(ROOT_QNAME, "leaf-a");
    private static final QName LEAF_B_QNAME = QName.create(ROOT_QNAME, "leaf-b");
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String ONE = "one";
    private static final String TWO = "two";

    private static final YangInstanceIdentifier LIST_A_FOO_PATH = YangInstanceIdentifier.builder()
            .node(LIST_A_QNAME).nodeWithKey(LIST_A_QNAME, LEAF_A_QNAME, FOO)
            .build();
    private static final YangInstanceIdentifier LIST_B_TWO_PATH = YangInstanceIdentifier.builder()
            .node(LIST_A_QNAME).nodeWithKey(LIST_A_QNAME, LEAF_A_QNAME, BAR)
            .node(LIST_B_QNAME).nodeWithKey(LIST_B_QNAME, LEAF_B_QNAME, TWO)
            .build();
    private static final YangInstanceIdentifier EMPTY = YangInstanceIdentifier.builder().build();

    private TestUtils() {
    }

    public static YangInstanceIdentifier getYangInstanceIdentifier(int inputCase) {
        switch (inputCase) {
            case 1: return LIST_A_FOO_PATH;
            default: return EMPTY;
        }
    }

    public static SchemaContext loadSchemaContext(final URI resourceDirectory) throws IOException {
        final YangParserImpl parser = new YangParserImpl();
        final File testDir = new File(resourceDirectory);
        final String[] fileList = testDir.list();
        final List<File> testFiles = new ArrayList<>();
        if (fileList == null) {
            throw new FileNotFoundException(resourceDirectory.toString());
        }
        for (String fileName : fileList) {
            testFiles.add(new File(testDir, fileName));
        }
        SchemaContext ctx = parser.parseFiles(testFiles);
        return ctx;
    }

    public static SchemaPath createPath(final boolean absolute, final URI namespace, final Date revision, final String prefix, final String... names) {
        List<QName> path = new ArrayList<>();
        for (String name : names) {
            path.add(QName.create(namespace, revision, name));
        }
        return SchemaPath.create(path, absolute);
    }

        /**
         * Returns a test document
         *
         * <pre>
         * root
         *     leaf-c "value c"
         *     list-a
         *          leaf-a "foo"
         *     list-a
         *          leaf-a "bar"
         *          list-b
         *                  leaf-b "one"
         *          list-b
         *                  leaf-b "two"
         *
         * </pre>
         *
         * @return
         */
    public static NormalizedNode<?, ?> createNormalizedNodes() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(ROOT_QNAME))
                .withChild(ImmutableNodes.leafNode(LEAF_C_QNAME, "value c"))
                .withChild(mapNodeBuilder(LIST_A_QNAME)
                        .withChild(mapEntry(LIST_A_QNAME, LEAF_A_QNAME, FOO))
                        .withChild(mapEntryBuilder(LIST_A_QNAME, LEAF_A_QNAME, BAR)
                                .withChild(mapNodeBuilder(LIST_B_QNAME)
                                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, ONE))
                                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, TWO))
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public static void findNodeTest() {
        NormalizedNode<?, ?> tree = createNormalizedNodes();
        assertNotNull(tree);

        Optional<NormalizedNode<?, ?>> listFooResult = NormalizedNodes.findNode(tree, LIST_A_FOO_PATH);
        assertTrue(listFooResult.isPresent());

        Optional<NormalizedNode<?, ?>> listTwoResult = NormalizedNodes.findNode(tree, LIST_B_TWO_PATH);
        assertTrue(listTwoResult.isPresent());
    }

}
