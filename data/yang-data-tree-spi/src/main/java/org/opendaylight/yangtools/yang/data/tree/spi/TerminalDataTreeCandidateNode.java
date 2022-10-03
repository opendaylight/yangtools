/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

// Non-final for mocking
class TerminalDataTreeCandidateNode implements DataTreeCandidateNode {
    private ModificationType modificationType;
    private final PathArgument identifier;
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

    TerminalDataTreeCandidateNode(final PathArgument identifier, final ModificationType modificationType,
                                  final NormalizedNode before, final NormalizedNode after) {
        this.modificationType = modificationType;
        this.identifier = identifier;
        this.before = before;
        this.after = after;
    }

    @Override
    public PathArgument getIdentifier() {
        return identifier;
    }

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        return Collections.unmodifiableCollection(childNodes.values());
    }

    @Override
    public Optional<DataTreeCandidateNode> getModifiedChild(final PathArgument childIdentifier) {
        return Optional.ofNullable(childNodes.get(identifier));
    }

    @Override
    public ModificationType getModificationType() {
        return modificationType;
    }

    @Override
    public Optional<NormalizedNode> getDataAfter() {
        return Optional.ofNullable(after);
    }

    @NonNull Optional<NormalizedNode> getDataAfter(final PathArgument id) {
        return getNode(id).flatMap(TerminalDataTreeCandidateNode::getDataAfter);
    }

    @Override
    public Optional<NormalizedNode> getDataBefore() {
        return Optional.ofNullable(before);
    }

    @NonNull Optional<NormalizedNode> getDataBefore(final PathArgument id) {
        return getNode(id).flatMap(TerminalDataTreeCandidateNode::getDataBefore);
    }

    void setAfter(final NormalizedNode after) {
        this.after = after;
    }

    void addChildNode(final TerminalDataTreeCandidateNode node) {
        childNodes.put(node.getIdentifier(), node);
    }

    void setModification(final PathArgument id, final ModificationType modification) {
        getNode(id)
            .orElseThrow(() -> new IllegalArgumentException("No node with " + id + " id was found"))
            .setModification(modification);
    }

    private void setModification(final ModificationType modification) {
        modificationType = modification;
    }

    ModificationType getModification(final PathArgument id) {
        return getNode(id).map(TerminalDataTreeCandidateNode::getModificationType).orElse(ModificationType.UNMODIFIED);
    }

    void deleteNode(final PathArgument id) {
        if (id != null) {
            getNode(id)
                .orElseThrow(() -> new IllegalArgumentException("No node with " + id + " id was found"))
                .parentNode.deleteChild(id);
        } else {
            modificationType = ModificationType.UNMODIFIED;
        }
    }

    private void deleteChild(final PathArgument id) {
        childNodes.remove(id);
    }

    @NonNull Optional<TerminalDataTreeCandidateNode> getNode(final PathArgument id) {
        if (id == null) {
            return Optional.of(this);
        }
        if (childNodes.isEmpty()) {
            return Optional.empty();
        }
        if (childNodes.containsKey(id)) {
            return Optional.ofNullable(childNodes.get(id));
        }
        return findNode(id);
    }

    void setData(final PathArgument id, final NormalizedNode node) {
        getNode(id).orElseThrow().setAfter(node);
    }

    private @NonNull Optional<TerminalDataTreeCandidateNode> findNode(final PathArgument id) {
        Collection<HashMap<PathArgument, TerminalDataTreeCandidateNode>> nodes = new HashSet<>();
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
        Collection<HashMap<PathArgument, TerminalDataTreeCandidateNode>> nextNodes = new HashSet<>();
        for (HashMap<PathArgument, TerminalDataTreeCandidateNode> map : nodes) {
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
