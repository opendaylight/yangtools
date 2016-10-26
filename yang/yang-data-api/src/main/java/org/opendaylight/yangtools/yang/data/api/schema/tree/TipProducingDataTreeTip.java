/*
 * Copyright (c) 2015 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
/**
 * A {@link DataTreeCandidateTip} which produces {@link DataTreeCandidateTip}s from its
 * {@link #prepare(DataTreeModification)} method.
 */
public interface TipProducingDataTreeTip extends DataTreeTip {
    @Override
    DataTreeCandidateTip prepare(DataTreeModification modification);
    @Override
    DataTreeCandidateTip prepare(DataTreeModification modification, TreeNode lastPrepareRoot);
}
