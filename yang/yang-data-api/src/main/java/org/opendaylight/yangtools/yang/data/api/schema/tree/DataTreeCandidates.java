/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class holding methods useful when dealing with {@link DataTreeCandidate} instances.
 */
@Beta
public final class DataTreeCandidates {
    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidates.class);

    private DataTreeCandidates() {
        // Hidden on purpose
    }

    public static @NonNull DataTreeCandidate newDataTreeCandidate(final YangInstanceIdentifier rootPath,
            final DataTreeCandidateNode rootNode) {
        return new DefaultDataTreeCandidate(rootPath, rootNode);
    }

    public static @NonNull DataTreeCandidate fromNormalizedNode(final YangInstanceIdentifier rootPath,
            final NormalizedNode<?, ?> node) {
        return new DefaultDataTreeCandidate(rootPath, new NormalizedNodeDataTreeCandidateNode(node));
    }

    public static void applyToCursor(final DataTreeModificationCursor cursor, final DataTreeCandidate candidate) {
        DataTreeCandidateNodes.applyToCursor(cursor, candidate.getRootNode());
    }

    public static void applyToModification(final DataTreeModification modification,
            final DataTreeCandidate candidate) {
        if (modification instanceof CursorAwareDataTreeModification) {
            applyToCursorAwareModification((CursorAwareDataTreeModification) modification, candidate);
            return;
        }

        final DataTreeCandidateNode node = candidate.getRootNode();
        final YangInstanceIdentifier path = candidate.getRootPath();
        switch (node.getModificationType()) {
            case DELETE:
                modification.delete(path);
                LOG.debug("Modification {} deleted path {}", modification, path);
                break;
            case SUBTREE_MODIFIED:
                LOG.debug("Modification {} modified path {}", modification, path);

                NodeIterator iterator = new NodeIterator(null, path, node.getChildNodes().iterator());
                do {
                    iterator = iterator.next(modification);
                } while (iterator != null);
                break;
            case UNMODIFIED:
                LOG.debug("Modification {} unmodified path {}", modification, path);
                // No-op
                break;
            case WRITE:
                modification.write(path, node.getDataAfter().get());
                LOG.debug("Modification {} written path {}", modification, path);
                break;
            default:
                throw new IllegalArgumentException("Unsupported modification " + node.getModificationType());
        }
    }

    // Compress List of DataTreeCandidate into one single DataTreeCandidate, summarizing all changes done between
    // first and last DataTreeCandidate
    public static DataTreeCandidate aggregate(List<DataTreeCandidate> candidates) {
        final Iterator<DataTreeCandidate> it = candidates.iterator();
        checkArgument(it.hasNext(), "Input must not be empty");
        final DataTreeCandidate first = it.next();
        if (!it.hasNext()) {
            // Short-circuit
            return first;
        }
        final YangInstanceIdentifier rootPath = first.getRootPath();
        final List<DataTreeCandidateNode> roots = new ArrayList<>(candidates.size());
        roots.add(first.getRootNode());
        it.forEachRemaining(candidate -> {
            final YangInstanceIdentifier root = candidate.getRootPath();
            checkArgument(rootPath.equals(root), "Expecting root path %s, encountered %s", rootPath, root);
            roots.add(candidate.getRootNode());
        });
        return DataTreeCandidates.newDataTreeCandidate(rootPath, compressNodes(roots));
    }

    private static DataTreeCandidateNode compressNodes(final List<DataTreeCandidateNode> input) {
        checkArgument(!input.isEmpty());
        // Single node: already compressed
        final DataTreeCandidateNode first = input.get(0);
        if (input.size() == 1) {
            return first;
        }
        // Fast path: check last node being a terminal node
        final DataTreeCandidateNode last = input.get(input.size() - 1);
        switch (last.getModificationType()) {
            case DELETE:
                ModificationType previous = first.getModificationType();
                // Check if node had data before
                if (previous == ModificationType.DELETE
                        || previous == ModificationType.DISAPPEARED
                            || (previous == ModificationType.UNMODIFIED && first.getDataBefore().isEmpty())) {
                    throw new IllegalArgumentException("Delete modification event on empty node");
                }

                if (first.getDataBefore().isEmpty()) {
                    return new TerminalDataTreeCandidateNode(last.getIdentifier(),ModificationType.UNMODIFIED,
                            null,null);
                }
                else {
                    return new TerminalDataTreeCandidateNode(last.getIdentifier(), last.getModificationType(),
                            first.getDataBefore().get(), null);
                }
            case WRITE:
                if (first.getDataBefore().isEmpty()) {
                    return new TerminalDataTreeCandidateNode(last.getIdentifier(), last.getModificationType(),
                            null, last.getDataAfter().get());
                }
                // Check if data before equals to data after
                if (first.getDataBefore().get().getValue().equals(last.getDataAfter().get().getValue())) {
                    return new TerminalDataTreeCandidateNode(last.getIdentifier(), ModificationType.UNMODIFIED,
                            first.getDataBefore().get(),last.getDataAfter().get());
                }

                return new TerminalDataTreeCandidateNode(last.getIdentifier(), last.getModificationType(),
                        first.getDataBefore().get(), last.getDataAfter().get());
            case APPEARED:
            case DISAPPEARED:
            case SUBTREE_MODIFIED:
            case UNMODIFIED:
                // No luck, we need to iterate
                return slowCompressNodes(first, input);
            default:
                throw new UnsupportedOperationException("Unsupported modification type " + last.getModificationType());
        }
    }

    private static DataTreeCandidateNode slowCompressNodes(final DataTreeCandidateNode first,
                                                           final List<DataTreeCandidateNode> input) {
        // finalNode contains summarized changes
        TerminalDataTreeCandidateNode finalNode = new TerminalDataTreeCandidateNode(
                first.getIdentifier(),
                ModificationType.UNMODIFIED,
                first.getDataBefore().orElse(null),
                first.getDataBefore().orElse(null));

        input.forEach(node -> {
            compressNode(finalNode,node,null);
        });
        finalNode.setAfter(input.get(input.size() - 1).getDataAfter());
        return cleanUpTree(finalNode);
    }

    private static void compressNode(TerminalDataTreeCandidateNode finalNode,
                                     DataTreeCandidateNode node, YangInstanceIdentifier.PathArgument parent) {
        YangInstanceIdentifier.PathArgument identifier = node.getIdentifier();
        // Check if finalNode has stored any changes for node
        if (finalNode.getNode(identifier).equals(Optional.empty())) {
            TerminalDataTreeCandidateNode parentNode = finalNode.getNode(parent).get();
            TerminalDataTreeCandidateNode childNode = new TerminalDataTreeCandidateNode(
                    identifier,
                    ModificationType.UNMODIFIED,
                    node.getDataBefore().orElse(null),
                    node.getDataBefore().orElse(null),
                    parentNode);
            parentNode.addChildNode(childNode);
        }

        switch (node.getModificationType()) {
            case UNMODIFIED:
                break;
            case WRITE:
            case DELETE:
            case APPEARED:
            case DISAPPEARED:
            case SUBTREE_MODIFIED:
                finalNode.setModification(identifier,
                        compressModifications(finalNode.getModification(identifier),
                                              node.getModificationType(),
                                              finalNode.getDataAfter(identifier).isEmpty()));
                finalNode.setData(identifier,node.getDataAfter());
                // If node is unmodified there is no need iterate through its child nodes
                if (finalNode.getModificationType() == ModificationType.UNMODIFIED) {
                    break;
                }

                node.getChildNodes().forEach(child -> {
                    compressNode(finalNode,child,identifier);
                });
                break;
            default:
                throw new UnsupportedOperationException("Unsupported modification type " + node.getModificationType());
        }
    }

    // Removes redundant changes
    private static DataTreeCandidateNode cleanUpTree(TerminalDataTreeCandidateNode finalNode) {
        return cleanUpTree(finalNode,finalNode);
    }

    private static DataTreeCandidateNode cleanUpTree(TerminalDataTreeCandidateNode finalNode,
                                                     DataTreeCandidateNode node) {
        YangInstanceIdentifier.PathArgument identifier = node.getIdentifier();
        ModificationType nodeModification = node.getModificationType();
        node.getChildNodes().forEach(child -> {
            cleanUpTree(finalNode,child);
        });
        Collection<DataTreeCandidateNode> childNodes = node.getChildNodes();
        Optional<NormalizedNode<?,?>> dataBefore = finalNode.getDataBefore(identifier);
        Optional<NormalizedNode<?,?>> dataAfter = node.getDataAfter();

        switch (nodeModification) {
            case UNMODIFIED:
                finalNode.deleteNode(identifier);
                return finalNode;
            case WRITE:
                if (dataBefore.isEmpty()) {
                    return finalNode;
                }
                if (dataBefore.get().getValue().equals(dataAfter.get().getValue())) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            case DELETE:
                if (dataBefore.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            case APPEARED:
                if (dataBefore.isPresent()) {
                    if (dataBefore.get().getValue().equals(dataAfter.get().getValue())) {
                        finalNode.deleteNode(identifier);
                        return finalNode;
                    }
                    else {
                        throw new IllegalArgumentException("Appear modification event on Write node");
                    }
                }
                if (childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            case DISAPPEARED:
                if (finalNode.getDataBefore(identifier).isEmpty()) {
                    throw new IllegalArgumentException("Disappear modification event on Delete node");
                }
                if (childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                    return finalNode;
                }
                return finalNode;
            case SUBTREE_MODIFIED:
                if (dataBefore.isEmpty()) {
                    throw new IllegalArgumentException("Subtree modification event on Delete node");
                }
                if (childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            default:
                throw new UnsupportedOperationException("Unsupported modification type " + nodeModification);
        }
    }

    private static ModificationType compressModifications(ModificationType firstModification,
                                                          ModificationType secondModification,boolean hasNoDataBefore) {
        switch (firstModification) {
            case UNMODIFIED:
                if (hasNoDataBefore) {
                    switch (secondModification) {
                        case UNMODIFIED:
                        case WRITE:
                        case APPEARED:
                            return secondModification;
                        case DELETE:
                            throw new IllegalArgumentException("Delete modification event on empty node");
                        case SUBTREE_MODIFIED:
                            throw new IllegalArgumentException("Subtree modification event on empty node");
                        case DISAPPEARED:
                            throw new IllegalArgumentException("Disappear modification event on empty node");
                    }
                }
                if (secondModification == ModificationType.APPEARED) {
                    throw new IllegalArgumentException("Appear modification event on non empty node");
                }
                return secondModification;
            case WRITE:
                switch (secondModification) {
                    case UNMODIFIED:
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        return ModificationType.WRITE;
                    case DELETE:
                        return ModificationType.DELETE;
                    case DISAPPEARED:
                        return ModificationType.DISAPPEARED;
                    case APPEARED:
                        throw new IllegalArgumentException(
                                "Appear modification event on " + firstModification + " node");
                    default:
                        throw new UnsupportedOperationException(
                                "Unsupported modification type " + secondModification);
                }
            case DELETE:
                switch (secondModification) {
                    case UNMODIFIED:
                        return ModificationType.DELETE;
                    case WRITE:
                    case APPEARED:
                        return ModificationType.WRITE;
                    case DELETE:
                        throw new IllegalArgumentException(
                                "Delete modification event on " + firstModification + " node");
                    case DISAPPEARED:
                        throw new IllegalArgumentException(
                                "Disappear modification event on " + firstModification + " node");
                    case SUBTREE_MODIFIED:
                        throw new IllegalArgumentException(
                                "Subtree modification modification event on " + firstModification + " node");
                    default:
                        throw new UnsupportedOperationException("Unsupported modification type " + secondModification);
                }
            case APPEARED:
                switch (secondModification) {
                    case UNMODIFIED:
                    case SUBTREE_MODIFIED:
                        return ModificationType.APPEARED;
                    case DELETE:
                    case DISAPPEARED:
                        return ModificationType.UNMODIFIED;
                    case WRITE:
                        return ModificationType.WRITE;
                    case APPEARED:
                        throw new IllegalArgumentException(
                                "Appear modification event on " + firstModification + " node");
                    default:
                        throw new UnsupportedOperationException("Unsupported modification type " + secondModification);
                }
            case DISAPPEARED:
                switch (secondModification) {
                    case UNMODIFIED:
                        return ModificationType.UNMODIFIED;
                    case WRITE:
                        return ModificationType.WRITE;
                    case APPEARED:
                        return ModificationType.SUBTREE_MODIFIED;
                    case DELETE:
                        throw new IllegalArgumentException(
                                "Delete modification event on " + firstModification + " node");
                    case DISAPPEARED:
                        throw new IllegalArgumentException(
                                "Disappear modification event on " + firstModification + " node");
                    case SUBTREE_MODIFIED:
                        throw new IllegalArgumentException(
                                "Subtree modification modification event on " + firstModification + " node");
                    default:
                        throw new UnsupportedOperationException("Unsupported modification type " + secondModification);
                }
            case SUBTREE_MODIFIED:
                switch (secondModification) {
                    case UNMODIFIED:
                    case SUBTREE_MODIFIED:
                        return ModificationType.SUBTREE_MODIFIED;
                    case WRITE:
                        return ModificationType.WRITE;
                    case DELETE:
                        return ModificationType.DELETE;
                    case DISAPPEARED:
                        return ModificationType.DISAPPEARED;
                    case APPEARED:
                        throw new IllegalArgumentException(
                                "Appear modification event on " + firstModification + " node");
                    default:
                        throw new UnsupportedOperationException("Unsupported modification type " + secondModification);
                }
            default:
                throw new UnsupportedOperationException("Unsupported modification type " + secondModification);
        }
    }

    private static void applyToCursorAwareModification(final CursorAwareDataTreeModification modification,
            final DataTreeCandidate candidate) {
        final YangInstanceIdentifier candidatePath = candidate.getRootPath();
        if (candidatePath.isEmpty()) {
            try (DataTreeModificationCursor cursor = modification.openCursor()) {
                DataTreeCandidateNodes.applyRootToCursor(cursor, candidate.getRootNode());
            }
        } else {
            try (DataTreeModificationCursor cursor = modification.openCursor(candidatePath.getParent()).get()) {
                DataTreeCandidateNodes.applyRootedNodeToCursor(cursor, candidatePath, candidate.getRootNode());
            }
        }
    }

    private static final class NodeIterator {
        private final Iterator<DataTreeCandidateNode> iterator;
        private final YangInstanceIdentifier path;
        private final NodeIterator parent;

        NodeIterator(final @Nullable NodeIterator parent, final YangInstanceIdentifier path,
                final Iterator<DataTreeCandidateNode> iterator) {
            this.iterator = requireNonNull(iterator);
            this.path = requireNonNull(path);
            this.parent = parent;
        }

        NodeIterator next(final DataTreeModification modification) {
            while (iterator.hasNext()) {
                final DataTreeCandidateNode node = iterator.next();
                final YangInstanceIdentifier child = path.node(node.getIdentifier());

                switch (node.getModificationType()) {
                    case DELETE:
                        modification.delete(child);
                        LOG.debug("Modification {} deleted path {}", modification, child);
                        break;
                    case APPEARED:
                    case DISAPPEARED:
                    case SUBTREE_MODIFIED:
                        LOG.debug("Modification {} modified path {}", modification, child);
                        return new NodeIterator(this, child, node.getChildNodes().iterator());
                    case UNMODIFIED:
                        LOG.debug("Modification {} unmodified path {}", modification, child);
                        // No-op
                        break;
                    case WRITE:
                        modification.write(child, node.getDataAfter().get());
                        LOG.debug("Modification {} written path {}", modification, child);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported modification " + node.getModificationType());
                }
            }

            return parent;
        }
    }
}
