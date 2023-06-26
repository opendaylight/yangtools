/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataWithContainer;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataWithContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.yang.data.with.container.ContainerFromYangDataBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;

@ExtendWith(MockitoExtension.class)
class YT1605Test extends AbstractBindingCodecTest {
    @Mock
    private NormalizedYangData mockYangData;

    @Test
    void testFromBinding() {
        final var binding = new YangDataWithContainerBuilder()
            .setContainerFromYangData(new ContainerFromYangDataBuilder().setStr("str").build())
            .build();

        final var dom = codecContext.getYangDataCodec(binding.implementedInterface()).fromBinding(binding);
        assertNotNull(dom);
    }

    @Test
    void testToBinding() {
        final var binding = codecContext.getYangDataCodec(YangDataWithContainer.NAME).toBinding(mockYangData);
        assertNotNull(binding);
    }
}
