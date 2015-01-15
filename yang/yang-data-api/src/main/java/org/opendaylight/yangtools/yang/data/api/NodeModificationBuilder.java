/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;


/**
 * @author michal.rehak
 *
 * @deprecated Use {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes} instead.
 */
@Deprecated
public interface NodeModificationBuilder {

    Node<?> getMutableEquivalent(Node<?> originalNode);

    CompositeNode buildDiffTree();

    void mergeNode(MutableCompositeNode alteredNode);

    void removeNode(MutableCompositeNode deadNode);

    void removeNode(MutableSimpleNode<?> deadNode);

    void deleteNode(MutableSimpleNode<?> deadNode);

    void deleteNode(MutableCompositeNode deadNode);

    void replaceNode(MutableCompositeNode replacementNode);

    void replaceNode(MutableSimpleNode<?> replacementNode);

    void addNode(MutableCompositeNode newNode);

    void addNode(MutableSimpleNode<?> newNode);

}
