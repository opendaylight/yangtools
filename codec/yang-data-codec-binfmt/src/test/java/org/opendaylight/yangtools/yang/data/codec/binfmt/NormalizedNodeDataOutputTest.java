/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class NormalizedNodeDataOutputTest {
    @Mock
    private NormalizedNodeDataOutput output;

    @Before
    public void before() throws IOException {
        doCallRealMethod().when(output).writeOptionalNormalizedNode(any());
    }

    @Test
    public void testWriteOptionalNormalizedNodeAbsent() throws IOException {
        doNothing().when(output).writeBoolean(false);
        output.writeOptionalNormalizedNode(null);
        verify(output).writeBoolean(false);
    }

    @Test
    public void testWriteOptionalNormalizedNodePresent() throws IOException {
        final ContainerNode node = ImmutableNodes.containerNode(QName.create("test", "test"));
        doNothing().when(output).writeBoolean(true);
        doNothing().when(output).writeNormalizedNode(node);
        output.writeOptionalNormalizedNode(node);
        verify(output).writeBoolean(true);
        verify(output).writeNormalizedNode(node);
    }
}
