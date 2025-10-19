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
import org.opendaylight.yangtools.yang.data.tree.api.CursorAwareDataTreeModification;
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
        return new DefaultDataTreeCandidate(rootPath, CreatedDataTreeCandidateNode.of(node));
    }

    public static void applyToCursor(final DataTreeModificationCursor cursor, final DataTreeCandidate candidate) {
        DataTreeCandidateNodes.applyToCursor(cursor, candidate.getRootNode());
    }

    public static void applyToModification(final DataTreeModification modification, final DataTreeCandidate candidate) {
        if (modification instanceof CursorAwareDataTreeModification cursorAware) {
            applyToCursorAwareModification(cursorAware, candidate);
            return;
        }

        final var node = candidate.getRootNode();
        final var path = candidate.getRootPath();
        final var type = node.modificationType();
        switch (type) {
            case DELETE -> {
                modification.delete(path);
                LOG.debug("Modification {} deleted path {}", modification, path);
            }
            case SUBTREE_MODIFIED -> {
                LOG.debug("Modification {} modified path {}", modification, path);

                var iterator = new NodeIterator(null, path, node.childNodes().iterator());
                do {
                    iterator = iterator.next(modification);
                } while (iterator != null);
            }
            case UNMODIFIED -> {
                LOG.debug("Modification {} unmodified path {}", modification, path);
                // No-op
            }
            case WRITE -> {
                modification.write(path, verifyNotNull(node.dataAfter()));
                LOG.debug("Modification {} written path {}", modification, path);
            }
            default -> throw new IllegalArgumentException("Unsupported modification " + type);
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
    public static @NonNull DataTreeCandidate aggregate(final @NonNull List<? extends DataTreeCandidate> candidates) {
        final var it = candidates.iterator();
        checkArgument(it.hasNext(), "Input must not be empty");
        final var first = requireNonNull(it.next(), "Input must not contain null entries");
        if (!it.hasNext()) {
            // Short-circuit
            return first;
        }
        final var rootPath = first.getRootPath();
        final var roots = new ArrayList<DataTreeCandidateNode>(candidates.size());
        roots.add(first.getRootNode());
        it.forEachRemaining(candidate -> {
            final var root = candidate.getRootPath();
            checkArgument(rootPath.equals(root), "Expecting root path %s, encountered %s", rootPath, root);
            roots.add(candidate.getRootNode());
        });
        return DataTreeCandidates.newDataTreeCandidate(rootPath, fastCompressNode(roots.getFirst(), roots));
    }

    private static DataTreeCandidateNode fastCompressNode(final DataTreeCandidateNode first,
            final List<DataTreeCandidateNode> input) {
        final var last = input.getLast();
        final var dataBefore = first.dataBefore();
        final var dataAfter = last.dataAfter();
        final var type = last.modificationType();

        return switch (type) {
            case DELETE -> {
                final var previous = first.modificationType();
                // Check if node had data before
                if (previous == ModificationType.DELETE || previous == ModificationType.DISAPPEARED
                    || previous == ModificationType.UNMODIFIED && dataBefore == null) {
                    throw illegalModification(ModificationType.DELETE, ModificationType.DELETE);
                }

                yield dataBefore != null ? new TerminalDataTreeCandidateNode(null, type, dataBefore, null)
                    : new TerminalDataTreeCandidateNode(null, ModificationType.UNMODIFIED, null, null);
            }
            case WRITE -> new TerminalDataTreeCandidateNode(null, type, dataBefore, verifyNotNull(dataAfter));
            case APPEARED, DISAPPEARED, SUBTREE_MODIFIED, UNMODIFIED ->
                // No luck, we need to iterate
                slowCompressNodes(first, input);
        };
    }

    private static DataTreeCandidateNode slowCompressNodes(final DataTreeCandidateNode first,
            final List<DataTreeCandidateNode> input) {
        // finalNode contains summarized changes
        final var finalNode = new TerminalDataTreeCandidateNode(null, first.dataBefore());
        input.forEach(node -> compressNode(finalNode, node, null));
        finalNode.setAfter(input.getLast().dataAfter());
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
            final var parentNode = finalNode.getNode(parent)
                .orElseThrow(() -> new IllegalArgumentException("No node found for " + parent + " identifier"));
            parentNode.addChildNode(new TerminalDataTreeCandidateNode(identifier, node.dataBefore(), parentNode));
        }

        final var nodeModification = node.modificationType();
        switch (nodeModification) {
            case UNMODIFIED -> {
                // If node is unmodified there is no need iterate through its child nodes
            }
            case APPEARED, DELETE, DISAPPEARED, SUBTREE_MODIFIED, WRITE -> {
                finalNode.setModification(identifier, compressModifications(finalNode.getModification(identifier),
                    nodeModification, finalNode.dataAfter(identifier) == null));
                finalNode.setData(identifier, node.dataAfter());

                for (var child : node.childNodes()) {
                    compressNode(finalNode, child, identifier);
                }
            }
            default -> throw new IllegalStateException("Unsupported modification type " + nodeModification);
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
        final var childNodes = node.childNodes();
        for (var childNode : childNodes) {
            cleanUpTree(finalNode, (TerminalDataTreeCandidateNode) childNode);
        }
        final var dataBefore = finalNode.dataBefore(identifier);

        switch (node.modificationType()) {
            // XXX: Forces JEP-441 exhaustiveness. Revisit when a better option comes along.
            case null -> throw new NullPointerException("null modificationType");
            case UNMODIFIED -> finalNode.deleteNode(identifier);
            case WRITE -> {
                // No-op
            }
            case DELETE -> {
                if (dataBefore == null) {
                    finalNode.deleteNode(identifier);
                }
            }
            case APPEARED -> {
                if (dataBefore != null) {
                    throw illegalModification(ModificationType.APPEARED, ModificationType.WRITE);
                }
                if (childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
            }
            case DISAPPEARED -> {
                if (dataBefore == null || childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
            }
            case SUBTREE_MODIFIED -> {
                if (dataBefore == null) {
                    throw illegalModification(ModificationType.SUBTREE_MODIFIED, ModificationType.DELETE);
                }
                if (childNodes.isEmpty()) {
                    finalNode.deleteNode(identifier);
                }
            }
        }

        return finalNode;
    }

    private static ModificationType compressModifications(final ModificationType first, final ModificationType second,
            final boolean hasNoDataBefore) {
        return switch (first) {
            case UNMODIFIED -> {
                if (hasNoDataBefore) {
                    yield switch (second) {
                            case UNMODIFIED, WRITE, APPEARED -> second;
                            case DELETE, DISAPPEARED, SUBTREE_MODIFIED ->
                                throw illegalModification(second, ModificationType.DELETE);
                        };
                }
                if (second == ModificationType.APPEARED) {
                    throw illegalModification(ModificationType.APPEARED, ModificationType.WRITE);
                }
                yield second;
            }
            case WRITE ->
                switch (second) {
                    case UNMODIFIED, WRITE, SUBTREE_MODIFIED -> ModificationType.WRITE;
                    case DELETE -> ModificationType.DELETE;
                    case DISAPPEARED -> ModificationType.DISAPPEARED;
                    case APPEARED -> throw illegalModification(ModificationType.APPEARED, first);
                };
            case DELETE ->
                switch (second) {
                    case UNMODIFIED -> ModificationType.DELETE;
                    case WRITE, APPEARED -> ModificationType.WRITE;
                    case DELETE, DISAPPEARED, SUBTREE_MODIFIED -> throw illegalModification(second, first);
                };
            case APPEARED ->
                switch (second) {
                    case UNMODIFIED, SUBTREE_MODIFIED -> ModificationType.APPEARED;
                    case DELETE, DISAPPEARED -> ModificationType.UNMODIFIED;
                    case WRITE -> ModificationType.WRITE;
                    case APPEARED -> throw illegalModification(ModificationType.APPEARED, first);
                };
            case DISAPPEARED ->
                switch (second) {
                    case UNMODIFIED, WRITE -> second;
                    case APPEARED -> ModificationType.SUBTREE_MODIFIED;
                    case DELETE, DISAPPEARED, SUBTREE_MODIFIED -> throw illegalModification(second, first);
                };
            case SUBTREE_MODIFIED ->
                switch (second) {
                    case UNMODIFIED, SUBTREE_MODIFIED -> ModificationType.SUBTREE_MODIFIED;
                    case WRITE, DELETE, DISAPPEARED -> second;
                    case APPEARED -> throw illegalModification(ModificationType.APPEARED, first);
                };
        };
    }

    private static IllegalArgumentException illegalModification(final ModificationType first,
            final ModificationType second) {
        return new IllegalArgumentException(first + " modification event on " + second + " node");
    }

    private static void applyToCursorAwareModification(final CursorAwareDataTreeModification modification,
                                                       final DataTreeCandidate candidate) {
        final var candidatePath = candidate.getRootPath();
        final var parent = candidatePath.getParent();
        if (parent == null) {
            try (var cursor = modification.openCursor()) {
                DataTreeCandidateNodes.applyRootToCursor(cursor, candidate.getRootNode());
            }
        } else {
            try (var cursor = modification.openCursor(parent).orElseThrow()) {
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
                final var node = iterator.next();
                final var child = path.node(node.name());
                switch (node.modificationType()) {
                    // XXX: Forces JEP-441 exhaustiveness. Revisit when a better option comes along.
                    case null -> throw new NullPointerException("null modificationType");
                    case DELETE -> {
                        modification.delete(child);
                        LOG.debug("Modification {} deleted path {}", modification, child);
                    }
                    case APPEARED, DISAPPEARED, SUBTREE_MODIFIED -> {
                        LOG.debug("Modification {} modified path {}", modification, child);
                        return new NodeIterator(this, child, node.childNodes().iterator());
                    }
                    case UNMODIFIED -> {
                        // No-op
                        LOG.debug("Modification {} unmodified path {}", modification, child);
                    }
                    case WRITE -> {
                        modification.write(child, verifyNotNull(node.dataAfter()));
                        LOG.debug("Modification {} written path {}", modification, child);
                    }
                }
            }
            return parent;
        }
    }
}
