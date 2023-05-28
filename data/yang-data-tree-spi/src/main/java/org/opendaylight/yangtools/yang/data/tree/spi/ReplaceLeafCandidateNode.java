/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class ReplaceLeafCandidateNode extends AbstractWriteCandidate {
    private final @NonNull NormalizedNode oldData;

    ReplaceLeafCandidateNode(final NormalizedNode oldData, final NormalizedNode newData) {
        super(newData);
        this.oldData = requireNonNull(oldData);
    }

    @Override
    public NormalizedNode dataBefore() {
        return oldData;
    }
}