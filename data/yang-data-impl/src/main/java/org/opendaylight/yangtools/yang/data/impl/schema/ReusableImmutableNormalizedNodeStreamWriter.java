/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationResult;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ReusableStreamReceiver;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;

/**
 * A reusable variant of {@link ImmutableNormalizedNodeStreamWriter}. It can be reset into its base state and used for
 * multiple streaming sessions.
 */
@Beta
public final class ReusableImmutableNormalizedNodeStreamWriter extends ImmutableNormalizedNodeStreamWriter
        implements ReusableStreamReceiver {
    private final NormalizationResultBuilder builder;

    private final ImmutableLeafSetEntryNodeBuilder<?> leafsetEntryBuilder = new ImmutableLeafSetEntryNodeBuilder<>();
    private final ImmutableLeafNodeBuilder<?> leafNodeBuilder = new ImmutableLeafNodeBuilder<>();

    private ReusableImmutableNormalizedNodeStreamWriter(final NormalizationResultBuilder builder) {
        super(builder);
        this.builder = requireNonNull(builder);
    }

    public static @NonNull ReusableImmutableNormalizedNodeStreamWriter create() {
        return new ReusableImmutableNormalizedNodeStreamWriter(new NormalizationResultBuilder());
    }

    @Override
    public void reset() {
        builder.reset();
        reset(builder);
    }

    @Override
    public NormalizationResult<?> result() {
        return builder.result();
    }

    @Override
    @SuppressWarnings("unchecked")
    <T> ImmutableLeafNodeBuilder<T> leafNodeBuilder() {
        return (ImmutableLeafNodeBuilder<T>) leafNodeBuilder;
    }

    @Override
    @SuppressWarnings("unchecked")
    <T> ImmutableLeafSetEntryNodeBuilder<T> leafsetEntryNodeBuilder() {
        return (ImmutableLeafSetEntryNodeBuilder<T>) leafsetEntryBuilder;
    }
}
