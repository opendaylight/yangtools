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
 * @deprecated Use {@link NormalizedNodeUtils} instead.
 */
@Deprecated
public interface NodeModificationBuilder {

    abstract Node<?> getMutableEquivalent(Node<?> originalNode);

    abstract CompositeNode buildDiffTree();

    abstract void mergeNode(MutableCompositeNode alteredNode);

    abstract void removeNode(MutableCompositeNode deadNode);

    abstract void removeNode(MutableSimpleNode<?> deadNode);

    abstract void deleteNode(MutableSimpleNode<?> deadNode);

    abstract void deleteNode(MutableCompositeNode deadNode);

    abstract void replaceNode(MutableCompositeNode replacementNode);

    abstract void replaceNode(MutableSimpleNode<?> replacementNode);

    abstract void addNode(MutableCompositeNode newNode);

    abstract void addNode(MutableSimpleNode<?> newNode);

}
