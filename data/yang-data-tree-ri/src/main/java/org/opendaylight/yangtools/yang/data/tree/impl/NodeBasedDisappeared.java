/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Disappeared;

@NonNullByDefault
record NodeBasedDisappeared(AbstractModifiedNodeBasedCandidateNode node) implements Disappeared {
    NodeBasedDisappeared {
        requireNonNull(node);
   }

   @Override
   public NormalizedNode dataBefore() {
       return node.dataBefore();
   }

   @Override
   public Collection<CandidateNode> children() {
       return node.candidateChildren();
   }

   @Override
   public @Nullable CandidateNode modifiedChild(final PathArgument childName) {
       return node.candidateModifiedChild(childName);
   }
}
