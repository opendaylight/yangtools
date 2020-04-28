/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class TerminalDataTreeCandidateNode implements DataTreeCandidateNode {
    private ModificationType modificationType;
    private final PathArgument identifier;
    private final NormalizedNode<?, ?> before;
    private NormalizedNode<?, ?> after;
    private final HashMap<PathArgument, TerminalDataTreeCandidateNode> childNodes = new HashMap<>();
    private TerminalDataTreeCandidateNode parentNode;

    TerminalDataTreeCandidateNode(PathArgument identifier, NormalizedNode<?, ?> data,
                                  TerminalDataTreeCandidateNode parentNode) {
        this(identifier, data);
        this.parentNode = requireNonNull(parentNode);
    }

    TerminalDataTreeCandidateNode(PathArgument identifier, NormalizedNode<?, ?> data) {
        this(identifier, ModificationType.UNMODIFIED, data, data);
    }

    TerminalDataTreeCandidateNode(PathArgument identifier, ModificationType modificationType,
                                  NormalizedNode<?, ?> before, NormalizedNode<?, ?> after) {
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
    public Optional<DataTreeCandidateNode> getModifiedChild(
            PathArgument childIdentifier) {
        return Optional.ofNullable(childNodes.get(identifier));
    }

    @Override
    public ModificationType getModificationType() {
        return modificationType;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        return Optional.ofNullable(after);
    }

    @NonNull Optional<NormalizedNode<?, ?>> getDataAfter(PathArgument id) {
        return getNode(id).flatMap(TerminalDataTreeCandidateNode::getDataAfter);
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataBefore() {
        return Optional.ofNullable(before);
    }

    @NonNull Optional<NormalizedNode<?, ?>> getDataBefore(PathArgument id) {
        Optional<TerminalDataTreeCandidateNode> node = getNode(id);
        if (node.isPresent()) {
            return node.get().getDataBefore();
        }
        return Optional.empty();
    }

    void setAfter(NormalizedNode<?, ?> after) {
        this.after = after;
    }

    void addChildNode(TerminalDataTreeCandidateNode node) {
        childNodes.put(node.getIdentifier(), node);
    }

    void setModification(PathArgument id, ModificationType modification) {
        Optional<TerminalDataTreeCandidateNode> node = getNode(id);
        if (node.isEmpty()) {
            throw new IllegalArgumentException("No node with " + id + " id was found");
        }
        node.get().setModification(modification);
    }

    private void setModification(ModificationType modification) {
        this.modificationType = modification;
    }

    ModificationType getModification(PathArgument id) {
        Optional<TerminalDataTreeCandidateNode> node = getNode(id);
        return (node.isEmpty() ? ModificationType.UNMODIFIED : node.get().getModificationType());
    }

    void deleteNode(PathArgument id) {
        if (id == null) {
            modificationType = ModificationType.UNMODIFIED;
            return;
        }
        Optional<TerminalDataTreeCandidateNode> node = getNode(id);
        if (node.isEmpty()) {
            throw new IllegalArgumentException("No node with " + id + " id was found");
        }
        node.get().parentNode.deleteChild(id);
    }

    private void deleteChild(PathArgument id) {
        childNodes.remove(id);
    }

    @NonNull Optional<TerminalDataTreeCandidateNode> getNode(PathArgument id) {
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

    void setData(PathArgument id, NormalizedNode<?, ?> node) {
        TerminalDataTreeCandidateNode terminalDataTreeCandidateNode = getNode(id).get();
        terminalDataTreeCandidateNode.setAfter(node);
    }

    private @NonNull Optional<TerminalDataTreeCandidateNode> findNode(PathArgument id) {
        Collection<HashMap<PathArgument, TerminalDataTreeCandidateNode>> nodes = new HashSet<>();
        childNodes.forEach((childIdentifier, childNode) -> {
            nodes.add(childNode.childNodes);
        });
        return findNode(nodes, id);
    }

    private @NonNull Optional<TerminalDataTreeCandidateNode> findNode(
            Collection<HashMap<PathArgument, TerminalDataTreeCandidateNode>> nodes,
            PathArgument id) {
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
