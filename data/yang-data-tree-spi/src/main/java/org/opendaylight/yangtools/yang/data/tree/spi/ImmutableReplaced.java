/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Replaced;

@NonNullByDefault
public record ImmutableReplaced(NormalizedNode dataBefore, NormalizedNode dataAfter) implements Replaced {
    public ImmutableReplaced {
        requireNonNull(dataBefore);
        requireNonNull(dataAfter);
    }

    @Override
    public NormalizedNode dataBefore() {
        return dataBefore;
    }

    @Override
    public NormalizedNode dataAfter() {
        return dataAfter;
    }
}
