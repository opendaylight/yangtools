/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Client allocated result holder for {@link ImmutableNormalizedNodeStreamWriter}.
 * which produces instance of NormalizedNode.
 *
 * Client may supply result holder to {@link ImmutableNormalizedNodeStreamWriter}
 * which will be once updated, when result is available.
 *
 * This is intended for using {@link ImmutableNormalizedNodeStreamWriter}
 * without supplying builder, so instantiated writer will select
 * correct builder based on first event and sets resulting
 *  {@link NormalizedNode} when end event is invoked for node.
 *
 */
public class NormalizedNodeResult {

    private NormalizedNode<?,?> result;

    public NormalizedNode<?, ?> getResult() {
        return result;
    }

    void setResult(final NormalizedNode<?, ?> result) {
        Preconditions.checkState(result != null, "Result was already set.");
        this.result = result;
    }

}
