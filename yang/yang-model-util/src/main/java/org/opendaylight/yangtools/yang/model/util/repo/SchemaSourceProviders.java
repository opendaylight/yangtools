/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.opendaylight.yangtools.concepts.Delegator;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 *
 * Utility functions for {@link SchemaSourceProvider}
 *
 */
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
    private static final SchemaSourceTransformation NO_CHANGE_TRANFORMATION = new SchemaSourceTransformation() {

        @Override
        public Object transform(final Object input) {
            return input;
        }

    };


    public SchemaSourceProviders() {
        throw new UnsupportedOperationException("Utility class.");
    }

    /**
     * Returns a noop schema source provider.
     *
     * Noop schema provider returns {@link Optional#absent()} for each call to
     * query schema source.
     *
     * @return
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
     * @return
     */
    public static SchemaSourceProvider<InputStream> inputStreamProviderfromStringProvider(
            final AdvancedSchemaSourceProvider<String> delegate) {
        return new StringToInputStreamSchemaSourceProvider(delegate);
    }

    /**
     * Returns no-change implementation of SchemaSourceTransformation
     *
     * No-change implementation of SchemaSourceTransformation is useful
     * for usecases where Input and Output of SchemaSourceTransformation
     * are same, and you want to reuse input as an output.
     *
     * This implementation is really simple <code>return input;</code>.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <I> SchemaSourceTransformation<I, I> noChangeTransformation() {
        return NO_CHANGE_TRANFORMATION;
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

    private final static class StringToInputStreamSchemaSourceProvider implements //
            AdvancedSchemaSourceProvider<InputStream>, Delegator<AdvancedSchemaSourceProvider<String>> {

        private final AdvancedSchemaSourceProvider<String> delegate;

        public StringToInputStreamSchemaSourceProvider(final AdvancedSchemaSourceProvider<String> delegate) {
            this.delegate = delegate;
        }

        @Override
        public AdvancedSchemaSourceProvider<String> getDelegate() {
            return delegate;
        }

        @Override
        public Optional<InputStream> getSchemaSource(final SourceIdentifier sourceIdentifier) {
            Optional<String> potentialSource = getDelegate().getSchemaSource(sourceIdentifier);
            if (potentialSource.isPresent()) {
                String stringSource = potentialSource.get();
                @SuppressWarnings("deprecation")
                StringBufferInputStream stringInputStream = new StringBufferInputStream(stringSource);
                return Optional.<InputStream> of(stringInputStream);
            }
            return Optional.absent();
        }

        @Override
        public Optional<InputStream> getSchemaSource(final String moduleName, final Optional<String> revision) {
            return getSchemaSource(SourceIdentifier.create(moduleName, revision));
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

        @Override
        public Optional<O> getSchemaSource(final SourceIdentifier sourceIdentifier) {

            final String moduleName = sourceIdentifier.getName();
            Optional<String> revision = Optional.fromNullable(sourceIdentifier.getRevision());
            return delegate.getSchemaSource(moduleName, revision);
        }

        @Override
        public Optional<O> getSchemaSource(final String moduleName, final Optional<String> revision) {
            return delegate.getSchemaSource(moduleName, revision);
        }
    }

}
