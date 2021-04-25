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
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.runners.Parameterized.Parameter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public abstract class AbstractSerializationTest {

    @Parameter(0)
    public NormalizedNodeStreamVersion version;

    final <T extends NormalizedNode> T assertEquals(final T node, final int expectedSize) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(baos))) {
            nnout.writeNormalizedNode(node);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize", e);
        }

        final byte[] bytes = baos.toByteArray();
        Assert.assertEquals(expectedSize, bytes.length);

        final NormalizedNode deser;
        try {
            deser = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes)).readNormalizedNode();
        } catch (IOException e) {
            throw new AssertionError("Failed to deserialize", e);
        }
        Assert.assertEquals(node, deser);
        return node;
    }

    final <T> T assertEquals(final T value, final int expectedSize) {
        return assertEquals(ImmutableNodes.leafNode(TestModel.TEST_QNAME, value), expectedSize).body();
    }

    final <T extends PathArgument> T assertEquals(final T arg, final int expectedSize) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(baos))) {
            nnout.writePathArgument(arg);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize", e);
        }

        final byte[] bytes = baos.toByteArray();
        Assert.assertEquals(expectedSize, bytes.length);

        final PathArgument deser;
        try {
            deser = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes)).readPathArgument();
        } catch (IOException e) {
            throw new AssertionError("Failed to deserialize", e);
        }
        Assert.assertEquals(arg, deser);
        return (T) deser;
    }

    final void assertEqualsTwice(final PathArgument arg, final int expectedSize) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(baos))) {
            nnout.writePathArgument(arg);
            nnout.writePathArgument(arg);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize", e);
        }

        final byte[] bytes = baos.toByteArray();
        Assert.assertEquals(expectedSize, bytes.length);

        try {
            final NormalizedNodeDataInput in = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
            Assert.assertEquals(arg, in.readPathArgument());
            Assert.assertEquals(arg, in.readPathArgument());
        } catch (IOException e) {
            throw new AssertionError("Failed to deserialize", e);
        }
    }

    final void assertSame(final Object value, final int expectedSize) {
        Assert.assertSame(value, assertEquals(value, expectedSize));
    }

    final void assertSame(final PathArgument arg, final int expectedSize) {
        Assert.assertSame(arg, assertEquals(arg, expectedSize));
    }

    static final List<QName> generateQNames(final int size) {
        final List<QName> qnames = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            qnames.add(QName.create(TestModel.TEST_QNAME, "a" + Integer.toHexString(i)));
        }
        return qnames;
    }
}
