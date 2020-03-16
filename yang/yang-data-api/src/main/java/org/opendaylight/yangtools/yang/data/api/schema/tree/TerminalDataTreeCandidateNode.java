/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashSet;

public class TerminalDataTreeCandidateNode implements DataTreeCandidateNode {
    private ModificationType modificationType;
    private YangInstanceIdentifier.PathArgument identifier;
    private NormalizedNode<?,?> before;
    private NormalizedNode<?,?> after;
    private HashMap<YangInstanceIdentifier.PathArgument,TerminalDataTreeCandidateNode> childNodes = new HashMap<>();
    private TerminalDataTreeCandidateNode parentNode;

    public TerminalDataTreeCandidateNode(YangInstanceIdentifier.PathArgument identifier,ModificationType modificationType,
               NormalizedNode<?, ?> before, NormalizedNode<?, ?> after, TerminalDataTreeCandidateNode parentNode) {
        this(identifier,modificationType,before,after);
        this.parentNode = parentNode;
    }

    public TerminalDataTreeCandidateNode( YangInstanceIdentifier.PathArgument identifier,ModificationType modificationType,
                                          NormalizedNode<?, ?> before, NormalizedNode<?, ?> after) {
        this.modificationType = modificationType;
        this.identifier = identifier;
        this.before = before;
        this.after = after;
    }

    @Override
    public YangInstanceIdentifier.@NonNull PathArgument getIdentifier() {
        return identifier;
    }

    @Override
    public @NonNull Collection<DataTreeCandidateNode> getChildNodes() {
        Collection<DataTreeCandidateNode> nodes = new ArrayList<>(childNodes.values());
        return nodes;
    }

    @Override
    public @NonNull Optional<DataTreeCandidateNode> getModifiedChild(YangInstanceIdentifier.PathArgument childIdentifier) {
        return Optional.ofNullable(getNode(identifier).get());
    }

    @Override
    public @NonNull ModificationType getModificationType() {
        return modificationType;
    }

    @Override
    public @NonNull Optional<NormalizedNode<?, ?>> getDataAfter() {
        return Optional.ofNullable(after);
    }

    @Override
    public @NonNull Optional<NormalizedNode<?, ?>> getDataBefore() {
        return Optional.ofNullable(before);
    }

    public void setBefore(Optional<NormalizedNode<?, ?>> before) {
        this.before = before.orElse(null);
    }

    public void setAfter(Optional<NormalizedNode<?, ?>> after) {
        this.after = after.orElse(null);
    }

    public void addChildNode(TerminalDataTreeCandidateNode node) {
        childNodes.put(node.getIdentifier(),node);
    }

    public void setModification(YangInstanceIdentifier.PathArgument identifier, ModificationType modification) {
        TerminalDataTreeCandidateNode node = getNode(identifier).get();
        node.setModification(modification);
    }

    private void setModification(ModificationType modification) {
        this.modificationType = modification;
    }

    public ModificationType getModification(YangInstanceIdentifier.PathArgument identifier) {
        Optional<TerminalDataTreeCandidateNode> node = getNode(identifier);
        return (node.isEmpty() ? ModificationType.UNMODIFIED : node.get().getModificationType());
    }

    public void deleteNode(YangInstanceIdentifier.PathArgument identifier) {
        TerminalDataTreeCandidateNode node = getNode(identifier).get();
        if (node.parentNode==null) {
            modificationType = ModificationType.UNMODIFIED;
            return;
        }
        node.parentNode.deleteChild(identifier);
    }

    private void deleteChild(YangInstanceIdentifier.PathArgument identifier) {
        childNodes.remove(identifier);
    }

    public @NonNull Optional<NormalizedNode<?,?>> getDataBefore(YangInstanceIdentifier.PathArgument identifier) {
        TerminalDataTreeCandidateNode node = getNode(identifier).get();
        return node.getDataBefore();
    }

    public @NonNull Optional<NormalizedNode<?,?>> getDataAfter(YangInstanceIdentifier.PathArgument identifier) {
        TerminalDataTreeCandidateNode node = getNode(identifier).get();
        return node.getDataAfter();
    }

    public @NonNull Optional<TerminalDataTreeCandidateNode> getNode(YangInstanceIdentifier.PathArgument identifier) {
        if (this.identifier.equals(identifier)) {
            return Optional.ofNullable(this);
        }
        if (childNodes.isEmpty()) {
            return Optional.empty();
        }
        if (childNodes.containsKey(identifier)) {
            return Optional.ofNullable(childNodes.get(identifier));
        }
        return findNode(identifier);
    }

    public void setData(YangInstanceIdentifier.PathArgument identifier, Optional<NormalizedNode<?,?>> node) {
        TerminalDataTreeCandidateNode terminalDataTreeCandidateNode = getNode(identifier).get();
        terminalDataTreeCandidateNode.setAfter(node);
    }

    private @NonNull Optional<TerminalDataTreeCandidateNode> findNode(YangInstanceIdentifier.PathArgument identifier) {
        Collection<HashMap<YangInstanceIdentifier.PathArgument,TerminalDataTreeCandidateNode>> nodes = new HashSet<>();
        childNodes.forEach( (childIdentifier,childNode) -> {
            nodes.add(childNode.childNodes);
        });
        return findNode(nodes,identifier);
    }

    private @NonNull Optional<TerminalDataTreeCandidateNode> findNode(
            Collection<HashMap<YangInstanceIdentifier.PathArgument,TerminalDataTreeCandidateNode>> nodes,
            YangInstanceIdentifier.PathArgument identifier) {
        Collection<HashMap<YangInstanceIdentifier.PathArgument,TerminalDataTreeCandidateNode>> nextNodes = new HashSet<>();
        for (HashMap<YangInstanceIdentifier.PathArgument,TerminalDataTreeCandidateNode> map : nodes) {
            if (map.containsKey(identifier)) {
                return Optional.ofNullable(map.get(identifier));
            }
            map.forEach( (childIdentifier,childNode) -> {
                nextNodes.add(childNode.childNodes);
            });
        }
        return findNode(nextNodes,identifier);
    }

}
