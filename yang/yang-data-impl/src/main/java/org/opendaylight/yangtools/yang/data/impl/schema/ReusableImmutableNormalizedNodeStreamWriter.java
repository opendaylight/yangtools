/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A reusable variant of {@link ImmutableNormalizedNodeStreamWriter}. It can be reset into its base state and used for
 * multiple streaming sessions.
 */
@Beta
public final class ReusableImmutableNormalizedNodeStreamWriter extends ImmutableNormalizedNodeStreamWriter {
    private NormalizedNodeResult result;

    private ReusableImmutableNormalizedNodeStreamWriter(final NormalizedNodeResult result) {
        super(result);
    }

    public static @NonNull ReusableImmutableNormalizedNodeStreamWriter create() {
        return new ReusableImmutableNormalizedNodeStreamWriter(new NormalizedNodeResult());
    }

    public void reset() {
        result = new NormalizedNodeResult();
        reset(result);
    }

    public NormalizedNode<?, ?> getResult() {
        return result.getResult();
    }
}
