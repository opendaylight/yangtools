/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

/**
 * A {@link DataTreeCandidate} which is also a {@link DataTreeTip}. This indicates that
 * a {@link DataTreeModification} can be prepared for commit in sequence after this candidate.
 * DataTree implementations are encouraged to provide this instead of the basic
 * {@link DataTreeCandidate}, as it allows users to amortize latency in systems where
 * a candidate commit needs to be coordinated across distributed parties.
 */
@Beta
public interface DataTreeCandidateTip extends DataTreeCandidate, TipProducingDataTreeTip {
    TreeNode getTipRoot();
}
