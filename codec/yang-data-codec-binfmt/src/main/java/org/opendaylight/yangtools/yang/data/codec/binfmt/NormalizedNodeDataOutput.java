/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName.QNameAwareDataOutput;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

/**
 * Interface for emitting {@link NormalizedNode}s, {@link YangInstanceIdentifier}s, {@link PathArgument}s
 * and {@link SchemaNodeIdentifier}s.
 */
@NonNullByDefault
public interface NormalizedNodeDataOutput extends AutoCloseable, QNameAwareDataOutput {
    /**
     * Write a {@link NormalizedNode}.
     *
     * @param normalizedNode NormalizedNode to write
     * @throws IOException if an error occurs
     * @throws NullPointerException if {@code normalizedNode} is {@code null}
     */
    void writeNormalizedNode(NormalizedNode normalizedNode) throws IOException;

    /**
     * Write a {@link PathArgument}.
     *
     * @param pathArgument PathArgument to write
     * @throws IOException if an error occurs
     * @throws NullPointerException if {@code pathArgument} is {@code null}
     */
    void writePathArgument(PathArgument pathArgument) throws IOException;

    /**
     * Write a {@link YangInstanceIdentifier}.
     *
     * @param identifier YangInstanceIdentifier to write
     * @throws IOException if an error occurs
     * @throws NullPointerException if {@code identifier} is {@code null}
     */
    void writeYangInstanceIdentifier(YangInstanceIdentifier identifier) throws IOException;

    /**
     * Write a {@link SchemaNodeIdentifier}.
     *
     * @param path SchemaNodeIdentifier to write
     * @throws IOException if an error occurs
     * @throws NullPointerException if {@code path} is {@code null}
     */
    void writeSchemaNodeIdentifier(SchemaNodeIdentifier path) throws IOException;

    /**
     * Write a {@link NormalizedNode} or {@code null} value.
     *
     * @param normalizedNode NormalizedNode to write, perhapss {@code null}, which will be restored on read.
     * @throws IOException if an error occurs
     * @throws NullPointerException if {@code normalizedNode} is {@code null}
     */
    default void writeOptionalNormalizedNode(final @Nullable NormalizedNode normalizedNode) throws IOException {
        if (normalizedNode != null) {
            writeBoolean(true);
            writeNormalizedNode(normalizedNode);
        } else {
            writeBoolean(false);
        }
    }

    /**
     * {@inheritDoc}
     * @throws IOException if an error occurs
     */
    @Override
    void close() throws IOException;
}
