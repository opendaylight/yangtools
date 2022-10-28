/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ReusableStreamReceiver;
import org.opendaylight.yangtools.yang.data.impl.schema.ReusableImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidateNodes;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility serialization/deserialization for {@link DataTreeCandidate}. Note that this utility does not maintain
 * before-image information across serialization.
 */
public final class DataTreeCandidateInputOutput {
    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidateInputOutput.class);
    private static final byte DELETE = 0;
    private static final byte SUBTREE_MODIFIED = 1;
    private static final byte UNMODIFIED = 2;
    private static final byte WRITE = 3;
    private static final byte APPEARED = 4;
    private static final byte DISAPPEARED = 5;

    private DataTreeCandidateInputOutput() {
        // Hidden on purpose
    }

    public static @NonNull DataTreeCandidate readDataTreeCandidate(final NormalizedNodeDataInput in)
            throws IOException {
        return readDataTreeCandidate(in, ReusableImmutableNormalizedNodeStreamWriter.create());
    }

    public static @NonNull DataTreeCandidate readDataTreeCandidate(final NormalizedNodeDataInput in,
            final ReusableStreamReceiver receiver) throws IOException {
        final var rootPath = in.readYangInstanceIdentifier();
        final byte type = in.readByte();

        final var rootNode = switch (type) {
            case APPEARED -> ModifiedDataTreeCandidateNode.create(
                ModificationType.APPEARED, readChildren(in, receiver));
            case DELETE -> DeletedDataTreeCandidateNode.create();
            case DISAPPEARED -> ModifiedDataTreeCandidateNode.create(
                ModificationType.DISAPPEARED, readChildren(in, receiver));
            case SUBTREE_MODIFIED -> ModifiedDataTreeCandidateNode.create(
                ModificationType.SUBTREE_MODIFIED, readChildren(in, receiver));
            case WRITE -> DataTreeCandidateNodes.written(in.readNormalizedNode(receiver));
            case UNMODIFIED -> UnmodifiedRootDataTreeCandidateNode.INSTANCE;
            default -> throw unhandledNodeType(type);
        };
        return DataTreeCandidates.newDataTreeCandidate(rootPath, rootNode);
    }

    public static void writeDataTreeCandidate(final NormalizedNodeDataOutput out, final DataTreeCandidate candidate)
            throws IOException {
        out.writeYangInstanceIdentifier(candidate.getRootPath());

        final var node = candidate.getRootNode();
        switch (node.getModificationType()) {
            case APPEARED -> {
                out.writeByte(APPEARED);
                writeChildren(out, node.getChildNodes());
            }
            case DELETE -> out.writeByte(DELETE);
            case DISAPPEARED -> {
                out.writeByte(DISAPPEARED);
                writeChildren(out, node.getChildNodes());
            }
            case SUBTREE_MODIFIED -> {
                out.writeByte(SUBTREE_MODIFIED);
                writeChildren(out, node.getChildNodes());
            }
            case UNMODIFIED -> out.writeByte(UNMODIFIED);
            case WRITE -> {
                out.writeByte(WRITE);
                out.writeNormalizedNode(node.getDataAfter().get());
            }
            default -> throw unhandledNodeType(node);
        }
    }

    private static DataTreeCandidateNode readModifiedNode(final ModificationType type, final NormalizedNodeDataInput in,
            final ReusableStreamReceiver receiver) throws IOException {
        final var identifier = in.readPathArgument();
        final var children = readChildren(in, receiver);
        if (children.isEmpty()) {
            LOG.debug("Modified node {} does not have any children, not instantiating it", identifier);
            return null;
        }

        return ModifiedDataTreeCandidateNode.create(identifier, type, children);
    }

    private static Collection<DataTreeCandidateNode> readChildren(final NormalizedNodeDataInput in,
            final ReusableStreamReceiver receiver) throws IOException {
        final int size = in.readInt();
        if (size == 0) {
            return ImmutableList.of();
        }

        final var ret = new ArrayList<DataTreeCandidateNode>(size);
        for (int i = 0; i < size; ++i) {
            final var child = readNode(in, receiver);
            if (child != null) {
                ret.add(child);
            }
        }
        return ret;
    }

    private static DataTreeCandidateNode readNode(final NormalizedNodeDataInput in,
            final ReusableStreamReceiver receiver) throws IOException {
        final byte type = in.readByte();
        return switch (type) {
            case APPEARED -> readModifiedNode(ModificationType.APPEARED, in, receiver);
            case DELETE -> DeletedDataTreeCandidateNode.create(in.readPathArgument());
            case DISAPPEARED -> readModifiedNode(ModificationType.DISAPPEARED, in, receiver);
            case SUBTREE_MODIFIED -> readModifiedNode(ModificationType.SUBTREE_MODIFIED, in, receiver);
            case UNMODIFIED -> null;
            case WRITE -> DataTreeCandidateNodes.written(in.readNormalizedNode(receiver));
            default -> throw unhandledNodeType(type);
        };
    }

    private static void writeChildren(final NormalizedNodeDataOutput out,
            final Collection<DataTreeCandidateNode> children) throws IOException {
        out.writeInt(children.size());
        for (var child : children) {
            writeNode(out, child);
        }
    }

    private static void writeNode(final NormalizedNodeDataOutput out, final DataTreeCandidateNode node)
            throws IOException {
        switch (node.getModificationType()) {
            case APPEARED -> {
                out.writeByte(APPEARED);
                out.writePathArgument(node.getIdentifier());
                writeChildren(out, node.getChildNodes());
            }
            case DELETE -> {
                out.writeByte(DELETE);
                out.writePathArgument(node.getIdentifier());
            }
            case DISAPPEARED -> {
                out.writeByte(DISAPPEARED);
                out.writePathArgument(node.getIdentifier());
                writeChildren(out, node.getChildNodes());
            }
            case SUBTREE_MODIFIED -> {
                out.writeByte(SUBTREE_MODIFIED);
                out.writePathArgument(node.getIdentifier());
                writeChildren(out, node.getChildNodes());
            }
            case WRITE -> {
                out.writeByte(WRITE);
                out.writeNormalizedNode(node.getDataAfter().get());
            }
            case UNMODIFIED -> {
                out.writeByte(UNMODIFIED);
            }
            default -> throw unhandledNodeType(node);
        }
    }

    private static IllegalArgumentException unhandledNodeType(final byte type) {
        return new IllegalArgumentException("Unhandled node type " + type);
    }

    private static IllegalArgumentException unhandledNodeType(final DataTreeCandidateNode node) {
        return new IllegalArgumentException("Unhandled node type " + node.getModificationType());
    }
}
