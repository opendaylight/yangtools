/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationResult;

@SuppressWarnings("rawtypes")
final class NormalizationResultBuilder implements NormalizedNodeContainerBuilder {
    private final @NonNull NormalizationResultHolder holder;

    NormalizationResultBuilder() {
        holder = new NormalizationResultHolder();
    }

    NormalizationResultBuilder(final NormalizationResultHolder holder) {
        this.holder = requireNonNull(holder);
    }

    @NonNull NormalizationResult result() {
        return holder.getResult();
    }

    void reset() {
        holder.reset();
    }

    @Override
    public NormalizedNodeBuilder withValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NormalizedNodeContainerBuilder withValue(final Collection value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NormalizedNode build() {
        throw new IllegalStateException("Can not close NormalizedNodeResult");
    }

    @Override
    public NormalizedNodeContainerBuilder withNodeIdentifier(final PathArgument nodeIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NormalizedNodeContainerBuilder addChild(final NormalizedNode child) {
        holder.setData(child);
        return this;
    }

    @Override
    public NormalizedNodeContainerBuilder removeChild(final PathArgument key) {
        throw new UnsupportedOperationException();
    }
}