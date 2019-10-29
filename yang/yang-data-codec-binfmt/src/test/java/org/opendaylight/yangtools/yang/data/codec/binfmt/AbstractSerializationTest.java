/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.runners.Parameterized.Parameter;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public abstract class AbstractSerializationTest {

    @Parameter(0)
    public NormalizedNodeStreamVersion version;

    <T> T assertEquals(final T value, final int expectedSize) {
        final LeafNode<T> leafNode = ImmutableNodes.leafNode(TestModel.TEST_QNAME, value);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(baos))) {
            nnout.writeNormalizedNode(leafNode);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize", e);
        }

        final byte[] bytes = baos.toByteArray();
        Assert.assertEquals(expectedSize, bytes.length);

        final NormalizedNode<?, ?> deser;
        try {
            deser = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes))
                    .readNormalizedNode();
        } catch (IOException e) {
            throw new AssertionError("Failed to deserialize", e);
        }
        Assert.assertEquals(leafNode, deser);
        return value;
    }

    void assertSame(final Object value, final int expectedSize) {
        Assert.assertSame(value, assertEquals(value, expectedSize));
    }
}
