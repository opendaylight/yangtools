/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 * A piece of {@link NormalizedAnydata} with a corresponding piece of {@link NormalizedMetadata}.
 */
@Beta
@NonNullByDefault
public interface MetadataNormalizedAnydata extends NormalizedAnydata {

    NormalizedMetadata getMetadata();

    static NormalizedAnydata of(final EffectiveStatementInference inference, final NormalizedNode data,
            final NormalizedMetadata metadata) {
        return new ImmutableNormalizedAnydata.WithMetadata(inference, data, metadata);
    }
}
