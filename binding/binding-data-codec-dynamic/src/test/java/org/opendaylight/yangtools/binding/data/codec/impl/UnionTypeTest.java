/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

public class UnionTypeTest extends AbstractBindingCodecTest {
    // FIXME: MDSAL-741: re-enable this test
    //    private static final String TEST_STRING = "testtesttest";
    //    private static final QName WRAPPER_QNAME = QName.create(
    //        "urn:opendaylight:params:xml:ns:yang:yangtools:test:union", "2015-01-21", "wrapper");
    //    private static final QName WRAP_LEAF_QNAME = QName.create(WRAPPER_QNAME, "wrap");

    //    @Test
    //    public void unionTest() {
    //        TopLevel topLevel = TopLevelBuilder.getDefaultInstance(TEST_STRING);
    //        Wrapper wrapper = new WrapperBuilder().setWrap(topLevel).build();
    //        NormalizedNode topLevelEntry = codecContext.toNormalizedNode(InstanceIdentifier.create(Wrapper.class),
    //            wrapper).getValue();
    //
    //        ContainerNode containerNode = ImmutableContainerNodeBuilder.create()
    //                .withNodeIdentifier(new NodeIdentifier(WRAPPER_QNAME))
    //                .withChild(ImmutableNodes.leafNode(WRAP_LEAF_QNAME, TEST_STRING))
    //                .build();
    //        assertEquals(topLevelEntry, containerNode);
    //    }
    //
    //    @Test
    //    public void bug5446Test() {
    //        IpAddressBinary ipAddress = IpAddressBinaryBuilder.getDefaultInstance("fwAAAQ==");
    //        Root root = new RootBuilder().setIpAddress(ipAddress).build();
    //        NormalizedNode rootNode = codecContext.toNormalizedNode(InstanceIdentifier.create(Root.class), root)
    //                .getValue();
    //
    //        Entry<InstanceIdentifier<?>, DataObject> rootEntry = codecContext.fromNormalizedNode(
    //                YangInstanceIdentifier.of(rootNode.getIdentifier().getNodeType()), rootNode);
    //
    //        DataObject rootObj = rootEntry.getValue();
    //        assertTrue(rootObj instanceof Root);
    //        IpAddressBinary desIpAddress = ((Root) rootObj).getIpAddress();
    //        assertEquals(ipAddress, desIpAddress);
    //    }
}
