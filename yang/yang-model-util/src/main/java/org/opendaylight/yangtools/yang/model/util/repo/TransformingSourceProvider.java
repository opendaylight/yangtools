/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.concepts.Delegator;

/**
 *
 * Utility Source Provider implementation which uses delegate to retrieve
 * sources and transformation function to convert sources to different
 * representation.
 *
 *
 * @param <I>
 *            Representation of schema sources used by delegate
 * @param <O>
 *            Representation of schema sources exposed by this provider
 *
 * @deprecated Replaced with {@link org.opendaylight.yangtools.yang.model.repo.util.SchemaSourceTransformer}
 */
@Deprecated
public final class TransformingSourceProvider<I, O> implements //
        AdvancedSchemaSourceProvider<O>, Delegator<AdvancedSchemaSourceProvider<I>> {

    private final AdvancedSchemaSourceProvider<I> delegate;
    private final SchemaSourceTransformation<I, O> transformation;

    /**
     * Creates instance of transforming schema source provider which uses
     * supplied delegate to retrieve sources and transformation to change
     * sources to different representation.
     *
     * @param delegate
     *            Delegate which provides sources.
     * @param transformation
     *            Transformation function which converts sources
     * @return Instance of TransformingSourceProvider
     * @throws NullPointerException
     *             if any of arguments is null.
     */
    public static <I, O> TransformingSourceProvider<I, O> create(final AdvancedSchemaSourceProvider<I> delegate,
            final SchemaSourceTransformation<I, O> transformation) {
        return new TransformingSourceProvider<>(delegate, transformation);
    }

    private TransformingSourceProvider(final AdvancedSchemaSourceProvider<I> delegate,
            final SchemaSourceTransformation<I, O> transformation) {
        this.delegate = Preconditions.checkNotNull(delegate, "delegate must not be null");
        this.transformation = Preconditions.checkNotNull(transformation, "transformation must not be null");
    }

    @Override
    public AdvancedSchemaSourceProvider<I> getDelegate() {
        return delegate;
    }

    @Override
    public Optional<O> getSchemaSource(final SourceIdentifier sourceIdentifier) {
        Optional<I> potentialSource = getDelegate().getSchemaSource(sourceIdentifier);
        if (potentialSource.isPresent()) {
            I inputSource = potentialSource.get();
            return Optional.<O> of(transformation.transform(inputSource));
        }
        return Optional.absent();
    }

    @Override
    public Optional<O> getSchemaSource(final String moduleName, final Optional<String> revision) {
        return getSchemaSource(SourceIdentifier.create(moduleName, revision));
    }
}
