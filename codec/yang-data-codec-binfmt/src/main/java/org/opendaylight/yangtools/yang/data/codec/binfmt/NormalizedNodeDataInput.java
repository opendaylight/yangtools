/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.io.DataInput;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName.QNameAwareDataInput;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ReusableStreamReceiver;
import org.opendaylight.yangtools.yang.data.impl.schema.ReusableImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

/**
 * Interface for reading {@link NormalizedNode}s, {@link YangInstanceIdentifier}s, {@link PathArgument}s
 * and {@link SchemaNodeIdentifier}s.
 */
public interface NormalizedNodeDataInput extends QNameAwareDataInput {
    /**
     * Interpret current stream position as a NormalizedNode, stream its events into a NormalizedNodeStreamWriter.
     *
     * @param writer Writer to emit events to
     * @throws IOException if an error occurs
     * @throws IllegalStateException if the dictionary has been detached
     * @throws NullPointerException if {@code writer} is {@code null}
     */
    void streamNormalizedNode(NormalizedNodeStreamWriter writer) throws IOException;

    /**
     * Read a normalized node from the reader.
     *
     * @return Next node from the stream, or null if end of stream has been reached.
     * @throws IOException if an error occurs
     * @throws IllegalStateException if the dictionary has been detached
     */
    default NormalizedNode readNormalizedNode() throws IOException {
        return readNormalizedNode(ReusableImmutableNormalizedNodeStreamWriter.create());
    }

    /**
     * Read a normalized node from the reader, using specified writer to construct the result.
     *
     * @param receiver Reusable receiver to, expected to be reset
     * @return Next node from the stream, or null if end of stream has been reached.
     * @throws IOException if an error occurs
     * @throws IllegalStateException if the dictionary has been detached
     * @throws NullPointerException if {@code receiver} is {@code null}
     */
    default NormalizedNode readNormalizedNode(final ReusableStreamReceiver receiver) throws IOException {
        try {
            streamNormalizedNode(receiver);
            return receiver.getResult();
        } finally {
            receiver.reset();
        }
    }

    YangInstanceIdentifier readYangInstanceIdentifier() throws IOException;

    PathArgument readPathArgument() throws IOException;

    SchemaNodeIdentifier readSchemaNodeIdentifier() throws IOException;

    /**
     * Return the version of the underlying input stream.
     *
     * @return Stream version
     * @throws IOException if the version cannot be ascertained
     */
    NormalizedNodeStreamVersion getVersion() throws IOException;

    default Optional<NormalizedNode> readOptionalNormalizedNode() throws IOException {
        return readBoolean() ? Optional.of(readNormalizedNode()) : Optional.empty();
    }

    /**
     * Creates a new {@link NormalizedNodeDataInput} instance that reads from the given input. This method first reads
     * and validates that the input contains a valid NormalizedNode stream.
     *
     * @param input the DataInput to read from
     * @return a new {@link NormalizedNodeDataInput} instance
     * @throws InvalidNormalizedNodeStreamException if the stream version is not supported
     * @throws IOException if an error occurs reading from the input
     * @throws NullPointerException if {@code input} is {@code null}
     */
    static @NonNull NormalizedNodeDataInput newDataInput(final @NonNull DataInput input) throws IOException {
        return new VersionedNormalizedNodeDataInput(input).delegate();
    }

    /**
     * Creates a new {@link NormalizedNodeDataInput} instance that reads from the given input. This method does not
     * perform any initial validation of the input stream.
     *
     * @param input the DataInput to read from
     * @return a new {@link NormalizedNodeDataInput} instance
     * @deprecated Use {@link #newDataInput(DataInput)} instead.
     */
    @Deprecated(since = "5.0.0", forRemoval = true)
    static @NonNull NormalizedNodeDataInput newDataInputWithoutValidation(final @NonNull DataInput input) {
        return new VersionedNormalizedNodeDataInput(input);
    }
}
