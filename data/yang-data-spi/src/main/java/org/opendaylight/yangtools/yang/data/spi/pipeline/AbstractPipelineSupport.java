/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.pipeline;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.pipeline.Pipeline;
import org.opendaylight.yangtools.yang.data.api.pipeline.PipelineBuilderFactory;
import org.opendaylight.yangtools.yang.data.api.pipeline.PipelineException;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A {@link PipelineBuilderFactory} centered around an {@link EffectiveModelContext}.
 */
@NonNullByDefault
public abstract class AbstractPipelineSupport implements PipelineBuilderFactory {
    @Override
    public final Pipeline.Builder newPipelineBuilder(final RootResolver rootResolver) throws PipelineException {


        return newPipelineBuilder(rootResolver.resolveRoot(modelContext()));
    }

    protected abstract EffectiveModelContext modelContext();

    protected abstract Pipeline.Builder newPipelineBuilder(final SchemaInferenceStack stack) throws PipelineException;

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected abstract ToStringHelper addToStringAttributes(ToStringHelper helper);
}
