/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.jaxen;

import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

final class TestUtils {

    private static final QName ROOT_QNAME = QName.create("urn:opendaylight.test2", "2015-08-08", "root");
    private static final QName LIST_A_QNAME = QName.create(ROOT_QNAME, "list-a");
    private static final QName LIST_B_QNAME = QName.create(ROOT_QNAME, "list-b");
    private static final QName LEAF_A_QNAME = QName.create(ROOT_QNAME, "leaf-a");
    private static final QName LEAF_B_QNAME = QName.create(ROOT_QNAME, "leaf-b");
    private static final QName LEAF_C_QNAME = QName.create(ROOT_QNAME, "leaf-c");
    private static final QName LEAF_D_QNAME = QName.create(ROOT_QNAME, "leaf-d");
    private static final QName CONTAINER_A_QNAME = QName.create(ROOT_QNAME, "container-a");
    private static final QName CONTAINER_B_QNAME = QName.create(ROOT_QNAME, "container-b");
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String WAZ = "waz";
    private static final String ONE = "one";
    private static final String TWO = "two";
    private static final String THREE = "three";

    private TestUtils() {
    }

    /**
     * Returns a test document
     *
     * <pre>
     * root
     *     leaf-c "waz"
     *     list-a
     *          leaf-a "foo"
     *     list-a
     *          leaf-a "bar"
     *          list-b
     *                  leaf-b "one"
     *          list-b
     *                  leaf-b "two"
     *     container-a
     *          container-b
     *                  leaf-d "three"
     * </pre>
     *
     * @return
     */
    public static NormalizedNode<?, ?> createNormalizedNodes() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(ROOT_QNAME))
                .withChild(ImmutableNodes.leafNode(LEAF_C_QNAME, WAZ))
                .withChild(mapNodeBuilder(LIST_A_QNAME)
                        .withChild(mapEntry(LIST_A_QNAME, LEAF_A_QNAME, FOO))
                        .withChild(mapEntryBuilder(LIST_A_QNAME, LEAF_A_QNAME, BAR)
                                .withChild(mapNodeBuilder(LIST_B_QNAME)
                                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, ONE))
                                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, TWO))
                                        .build())
                                .build())
                        .build())
                .withChild(ImmutableContainerNodeBuilder.create()
                        .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CONTAINER_A_QNAME))
                        .withChild(ImmutableContainerNodeBuilder.create().
                                withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier
                                        (CONTAINER_B_QNAME))
                                .withChild(ImmutableNodes.leafNode(LEAF_D_QNAME, THREE))
                                .build())
                        .build())
                .build();
    }
}