/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yang.gen.v1.mdsal668.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal668.norev.FooBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

@ExtendWith(MockitoExtension.class)
class YT1648Test extends AbstractBindingCodecTest {
    @Mock
    private NormalizedNodeStreamWriter writer;

    @Test
    void testStreamTo() throws Exception {
        final var codec = codecContext.getStreamDataObject(Foo.class);

        doNothing().when(writer).startContainerNode(new NodeIdentifier(Foo.QNAME), -1);
        doNothing().when(writer).nextDataSchemaNode(any(LeafSchemaNode.class));
        doNothing().when(writer).startLeafNode(new NodeIdentifier(Foo.QNAME));
        doNothing().when(writer).scalarValue(YangInstanceIdentifier.of(Foo.QNAME));
        doNothing().when(writer).endNode();

        codec.writeTo(writer, new FooBuilder()
            .setFoo(DataObjectIdentifier.builder(Foo.class).build())
            .build());

        verify(writer, times(2)).endNode();
    }
}
