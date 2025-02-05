/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainer;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListIntBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListIntKey;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListKey;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class Bug5845booleanKeyTest extends AbstractBindingCodecTest {
    @Test
    void testBug5845() {
        final var booleanContainer = new BooleanContainerBuilder()
            .setBooleanList(BindingMap.of(new BooleanListBuilder()
                .withKey(new BooleanListKey(true, true))
                .setBooleanLeaf1(true)
                .setBooleanLeaf2(true)
                .build()))
            .build();

        final var booleanContainerInt = new BooleanContainerBuilder()
            .setBooleanListInt(BindingMap.of(new BooleanListIntBuilder()
                .withKey(new BooleanListIntKey((byte) 1))
                .setBooleanLeafInt((byte) 1)
                .build()))
            .build();

        final var subtreeCodec = codecContext.getDataObjectCodec(InstanceIdentifier.create(BooleanContainer.class));
        final var serializedInt = subtreeCodec.serialize(booleanContainerInt);
        assertNotNull(serializedInt);
        final var serialized = subtreeCodec.serialize(booleanContainer);
        assertNotNull(serialized);
    }
}
