/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;

/*
* Schema structure of document is:
*
* container root { 
*      list list-a {
*              key leaf-a;
*              leaf leaf-a;
*              choice choice-a {
*                      case one {
*                              leaf one;
*                      }
*                      case two-three {
*                              leaf two;
*                              leaf three;
*                      }
*              }
*              list list-b {
*                      key leaf-b;
*                      leaf leaf-b;
*              }
*      }
* }
*/
class NormalizedNodeUtilsTest {
    private static final QName ROOT_QNAME = QName.create("urn:opendaylight:controller:sal:dom:store:test", "2014-03-13",
            "root");
    private static final QName LIST_A_QNAME = QName.create(ROOT_QNAME, "list-a");
    private static final QName LIST_B_QNAME = QName.create(ROOT_QNAME, "list-b");
    private static final QName LEAF_A_QNAME = QName.create(ROOT_QNAME, "leaf-a");
    private static final QName LEAF_B_QNAME = QName.create(ROOT_QNAME, "leaf-b");
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String ONE = "one";
    private static final String TWO = "two";

    private static final YangInstanceIdentifier LIST_A_FOO_PATH = YangInstanceIdentifier.builder()
        .node(LIST_A_QNAME)
        .nodeWithKey(LIST_A_QNAME, LEAF_A_QNAME, FOO)
        .build();
    private static final YangInstanceIdentifier LIST_B_TWO_PATH = YangInstanceIdentifier.builder()
        .node(LIST_A_QNAME)
        .nodeWithKey(LIST_A_QNAME, LEAF_A_QNAME, BAR)
        .node(LIST_B_QNAME)
        .nodeWithKey(LIST_B_QNAME,LEAF_B_QNAME,TWO)
        .build();

    /**
     * Returns a test document.
     *
     * <pre>
     * root
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
     * @return A test document
     */
    private static NormalizedNode createDocumentOne() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
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

    @Test
    void findNodeTest() {
        final var tree = createDocumentOne();
        assertNotNull(tree);

        final var listFooResult = NormalizedNodes.findNode(tree, LIST_A_FOO_PATH);
        assertTrue(listFooResult.isPresent());

        final var listTwoResult = NormalizedNodes.findNode(tree, LIST_B_TWO_PATH);
        assertTrue(listTwoResult.isPresent());
    }
}
