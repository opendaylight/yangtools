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
 * An {@link AnydataNode#bodyObjectModel() anydata value object model} which can be normalized to
 * {@link NormalizedAnydata} when provided with proper context.
 */
@Beta
@NonNullByDefault
public interface NormalizableAnydata {
    /**
     * Attempt to interpret this anydata content in the context of specified {@link EffectiveStatementInference}.
     *
     * @param inference effective statement inference
     * @return Normalized anydata instance
     * @throws NullPointerException if {@code inference} is null
     * @throws AnydataNormalizationException if this data cannot be interpreted in the requested context
     */
    NormalizedAnydata normalizeTo(EffectiveStatementInference inference) throws AnydataNormalizationException;
}
