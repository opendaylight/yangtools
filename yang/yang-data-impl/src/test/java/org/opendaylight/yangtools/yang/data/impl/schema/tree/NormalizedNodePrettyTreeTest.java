/*
 * Copyright (c) 2021 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

public class NormalizedNodePrettyTreeTest {

    private static final QName ROOT_QNAME = QName.create(
        "urn:opendaylight:controller:sal:dom:store:test", "2014-03-13", "root");
    private static final QName ANOTHER_NAMESPACE_QNAME = QName.create(
        "urn:opendaylight:controller:sal:dom:store:another", "another");
    private static final QName LIST_A_QNAME = QName.create(ROOT_QNAME, "list-a");
    private static final QName LIST_B_QNAME = QName.create(ROOT_QNAME, "list-b");
    private static final QName LEAF_A_QNAME = QName.create(ROOT_QNAME, "leaf-a");
    private static final QName LEAF_B_QNAME = QName.create(ROOT_QNAME, "leaf-b");
    private static final QName LIST_ANOTHER_NAMESPACE_QNAME = QName.create(ANOTHER_NAMESPACE_QNAME,
        "list-from-another-namespace");
    private static final QName LEAF_ANOTHER_NAMESPACE_QNAME = QName.create(ANOTHER_NAMESPACE_QNAME,
        "leaf-from-another-namespace");
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String ONE = "one";
    private static final String TWO = "two";

    /**
     * Return a test node.
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
     *     list-from-another-namespace
     *          leaf-from-another-namespace "I am leaf from another namespace"
     *          leaf-b "one"
     *          leaf-c "two"
     *
     * </pre>
     *
     * @return A test node
     */
    private static NormalizedNode createContainerNode() {
        return ImmutableContainerNodeBuilder.create()
            .withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
            .withChild(mapNodeBuilder(LIST_A_QNAME)
                .withChild(mapEntry(LIST_A_QNAME, LEAF_A_QNAME, FOO))
                .withChild(mapEntryBuilder(LIST_A_QNAME, LEAF_A_QNAME, BAR)
                    .withChild(mapNodeBuilder(LIST_B_QNAME)
                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, ONE))
                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, TWO))
                        .build()).build()).build())
            .withChild(mapNodeBuilder(LIST_ANOTHER_NAMESPACE_QNAME)
                .withChild(mapEntry(LIST_ANOTHER_NAMESPACE_QNAME,
                    LEAF_ANOTHER_NAMESPACE_QNAME, "I am leaf from another namespace"))
                .withChild(mapEntry(LIST_ANOTHER_NAMESPACE_QNAME, LEAF_B_QNAME, ONE))
                .withChild(mapEntry(LIST_ANOTHER_NAMESPACE_QNAME, LEAF_B_QNAME, TWO))
                .build())
            .build();
    }

    @Test
    @SuppressWarnings("checkstyle:LineLength")
    public void prettyTreeTest() {
        final String expectedString = String.join("\n",
                "",
                "containerNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, value=[",
                "    systemMapNode{identifier=list-a, value=[",
                "        mapEntryNode{identifier=list-a[{leaf-a=bar}], value=[",
                "            leafNode{identifier=leaf-a, value=bar}",
                "            systemMapNode{identifier=list-b, value=[",
                "                mapEntryNode{identifier=list-b[{leaf-b=two}], value=[",
                "                    leafNode{identifier=leaf-b, value=two}]}",
                "                mapEntryNode{identifier=list-b[{leaf-b=one}], value=[",
                "                    leafNode{identifier=leaf-b, value=one}]}]}]}",
                "        mapEntryNode{identifier=list-a[{leaf-a=foo}], value=[",
                "            leafNode{identifier=leaf-a, value=foo}]}]}",
                "    systemMapNode{identifier=(urn:opendaylight:controller:sal:dom:store:another)list-from-another-namespace, value=[",
                "        mapEntryNode{identifier=list-from-another-namespace[{leaf-from-another-namespace=I am leaf from another namespace}], value=[",
                "            leafNode{identifier=leaf-from-another-namespace, value=I am leaf from another namespace}]}",
                "        mapEntryNode{identifier=list-from-another-namespace[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-b=one}], value=[",
                "            leafNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-b, value=one}]}",
                "        mapEntryNode{identifier=list-from-another-namespace[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-b=two}], value=[",
                "            leafNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-b, value=two}]}]}]}");

        assertEquals(expectedString, createContainerNode().prettyTree().get());
    }
}
