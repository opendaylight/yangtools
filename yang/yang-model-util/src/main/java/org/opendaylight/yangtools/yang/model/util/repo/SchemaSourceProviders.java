/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.opendaylight.yangtools.concepts.Delegator;

/**
 *
 * Utility functions for {@link SchemaSourceProvider}
 *
 *
 * @deprecated Utility classes for deprecated APIs.
 */
@Deprecated
public final class SchemaSourceProviders {

    @SuppressWarnings("rawtypes")
    private static final SchemaSourceProvider NOOP_PROVIDER = new AdvancedSchemaSourceProvider() {

        @Override
        public Optional getSchemaSource(final String moduleName, final Optional revision) {
            return Optional.absent();
        }

        @Override
        public Optional getSchemaSource(final SourceIdentifier sourceIdentifier) {
            return Optional.absent();
        }

    };

    @SuppressWarnings("rawtypes")
    private static final SchemaSourceTransformation IDENTITY_TRANFORMATION = new IdentityTransformation();

    private static final StringToInputStreamTransformation STRING_TO_INPUTSTREAM_TRANSFORMATION = new StringToInputStreamTransformation();

    private SchemaSourceProviders() {
        throw new UnsupportedOperationException("Utility class.");
    }

    /**
     * Returns a noop schema source provider.
     *
     * Noop schema provider returns {@link Optional#absent()} for each call to
     * query schema source.
     *
     * @return A reusable no-operation provider.
     */
    @SuppressWarnings("unchecked")
    public static <T> SchemaSourceProvider<T> noopProvider() {
        return NOOP_PROVIDER;
    }

    /**
     *
     * Returns delegating schema source provider which returns InputStream from
     * supplied String based schema source provider.
     *
     * @param delegate
     * @return InputStream-based source provider.
     */
    public static SchemaSourceProvider<InputStream> inputStreamProviderfromStringProvider(
            final AdvancedSchemaSourceProvider<String> delegate) {
        return TransformingSourceProvider.create(delegate, STRING_TO_INPUTSTREAM_TRANSFORMATION);
    }

    /**
     * Returns identity implementation of SchemaSourceTransformation
     *
     * Identity implementation of SchemaSourceTransformation is useful
     * for usecases where Input and Output of SchemaSourceTransformation
     * are identitcal, and you want to reuse input as an output.
     *
     * This implementation is really simple <code>return input;</code>.
     *
     * @return Identity transformation.
     */
    @SuppressWarnings("unchecked")
    public static <I> SchemaSourceTransformation<I, I> identityTransformation() {
        return IDENTITY_TRANFORMATION;
    }

    public static <I, O> SchemaSourceTransformation<I, O> schemaSourceTransformationFrom(
            final Function<I, O> transformation) {
        return new FunctionBasedSchemaSourceTransformation<I, O>(transformation);
    }

    /**
     *
     * Casts {@link SchemaSourceProvider} to
     * {@link AdvancedSchemaSourceProvider} or wraps it with utility
     * implementation if supplied delegate does not implement
     * {@link AdvancedSchemaSourceProvider}.
     *
     * @param schemaSourceProvider
     */
    public static <O> AdvancedSchemaSourceProvider<O> toAdvancedSchemaSourceProvider(
            final SchemaSourceProvider<O> schemaSourceProvider) {
        if (schemaSourceProvider instanceof AdvancedSchemaSourceProvider<?>) {
            return (AdvancedSchemaSourceProvider<O>) schemaSourceProvider;
        }
        return new SchemaSourceCompatibilityWrapper<O>(schemaSourceProvider);
    }

    private static final class FunctionBasedSchemaSourceTransformation<I, O> implements
            SchemaSourceTransformation<I, O> {


        private final Function<I, O> delegate;

        protected FunctionBasedSchemaSourceTransformation(final Function<I, O> delegate) {
            super();
            this.delegate = Preconditions.checkNotNull(delegate, "delegate MUST NOT be null.");
        }

        @Override
        public O transform(final I input) {
            return delegate.apply(input);
        }

        @Override
        public String toString() {
            return "FunctionBasedSchemaSourceTransformation [delegate=" + delegate + "]";
        }
    }

    private final static class SchemaSourceCompatibilityWrapper<O> implements //
            AdvancedSchemaSourceProvider<O>, //
            Delegator<SchemaSourceProvider<O>> {

        private final SchemaSourceProvider<O> delegate;

        public SchemaSourceCompatibilityWrapper(final SchemaSourceProvider<O> delegate) {
            this.delegate = delegate;
        }

        @Override
        public SchemaSourceProvider<O> getDelegate() {
            return delegate;
        }


        /*
         * Deprecation warnings are suppresed, since this implementation
         * needs to invoke deprecated method in order to provide
         * implementation of non-deprecated APIs using legacy ones.
         *
         * (non-Javadoc)
         * @see org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider#getSchemaSource(org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier)
         */
        @Override
        public Optional<O> getSchemaSource(final SourceIdentifier sourceIdentifier) {

            final String moduleName = sourceIdentifier.getName();
            Optional<String> revision = Optional.fromNullable(sourceIdentifier.getRevision());
            return delegate.getSchemaSource(moduleName, revision);
        }

        /*
         * Deprecation warnings are suppresed, since this implementation
         * needs to invoke deprecated method in order to provide
         * implementation of non-deprecated APIs using legacy ones.
         *
         * (non-Javadoc)
         * @see org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider#getSchemaSource(org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier)
         */
        @Override
        public Optional<O> getSchemaSource(final String moduleName, final Optional<String> revision) {
            return delegate.getSchemaSource(moduleName, revision);
        }
    }

    @SuppressWarnings("rawtypes")
    private static class  IdentityTransformation implements SchemaSourceTransformation {

        @Override
        public Object transform(final Object input) {
            return input;
        }
    }

    private static class StringToInputStreamTransformation implements SchemaSourceTransformation<String, InputStream> {

        @Override
        public InputStream transform(final String input) {
            return new ByteArrayInputStream(input.getBytes(Charsets.UTF_8));
        }

    }

}
