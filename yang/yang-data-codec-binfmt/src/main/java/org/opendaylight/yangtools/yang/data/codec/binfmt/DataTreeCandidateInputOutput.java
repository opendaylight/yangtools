/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ReusableStreamReceiver;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.impl.schema.ReusableImmutableNormalizedNodeStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility serialization/deserialization for {@link DataTreeCandidate}. Note that this utility does not maintain
 * before-image information across serialization.
 *
 * @author Robert Varga
 */
@Beta

public final class DataTreeCandidateInputOutput {
    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidateInputOutput.class);

    /*
     * A denser layout can be achieved when we consider split the single byte into two logical field. Bottom-most three
     * bits are used to indicate which ModificationType has occured and the top-most five bits indicate how many
     * children the node has.
     *
     * In case there are fewer than 29 children, the number of children is directly encoded in the bits.
     * If there are 29-284 children, '29' is encoded in the field and an additional unsigned byte is appended.
     * If there are 285-65820 children, '30' is encoded in the field and an additional unsigned short is appended.
     * If there are more than 655820, '31' is encoded in the and an additional signed int is appended.
     */
    private static final byte UNMODIFIED       = 0x00;
    private static final byte SUBTREE_MODIFIED = 0x01;
    private static final byte WRITE            = 0x02;
    private static final byte DELETE           = 0x03;
    private static final byte APPEARED         = 0x04;
    private static final byte DISAPPEARED      = 0x05;

    private static final byte TYPE_MASK        = 0x07;
    private static final byte SIZE_SHIFT       = 3;

    private static final byte SIZE_0           = 0;
    private static final byte SIZE_28          = 28;
    private static final byte SIZE_1B          = 29;
    private static final byte SIZE_2B          = 30;
    private static final byte SIZE_4B          = 31;

    private DataTreeCandidateInputOutput() {

    }

    public static @NonNull DataTreeCandidate readDataTreeCandidate(final NormalizedNodeDataInput in)
            throws IOException {
        return readDataTreeCandidate(in, ReusableImmutableNormalizedNodeStreamWriter.create());
    }

    public static @NonNull DataTreeCandidate readDataTreeCandidate(final NormalizedNodeDataInput in,
            final ReusableStreamReceiver receiver) throws IOException {
        final YangInstanceIdentifier rootPath = in.readYangInstanceIdentifier();
        final byte type = in.readByte();

        final DataTreeCandidateNode rootNode;
        switch (type) {
            case APPEARED:
                rootNode = ModifiedDataTreeCandidateNode.create(ModificationType.APPEARED, readChildren(in, receiver));
                break;
            case DELETE:
                rootNode = DeletedDataTreeCandidateNode.create();
                break;
            case DISAPPEARED:
                rootNode = ModifiedDataTreeCandidateNode.create(ModificationType.DISAPPEARED,
                    readChildren(in, receiver));
                break;
            case SUBTREE_MODIFIED:
                rootNode = ModifiedDataTreeCandidateNode.create(ModificationType.SUBTREE_MODIFIED,
                    readChildren(in, receiver));
                break;
            case WRITE:
                rootNode = DataTreeCandidateNodes.written(in.readNormalizedNode(receiver));
                break;
            case UNMODIFIED:
                rootNode = UnmodifiedRootDataTreeCandidateNode.INSTANCE;
                break;
            default:
                throw new IllegalArgumentException("Unhandled node type " + type);
        }

        return DataTreeCandidates.newDataTreeCandidate(rootPath, rootNode);
    }

    public static void writeDataTreeCandidate(final NormalizedNodeDataOutput out, final DataTreeCandidate candidate)
            throws IOException {
        out.writeYangInstanceIdentifier(candidate.getRootPath());

        final DataTreeCandidateNode node = candidate.getRootNode();
        switch (node.getModificationType()) {
            case APPEARED:
                out.writeByte(APPEARED);
                writeChildren(out, node.getChildNodes());
                break;
            case DELETE:
                out.writeByte(DELETE);
                break;
            case DISAPPEARED:
                out.writeByte(DISAPPEARED);
                writeChildren(out, node.getChildNodes());
                break;
            case SUBTREE_MODIFIED:
                out.writeByte(SUBTREE_MODIFIED);
                writeChildren(out, node.getChildNodes());
                break;
            case UNMODIFIED:
                out.writeByte(UNMODIFIED);
                break;
            case WRITE:
                out.writeByte(WRITE);
                out.writeNormalizedNode(node.getDataAfter().get());
                break;
            default:
                throwUnhandledNodeType(node);
        }
    }

    private static DataTreeCandidateNode readModifiedNode(final ModificationType type, final NormalizedNodeDataInput in,
            final ReusableStreamReceiver receiver) throws IOException {
        final PathArgument identifier = in.readPathArgument();
        final Collection<DataTreeCandidateNode> children = readChildren(in, receiver);
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

        final Collection<DataTreeCandidateNode> ret = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            final DataTreeCandidateNode child = readNode(in, receiver);
            if (child != null) {
                ret.add(child);
            }
        }
        return ret;
    }

    private static DataTreeCandidateNode readNode(final NormalizedNodeDataInput in,
            final ReusableStreamReceiver receiver) throws IOException {
        final byte type = in.readByte();
        switch (type) {
            case APPEARED:
                return readModifiedNode(ModificationType.APPEARED, in, receiver);
            case DELETE:
                return DeletedDataTreeCandidateNode.create(in.readPathArgument());
            case DISAPPEARED:
                return readModifiedNode(ModificationType.DISAPPEARED, in, receiver);
            case SUBTREE_MODIFIED:
                return readModifiedNode(ModificationType.SUBTREE_MODIFIED, in, receiver);
            case UNMODIFIED:
                return null;
            case WRITE:
                return DataTreeCandidateNodes.written(in.readNormalizedNode(receiver));
            default:
                throw new IllegalArgumentException("Unhandled node type " + type);
        }
    }

    private static void writeChildren(final NormalizedNodeDataOutput out,
            final Collection<DataTreeCandidateNode> children) throws IOException {
        out.writeInt(children.size());
        for (DataTreeCandidateNode child : children) {
            writeNode(out, child);
        }
    }

    private static void writeNode(final NormalizedNodeDataOutput out, final DataTreeCandidateNode node)
            throws IOException {
        switch (node.getModificationType()) {
            case APPEARED:
                out.writeByte(APPEARED);
                out.writePathArgument(node.getIdentifier());
                writeChildren(out, node.getChildNodes());
                break;
            case DELETE:
                out.writeByte(DELETE);
                out.writePathArgument(node.getIdentifier());
                break;
            case DISAPPEARED:
                out.writeByte(DISAPPEARED);
                out.writePathArgument(node.getIdentifier());
                writeChildren(out, node.getChildNodes());
                break;
            case SUBTREE_MODIFIED:
                out.writeByte(SUBTREE_MODIFIED);
                out.writePathArgument(node.getIdentifier());
                writeChildren(out, node.getChildNodes());
                break;
            case WRITE:
                out.writeByte(WRITE);
                out.writeNormalizedNode(node.getDataAfter().get());
                break;
            case UNMODIFIED:
                out.writeByte(UNMODIFIED);
                break;
            default:
                throwUnhandledNodeType(node);
        }
    }

    private static void throwUnhandledNodeType(final DataTreeCandidateNode node) {
        throw new IllegalArgumentException("Unhandled node type " + node.getModificationType());
    }
}
