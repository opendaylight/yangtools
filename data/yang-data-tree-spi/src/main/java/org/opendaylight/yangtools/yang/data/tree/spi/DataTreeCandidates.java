/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
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
                                                                final NormalizedNode node) {
        return new DefaultDataTreeCandidate(rootPath, new NormalizedNodeDataTreeCandidateNode(node));
    }

    public static void applyToCursor(final DataTreeModificationCursor cursor, final DataTreeCandidate candidate) {
        DataTreeCandidateNodes.applyToCursor(cursor, candidate.getRootNode());
    }

    public static void applyToModification(final DataTreeModification modification, final DataTreeCandidate candidate) {
//        if (modification instanceof CursorAwareDataTreeModification) {
            applyToCursorAwareModification(modification, candidate);
//            return;
//        }

//        final DataTreeCandidateNode node = candidate.getRootNode();
//        final YangInstanceIdentifier path = candidate.getRootPath();
//        switch (node.modificationType()) {
//            case DELETE:
//                modification.delete(path);
//                LOG.debug("Modification {} deleted path {}", modification, path);
//                break;
//            case SUBTREE_MODIFIED:
//                LOG.debug("Modification {} modified path {}", modification, path);
//
//                NodeIterator iterator = new NodeIterator(null, path, node.childNodes().iterator());
//                do {
//                    iterator = iterator.next(modification);
//                } while (iterator != null);
//                break;
//            case UNMODIFIED:
//                LOG.debug("Modification {} unmodified path {}", modification, path);
//                // No-op
//                break;
//            case WRITE:
//                modification.write(path, verifyNotNull(node.dataAfter()));
//                LOG.debug("Modification {} written path {}", modification, path);
//                break;
//            default:
//                throw new IllegalArgumentException("Unsupported modification " + node.modificationType());
//        }
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
        final var last = input.get(input.size() - 1);
        final var nodeModification = last.modificationType();
        final var dataBefore = first.dataBefore();
        final var dataAfter = last.dataAfter();
        switch (nodeModification) {
            case DELETE:
                ModificationType previous = first.modificationType();
                // Check if node had data before
                if (previous == ModificationType.DELETE || previous == ModificationType.DISAPPEARED
                    || previous == ModificationType.UNMODIFIED && dataBefore == null) {
                    illegalModification(ModificationType.DELETE, ModificationType.DELETE);
                }
                if (dataBefore == null) {
                    return new TerminalDataTreeCandidateNode(null, ModificationType.UNMODIFIED, null, null);
                }
                return new TerminalDataTreeCandidateNode(null, nodeModification, verifyNotNull(dataBefore), null);
            case WRITE:
                return new TerminalDataTreeCandidateNode(null, nodeModification, dataBefore, verifyNotNull(dataAfter));
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
        TerminalDataTreeCandidateNode finalNode = new TerminalDataTreeCandidateNode(null, first.dataBefore());
        input.forEach(node -> compressNode(finalNode, node, null));
        finalNode.setAfter(input.get(input.size() - 1).dataAfter());
        return cleanUpTree(finalNode);
    }

    private static void compressNode(final TerminalDataTreeCandidateNode finalNode, final DataTreeCandidateNode node,
                                     final PathArgument parent) {
        PathArgument identifier;
        try {
            identifier = node.name();
        } catch (IllegalStateException e) {
            identifier = null;
        }
        // Check if finalNode has stored any changes for node
        if (finalNode.getNode(identifier).isEmpty()) {
            TerminalDataTreeCandidateNode parentNode = finalNode.getNode(parent)
                    .orElseThrow(() -> new IllegalArgumentException("No node found for " + parent + " identifier"));
            TerminalDataTreeCandidateNode childNode = new TerminalDataTreeCandidateNode(
                    identifier,
                    node.dataBefore(),
                    parentNode);
            parentNode.addChildNode(childNode);
        }

        ModificationType nodeModification = node.modificationType();
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
                                finalNode.dataAfter(identifier) == null));
                finalNode.setData(identifier, node.dataAfter());

                for (DataTreeCandidateNode child : node.childNodes()) {
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
        final var identifier = node.name();
        final var nodeModification = node.modificationType();
        final var childNodes = node.childNodes();
        for (var childNode : childNodes) {
            cleanUpTree(finalNode, (TerminalDataTreeCandidateNode) childNode);
        }
        final var dataBefore = finalNode.dataBefore(identifier);

        switch (nodeModification) {
            case UNMODIFIED:
                finalNode.deleteNode(identifier);
                return finalNode;
            case WRITE:
                return finalNode;
            case DELETE:
                if (dataBefore == null) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            case APPEARED:
                if (dataBefore != null) {
                    illegalModification(ModificationType.APPEARED, ModificationType.WRITE);
                }
                if (childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            case DISAPPEARED:
                if (dataBefore == null || childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
                return finalNode;
            case SUBTREE_MODIFIED:
                if (dataBefore == null) {
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
                    return switch (secondModification) {
                        case UNMODIFIED, WRITE, APPEARED -> secondModification;
                        case DELETE -> illegalModification(ModificationType.DELETE, ModificationType.DELETE);
                        case SUBTREE_MODIFIED ->
                            illegalModification(ModificationType.SUBTREE_MODIFIED, ModificationType.DELETE);
                        case DISAPPEARED -> illegalModification(ModificationType.DISAPPEARED, ModificationType.DELETE);
                        default ->
                            throw new IllegalStateException("Unsupported modification type " + secondModification);
                    };
                }
                if (secondModification == ModificationType.APPEARED) {
                    return illegalModification(ModificationType.APPEARED, ModificationType.WRITE);
                }
                return secondModification;
            case WRITE:
                return switch (secondModification) {
                    case UNMODIFIED, WRITE, SUBTREE_MODIFIED -> ModificationType.WRITE;
                    case DELETE -> ModificationType.DELETE;
                    case DISAPPEARED -> ModificationType.DISAPPEARED;
                    case APPEARED -> illegalModification(ModificationType.APPEARED, firstModification);
                    default -> throw new IllegalStateException("Unsupported modification type " + secondModification);
                };
            case DELETE:
                return switch (secondModification) {
                    case UNMODIFIED -> ModificationType.DELETE;
                    case WRITE, APPEARED -> ModificationType.WRITE;
                    case DELETE -> illegalModification(ModificationType.DELETE, firstModification);
                    case DISAPPEARED -> illegalModification(ModificationType.DISAPPEARED, firstModification);
                    case SUBTREE_MODIFIED -> illegalModification(ModificationType.SUBTREE_MODIFIED, firstModification);
                    default -> throw new IllegalStateException("Unsupported modification type " + secondModification);
                };
            case APPEARED:
                return switch (secondModification) {
                    case UNMODIFIED, SUBTREE_MODIFIED -> ModificationType.APPEARED;
                    case DELETE, DISAPPEARED -> ModificationType.UNMODIFIED;
                    case WRITE -> ModificationType.WRITE;
                    case APPEARED -> illegalModification(ModificationType.APPEARED, firstModification);
                    default -> throw new IllegalStateException("Unsupported modification type " + secondModification);
                };
            case DISAPPEARED:
                return switch (secondModification) {
                    case UNMODIFIED, WRITE -> secondModification;
                    case APPEARED -> ModificationType.SUBTREE_MODIFIED;
                    case DELETE -> illegalModification(ModificationType.DELETE, firstModification);
                    case DISAPPEARED -> illegalModification(ModificationType.DISAPPEARED, firstModification);
                    case SUBTREE_MODIFIED -> illegalModification(ModificationType.SUBTREE_MODIFIED, firstModification);
                    default -> throw new IllegalStateException("Unsupported modification type " + secondModification);
                };
            case SUBTREE_MODIFIED:
                return switch (secondModification) {
                    case UNMODIFIED, SUBTREE_MODIFIED -> ModificationType.SUBTREE_MODIFIED;
                    case WRITE, DELETE, DISAPPEARED -> secondModification;
                    case APPEARED -> illegalModification(ModificationType.APPEARED, firstModification);
                    default -> throw new IllegalStateException("Unsupported modification type " + secondModification);
                };
            default:
                throw new IllegalStateException("Unsupported modification type " + secondModification);
        }
    }

    private static ModificationType illegalModification(final ModificationType first, final ModificationType second) {
        throw new IllegalArgumentException(first + " modification event on " + second + " node");
    }

    private static void applyToCursorAwareModification(final DataTreeModification modification,
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
                final YangInstanceIdentifier child = path.node(node.name());

                switch (node.modificationType()) {
                    case DELETE:
                        modification.delete(child);
                        LOG.debug("Modification {} deleted path {}", modification, child);
                        break;
                    case APPEARED:
                    case DISAPPEARED:
                    case SUBTREE_MODIFIED:
                        LOG.debug("Modification {} modified path {}", modification, child);
                        return new NodeIterator(this, child, node.childNodes().iterator());
                    case UNMODIFIED:
                        LOG.debug("Modification {} unmodified path {}", modification, child);
                        // No-op
                        break;
                    case WRITE:
                        modification.write(child, verifyNotNull(node.dataAfter()));
                        LOG.debug("Modification {} written path {}", modification, child);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported modification " + node.modificationType());
                }
            }
            return parent;
        }
    }
}
