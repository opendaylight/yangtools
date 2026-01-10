/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.pipeline;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 *
 */
@Beta
@NonNullByDefault
public interface PipelineBuilderFactory {

    @FunctionalInterface
    interface RootResolver {

        EffectiveStatementInference resolveRoot(EffectiveModelContext modelContext) throws PipelineException;
    }

    Pipeline.Builder newPipelineBuilder(RootResolver rootResolver) throws PipelineException;
}
