/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainer;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListIntBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListIntKey;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class Bug5845booleanKeyTest extends AbstractBindingCodecTest {
    @Test
    public void testBug5845() throws Exception {
        final BooleanListKey blk = new BooleanListKey(true, true);
        final BooleanContainer booleanContainer = new BooleanContainerBuilder()
                .setBooleanList(Collections.singletonMap(blk, new BooleanListBuilder()
                    .withKey(blk)
                    .setBooleanLeaf1(true)
                    .setBooleanLeaf2(true)
                    .build()))
                .build();

        final BooleanListIntKey blik = new BooleanListIntKey((byte) 1);
        final BooleanContainer booleanContainerInt = new BooleanContainerBuilder()
                .setBooleanListInt(Collections.singletonMap(blik, new BooleanListIntBuilder()
                        .withKey(blik)
                        .setBooleanLeafInt((byte) 1)
                        .build()))
                .build();

        final BindingCodecTree codecContext = registry.getCodecContext();
        final BindingDataObjectCodecTreeNode<BooleanContainer> subtreeCodec = codecContext.getSubtreeCodec(
                InstanceIdentifier.create(BooleanContainer.class));
        final NormalizedNode<?, ?> serializedInt = subtreeCodec.serialize(booleanContainerInt);
        assertNotNull(serializedInt);
        final NormalizedNode<?, ?> serialized = subtreeCodec.serialize(booleanContainer);
        assertNotNull(serialized);
    }
}
