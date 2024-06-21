/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataWithContainer;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataWithContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.yang.data.with.container.ContainerFromYangDataBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class Mdsal805Test extends AbstractBindingCodecTest {
    @Ignore
    @Test
    public void testFromBinding() {
        final var binding = new YangDataWithContainerBuilder()
            .setContainerFromYangData(new ContainerFromYangDataBuilder().setStr("str").build())
            .build();

        final var dom = codecContext.getYangDataCodec(binding.implementedInterface()).fromBinding(binding);
        assertNotNull(dom);
    }

    @Ignore
    @Test
    public void testToBinding() {
        final var dom = mock(NormalizedYangData.class);

        final var binding = codecContext.getYangDataCodec(YangDataWithContainer.NAME).toBinding(dom);
        assertNotNull(binding);
    }
}
