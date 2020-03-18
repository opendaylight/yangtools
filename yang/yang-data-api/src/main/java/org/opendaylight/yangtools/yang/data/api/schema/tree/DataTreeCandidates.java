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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
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

    public static void applyToModification(final DataTreeModification modification, final DataTreeCandidate candidate) {
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

    /**
     * Compress a list of DataTreeCandidates into a single DataTreeCandidate. The resulting candidate is a summarization
     * of changes recorded in the input candidates.
     *
     * @param candidates Input list, must be non-empty
     * @return Summarized DataTreeCandidate
     * @throws IllegalArgumentException if candidates is empty, or contains candidates with mismatched root path
     * @throws NullPointerException     if {@code candidates} is null or contains a null entry
     */
    public static @NonNull DataTreeCandidate aggregate(@NonNull final List<? extends DataTreeCandidate> candidates) {
        final Iterator<? extends DataTreeCandidate> it = candidates.iterator();
        checkArgument(it.hasNext(), "Input must not be empty");
        final DataTreeCandidate first = requireNonNull(it.next(), "Input must not contain null entries");
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
        return DataTreeCandidates.newDataTreeCandidate(rootPath, fastCompressNode(roots.get(0), roots));
    }

    private static DataTreeCandidateNode fastCompressNode(final DataTreeCandidateNode first,
                                                          final List<DataTreeCandidateNode> input) {
        final DataTreeCandidateNode last = input.get(input.size() - 1);
        ModificationType nodeModification = last.getModificationType();
        Optional<NormalizedNode<?, ?>> dataBefore = first.getDataBefore();
        Optional<NormalizedNode<?, ?>> dataAfter = last.getDataAfter();
        switch (nodeModification) {
            case DELETE:
                ModificationType previous = first.getModificationType();
                // Check if node had data before
                if (previous == ModificationType.DELETE
                        || previous == ModificationType.DISAPPEARED
                        || previous == ModificationType.UNMODIFIED && dataBefore.isEmpty()) {
                    illegalModification(ModificationType.DELETE, ModificationType.DELETE);
                }
                if (dataBefore.isEmpty()) {
                    return new TerminalDataTreeCandidateNode(null, ModificationType.UNMODIFIED, null, null);
                }
                return new TerminalDataTreeCandidateNode(null, nodeModification, dataBefore.get(), null);
            case WRITE:
                return new TerminalDataTreeCandidateNode(null, nodeModification, dataBefore.orElse(null),
                        dataAfter.orElseThrow());
            case APPEARED:
            case DISAPPEARED:
            case SUBTREE_MODIFIED:
            case UNMODIFIED:
                // No luck, we need to iterate
                return slowCompressNodes(first, input);
            default:
                throw new IllegalStateException("Unsupported modification type " + nodeModification);
        }
    }

    private static DataTreeCandidateNode slowCompressNodes(final DataTreeCandidateNode first,
                                                           final List<DataTreeCandidateNode> input) {
        // finalNode contains summarized changes
        TerminalDataTreeCandidateNode finalNode = new TerminalDataTreeCandidateNode(
                null, first.getDataBefore().orElse(null));
        input.forEach(node -> compressNode(finalNode, node, null));
        finalNode.setAfter(input.get(input.size() - 1).getDataAfter().orElse(null));
        return cleanUpTree(finalNode);
    }

    private static void compressNode(final TerminalDataTreeCandidateNode finalNode, final DataTreeCandidateNode node,
                                     final PathArgument parent) {
        PathArgument identifier;
        try {
            identifier = node.getIdentifier();
        } catch (IllegalStateException e) {
            identifier = null;
        }
        // Check if finalNode has stored any changes for node
        if (finalNode.getNode(identifier).isEmpty()) {
            TerminalDataTreeCandidateNode parentNode = finalNode.getNode(parent)
                    .orElseThrow(() -> new IllegalArgumentException("No node found for " + parent + " identifier"));
            TerminalDataTreeCandidateNode childNode = new TerminalDataTreeCandidateNode(
                    identifier,
                    node.getDataBefore().orElse(null),
                    parentNode);
            parentNode.addChildNode(childNode);
        }

        ModificationType nodeModification = node.getModificationType();
        switch (nodeModification) {
            case UNMODIFIED:
                // If node is unmodified there is no need iterate through its child nodes
                break;
            case WRITE:
            case DELETE:
            case APPEARED:
            case DISAPPEARED:
            case SUBTREE_MODIFIED:
                finalNode.setModification(identifier,
                        compressModifications(finalNode.getModification(identifier), nodeModification,
                                finalNode.getDataAfter(identifier).isEmpty()));
                finalNode.setData(identifier, node.getDataAfter().orElse(null));

                for (DataTreeCandidateNode child : node.getChildNodes()) {
                    compressNode(finalNode, child, identifier);
                }
                break;
            default:
                throw new IllegalStateException("Unsupported modification type " + nodeModification);
        }
    }

    // Removes redundant changes
    private static DataTreeCandidateNode cleanUpTree(final TerminalDataTreeCandidateNode finalNode) {
        return cleanUpTree(finalNode, finalNode);
    }

    // Compare data before and after in order to find modified nodes without actual changes
    private static DataTreeCandidateNode cleanUpTree(final TerminalDataTreeCandidateNode finalNode,
                                                     final TerminalDataTreeCandidateNode node) {
        PathArgument identifier = node.getIdentifier();
        ModificationType nodeModification = node.getModificationType();
        Collection<DataTreeCandidateNode> childNodes = node.getChildNodes();
        for (Iterator<DataTreeCandidateNode> iterator = childNodes.iterator(); iterator.hasNext(); ) {
            cleanUpTree(finalNode, (TerminalDataTreeCandidateNode) iterator.next());
        }
        Optional<NormalizedNode<?, ?>> dataBefore = finalNode.getDataBefore(identifier);

        switch (nodeModification) {
            case UNMODIFIED:
                finalNode.deleteNode(identifier);
                return finalNode;
            case WRITE:
                return finalNode;
            case DELETE:
                if (dataBefore.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            case APPEARED:
                if (dataBefore.isPresent()) {
                    illegalModification(ModificationType.APPEARED, ModificationType.WRITE);
                }
                if (childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            case DISAPPEARED:
                if (dataBefore.isEmpty() || childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            case SUBTREE_MODIFIED:
                if (dataBefore.isEmpty()) {
                    illegalModification(ModificationType.SUBTREE_MODIFIED, ModificationType.DELETE);
                }
                if (childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            default:
                throw new IllegalStateException("Unsupported modification type " + nodeModification);
        }
    }

    private static ModificationType compressModifications(final ModificationType firstModification,
                                                          final ModificationType secondModification,
                                                          final boolean hasNoDataBefore) {
        switch (firstModification) {
            case UNMODIFIED:
                if (hasNoDataBefore) {
                    switch (secondModification) {
                        case UNMODIFIED:
                        case WRITE:
                        case APPEARED:
                            return secondModification;
                        case DELETE:
                            return illegalModification(ModificationType.DELETE, ModificationType.DELETE);
                        case SUBTREE_MODIFIED:
                            return illegalModification(ModificationType.SUBTREE_MODIFIED, ModificationType.DELETE);
                        case DISAPPEARED:
                            return illegalModification(ModificationType.DISAPPEARED, ModificationType.DELETE);
                        default:
                            throw new IllegalStateException("Unsupported modification type " + secondModification);
                    }
                }
                if (secondModification == ModificationType.APPEARED) {
                    return illegalModification(ModificationType.APPEARED, ModificationType.WRITE);
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
                        return illegalModification(ModificationType.APPEARED, firstModification);
                    default:
                        throw new IllegalStateException("Unsupported modification type " + secondModification);
                }
            case DELETE:
                switch (secondModification) {
                    case UNMODIFIED:
                        return ModificationType.DELETE;
                    case WRITE:
                    case APPEARED:
                        return ModificationType.WRITE;
                    case DELETE:
                        return illegalModification(ModificationType.DELETE, firstModification);
                    case DISAPPEARED:
                        return illegalModification(ModificationType.DISAPPEARED, firstModification);
                    case SUBTREE_MODIFIED:
                        return illegalModification(ModificationType.SUBTREE_MODIFIED, firstModification);
                    default:
                        throw new IllegalStateException("Unsupported modification type " + secondModification);
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
                        return illegalModification(ModificationType.APPEARED, firstModification);
                    default:
                        throw new IllegalStateException("Unsupported modification type " + secondModification);
                }
            case DISAPPEARED:
                switch (secondModification) {
                    case UNMODIFIED:
                    case WRITE:
                        return secondModification;
                    case APPEARED:
                        return ModificationType.SUBTREE_MODIFIED;
                    case DELETE:
                        return illegalModification(ModificationType.DELETE, firstModification);
                    case DISAPPEARED:
                        return illegalModification(ModificationType.DISAPPEARED, firstModification);

                    case SUBTREE_MODIFIED:
                        return illegalModification(ModificationType.SUBTREE_MODIFIED, firstModification);
                    default:
                        throw new IllegalStateException("Unsupported modification type " + secondModification);
                }
            case SUBTREE_MODIFIED:
                switch (secondModification) {
                    case UNMODIFIED:
                    case SUBTREE_MODIFIED:
                        return ModificationType.SUBTREE_MODIFIED;
                    case WRITE:
                    case DELETE:
                    case DISAPPEARED:
                        return secondModification;
                    case APPEARED:
                        return illegalModification(ModificationType.APPEARED, firstModification);
                    default:
                        throw new IllegalStateException("Unsupported modification type " + secondModification);
                }
            default:
                throw new IllegalStateException("Unsupported modification type " + secondModification);
        }
    }

    private static ModificationType illegalModification(final ModificationType first, final ModificationType second) {
        throw new IllegalArgumentException(first + " modification event on " + second + " node");
    }

    private static void applyToCursorAwareModification(final CursorAwareDataTreeModification modification,
                                                       final DataTreeCandidate candidate) {
        final YangInstanceIdentifier candidatePath = candidate.getRootPath();
        final YangInstanceIdentifier parent = candidatePath.getParent();
        if (parent == null) {
            try (DataTreeModificationCursor cursor = modification.openCursor()) {
                DataTreeCandidateNodes.applyRootToCursor(cursor, candidate.getRootNode());
            }
        } else {
            try (DataTreeModificationCursor cursor = modification.openCursor(parent).orElseThrow()) {
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
