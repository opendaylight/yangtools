/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Result holder for {@link NormalizedNodeStreamWriter} implementations,
 * which produces instance of NormalizedNode.
 *
 */
public class NormalizedNodeResult {

    private NormalizedNode<?,?> result;

    public NormalizedNode<?, ?> getResult() {
        return result;
    }

    public void setResult(final NormalizedNode<?, ?> result) {
        Preconditions.checkState(result != null, "Result was already set.");
        this.result = result;
    }

}
