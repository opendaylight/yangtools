/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

// Non-final for mocking
class TerminalDataTreeCandidateNode implements DataTreeCandidateNode {
    private ModificationType modificationType;
    private final @NonNull PathArgument name;
    private final NormalizedNode before;
    private NormalizedNode after;
    private final HashMap<PathArgument, TerminalDataTreeCandidateNode> childNodes = new HashMap<>();
    private TerminalDataTreeCandidateNode parentNode;

    TerminalDataTreeCandidateNode(final PathArgument identifier, final NormalizedNode data,
                                  final TerminalDataTreeCandidateNode parentNode) {
        this(identifier, data);
        this.parentNode = requireNonNull(parentNode);
    }

    TerminalDataTreeCandidateNode(final PathArgument identifier, final NormalizedNode data) {
        this(identifier, ModificationType.UNMODIFIED, data, data);
    }

    TerminalDataTreeCandidateNode(final PathArgument name, final ModificationType modificationType,
                                  final NormalizedNode before, final NormalizedNode after) {
        this.name = requireNonNull(name);
        this.modificationType = requireNonNull(modificationType);
        this.before = before;
        this.after = after;
    }

    @Override
    public PathArgument name() {
        return name;
    }

    @Override
    public Collection<DataTreeCandidateNode> childNodes() {
        return Collections.unmodifiableCollection(childNodes.values());
    }

    @Override
    public DataTreeCandidateNode modifiedChild(final PathArgument childIdentifier) {
        return childNodes.get(name);
    }

    @Override
    public ModificationType modificationType() {
        return requireNonNull(modificationType);
    }

    @Override
    public NormalizedNode dataAfter() {
        return after;
    }

    @Nullable NormalizedNode dataAfter(final PathArgument id) {
        final var child = node(id);
        return child != null ? child.dataAfter() : null;
    }

    @Override
    public NormalizedNode dataBefore() {
        return before;
    }

    @Nullable NormalizedNode dataBefore(final PathArgument id) {
        final var child = node(id);
        return child != null ? child.dataBefore() : null;
    }

    void setAfter(final NormalizedNode after) {
        this.after = after;
    }

    void addChildNode(final TerminalDataTreeCandidateNode node) {
        childNodes.put(node.name(), node);
    }

    void setModification(final PathArgument id, final ModificationType modification) {
        getNode(id)
            .orElseThrow(() -> new IllegalArgumentException("No node with " + id + " id was found"))
            .setModification(modification);
    }

    private void setModification(final ModificationType modification) {
        modificationType = requireNonNull(modification);
    }

    ModificationType getModification(final PathArgument id) {
        return getNode(id).map(TerminalDataTreeCandidateNode::modificationType).orElse(ModificationType.UNMODIFIED);
    }

    void deleteNode(final PathArgument id) {
        if (id != null) {
            getNode(id).orElseThrow(() -> new IllegalArgumentException("No node with " + id + " id was found"))
                .parentNode.deleteChild(id);
        } else {
            modificationType = ModificationType.UNMODIFIED;
        }
    }

    private void deleteChild(final PathArgument id) {
        childNodes.remove(id);
    }

    final @Nullable TerminalDataTreeCandidateNode node(final PathArgument id) {
        if (id == null) {
            return this;
        }
        if (childNodes.isEmpty()) {
            return null;
        }
        final var childNode = childNodes.get(id);
        if (childNode != null) {
            return childNode;
        }
        return findNode(id).orElse(null);
    }

    final @NonNull Optional<TerminalDataTreeCandidateNode> getNode(final PathArgument id) {
        return Optional.ofNullable(node(id));
    }

    void setData(final PathArgument id, final NormalizedNode node) {
        verifyNotNull(node(id)).setAfter(node);
    }

    private @NonNull Optional<TerminalDataTreeCandidateNode> findNode(final PathArgument id) {
        final var nodes = new HashSet<HashMap<PathArgument, TerminalDataTreeCandidateNode>>();
        childNodes.forEach((childIdentifier, childNode) -> {
            nodes.add(childNode.childNodes);
        });
        return findNode(nodes, id);
    }

    private @NonNull Optional<TerminalDataTreeCandidateNode> findNode(
            final Collection<HashMap<PathArgument, TerminalDataTreeCandidateNode>> nodes, final PathArgument id) {
        if (nodes.isEmpty()) {
            return Optional.empty();
        }
        final var nextNodes = new HashSet<HashMap<PathArgument, TerminalDataTreeCandidateNode>>();
        for (var map : nodes) {
            if (map.containsKey(id)) {
                return Optional.ofNullable(map.get(id));
            }
            map.forEach((childIdentifier, childNode) -> {
                nextNodes.add(childNode.childNodes);
            });
        }
        return findNode(nextNodes, id);
    }
}
