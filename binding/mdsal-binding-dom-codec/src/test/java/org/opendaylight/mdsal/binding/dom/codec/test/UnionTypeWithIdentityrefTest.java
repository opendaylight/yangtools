/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map.Entry;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.IdentOne;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.UnionNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.UnionNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.UnionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.bug._6006.rev160607.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class UnionTypeWithIdentityrefTest extends AbstractBindingCodecTest {
    private static final String IDENT_ONE_STRING = "IdentOne";
    public static final QName NODE_QNAME = QName.create("urn:opendaylight:params:xml:ns:yang:mdsal:test:bug:6066",
        "2016-06-07", "union-node");
    public static final QName NODE_LEAF_QNAME = QName.create(NODE_QNAME, "value");

    private DataObject createValueNode(final String valueString) {
        UnionType unionType = UnionTypeBuilder.getDefaultInstance(valueString);
        UnionNode unionNode = new UnionNodeBuilder().setValue(unionType).build();
        NormalizedNode<?, ?> normalizedUnionNode = registry
            .toNormalizedNode(InstanceIdentifier.builder(UnionNode.class).build(), unionNode)
            .getValue();

        Entry<InstanceIdentifier<?>, DataObject> unionNodeEntry = registry.fromNormalizedNode(
                YangInstanceIdentifier.of(normalizedUnionNode.getNodeType()), normalizedUnionNode);
        DataObject unionNodeObj = unionNodeEntry.getValue();
        assertTrue(unionNodeObj instanceof UnionNode);
        return unionNodeObj;
    }

    @Test
    public void bug6006Test() {
        DataObject unionNodeObj = createValueNode(IDENT_ONE_STRING);
        UnionType unionTypeObj = ((UnionNode) unionNodeObj).getValue();
        assertEquals(null, unionTypeObj.getUint8());
        assertEquals(IdentOne.class, unionTypeObj.getIdentityref());
    }

    @Test
    public void bug6112Test() {
        DataObject unionNodeObj = createValueNode("1");
        UnionType unionTypeObj = ((UnionNode) unionNodeObj).getValue();
        assertEquals(Short.valueOf((short)1), unionTypeObj.getUint8());
        assertEquals(null, unionTypeObj.getIdentityref());
    }
}
