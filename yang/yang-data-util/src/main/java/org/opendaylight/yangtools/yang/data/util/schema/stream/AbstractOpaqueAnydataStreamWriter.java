/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.schema.stream;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueData;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.OpaqueAnydataExtension;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.AbstractOpaqueDataContainerBuilder;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.AbstractOpaqueDataNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataBuilder;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataContainerBuilder;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataListBuilder;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataValueBuilder;

@Beta
public abstract class AbstractOpaqueAnydataStreamWriter implements OpaqueAnydataExtension.StreamWriter {
    private final Deque<AbstractOpaqueDataNodeBuilder<?>> builders = new ArrayDeque<>();
    private final OpaqueDataBuilder builder;

    protected AbstractOpaqueAnydataStreamWriter(final boolean accurateLists) {
        this.builder = new OpaqueDataBuilder().withAccurateLists(accurateLists);
    }

    @Override
    public final void startOpaqueContainer(final NodeIdentifier name, final int childSizeHint) throws IOException {
        enter(name);
        builders.push(childSizeHint == NormalizedNodeStreamWriter.UNKNOWN_SIZE
                ? new OpaqueDataContainerBuilder() : new OpaqueDataContainerBuilder(childSizeHint));
    }

    @Override
    public final void startOpaqueList(final NodeIdentifier name, final int childSizeHint) throws IOException {
        enter(name);
        builders.push(childSizeHint == NormalizedNodeStreamWriter.UNKNOWN_SIZE ? new OpaqueDataListBuilder()
                : new OpaqueDataListBuilder(childSizeHint));
    }

    @Override
    public final void opaqueValue(final Object value) {
        final OpaqueDataValueBuilder valueBuilder = builders.pop().withValue(value);
        builders.push(valueBuilder);
    }

    @Override
    public final void endNode() throws IOException {
        final AbstractOpaqueDataNodeBuilder<?> doneBuilder = builders.pop();
        final AbstractOpaqueDataNodeBuilder<?> parent = builders.peek();
        final OpaqueDataNode current = doneBuilder.build();
        if (parent == null) {
            // End of data, build the node.
            finishAnydata(builder.build());
        } else {
            verify(parent instanceof AbstractOpaqueDataContainerBuilder);
            exit();
            ((AbstractOpaqueDataContainerBuilder<?>) parent).withChild(current);
        }
    }

    protected void enter(final NodeIdentifier name) throws IOException {
        // No-op default
    }

    protected void exit() throws IOException {
        // No-op defualt
    }

    protected abstract void finishAnydata(@NonNull OpaqueData opaqueData);
}
