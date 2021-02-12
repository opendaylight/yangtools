/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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

    /**
     * Returns a test node.
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
     * @return A test node
     */
    private static NormalizedNode createContainerNode() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
                .withChild(
                        mapNodeBuilder(LIST_A_QNAME)
                                .withChild(mapEntry(LIST_A_QNAME, LEAF_A_QNAME, FOO))
                                .withChild(
                                        mapEntryBuilder(LIST_A_QNAME, LEAF_A_QNAME, BAR).withChild(
                                                mapNodeBuilder(LIST_B_QNAME)
                                                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, ONE))
                                                        .withChild(mapEntry(LIST_B_QNAME, LEAF_B_QNAME, TWO)).build())
                                                .build()).build()).build();

    }

    @Test
    public void prettyTreeTest() {
        String expectedString = String.join("\n",
                "",
                "containerNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)root, body=[", 
                "    mapNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-a, body=[", 
                "        mapEntryNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-a[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-a=bar}], body=[", 
                "            leafNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-a, body=bar}",
                "            mapNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-b, body=[",
                "                mapEntryNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-b[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-b=two}], body=[",
                "                    leafNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-b, body=two}]}",
                "                mapEntryNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-b[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-b=one}], body=[",
                "                    leafNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-b, body=one}]}]}]}",
                "        mapEntryNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)list-a[{(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-a=foo}], body=[",
                "            leafNode{identifier=(urn:opendaylight:controller:sal:dom:store:test?revision=2014-03-13)leaf-a, body=foo}]}]}]}");

        assertEquals(expectedString, createContainerNode().prettyTree().get());
    }
}
