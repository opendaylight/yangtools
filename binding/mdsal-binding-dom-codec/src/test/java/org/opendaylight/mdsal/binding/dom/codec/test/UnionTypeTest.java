/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.bug5446.rev151105.IpAddressBinary;
import org.opendaylight.yang.gen.v1.bug5446.rev151105.IpAddressBinaryBuilder;
import org.opendaylight.yang.gen.v1.bug5446.rev151105.Root;
import org.opendaylight.yang.gen.v1.bug5446.rev151105.RootBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.TopLevel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.TopLevelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.Wrapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.WrapperBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

public class UnionTypeTest extends AbstractBindingCodecTest {

    private static final String TEST_STRING = "testtesttest";

    public static final QName WRAPPER_QNAME = QName.create("urn:opendaylight:params:xml:ns:yang:yangtools:test:union",
        "2015-01-21", "wrapper");
    public static final QName WRAP_LEAF_QNAME = QName.create(WRAPPER_QNAME, "wrap");

    @Test
    public void unionTest() {
        TopLevel topLevel = TopLevelBuilder.getDefaultInstance(TEST_STRING);
        Wrapper wrapper = new WrapperBuilder().setWrap(topLevel).build();
        NormalizedNode<?, ?> topLevelEntry = registry.toNormalizedNode(InstanceIdentifier.create(Wrapper.class),
            wrapper).getValue();

        ContainerNode containerNode = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(WRAPPER_QNAME))
                .withChild(ImmutableNodes.leafNode(WRAP_LEAF_QNAME, TEST_STRING))
                .build();
        Assert.assertEquals(topLevelEntry, containerNode);
    }

    @Test
    public void bug5446Test() {
        IpAddressBinary ipAddress = IpAddressBinaryBuilder.getDefaultInstance("fwAAAQ==");
        Root root = new RootBuilder().setIpAddress(ipAddress).build();
        NormalizedNode<?, ?> rootNode = registry.toNormalizedNode(InstanceIdentifier.builder(Root.class).build(), root)
                .getValue();

        Entry<InstanceIdentifier<?>, DataObject> rootEntry = registry.fromNormalizedNode(
                YangInstanceIdentifier.of(rootNode.getNodeType()), rootNode);

        DataObject rootObj = rootEntry.getValue();
        assertTrue(rootObj instanceof Root);
        IpAddressBinary desIpAddress = ((Root) rootObj).getIpAddress();
        assertEquals(ipAddress, desIpAddress);
    }
}
