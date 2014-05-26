/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import static com.google.common.base.Preconditions.checkNotNull;

import org.opendaylight.yangtools.concepts.Delegator;

import com.google.common.base.Optional;


/**
 *
 * Abstract caching schema provider with support of multiple context
 * per backing {@link SchemaSourceProvider}.
 *
 *
 * @param <I> Input Schema Source Type
 * @param <O> Output Schema Source Type
 */
public abstract class AbstractCachingSchemaSourceProvider<I, O> implements AdvancedSchemaSourceProvider<O>,
        Delegator<AdvancedSchemaSourceProvider<I>> {

    public class CompatibilitySchemaSourceProviderInstance implements SchemaSourceProvider<O> {

        @Override
        public Optional<O> getSchemaSource(final String moduleName, final Optional<String> revision) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private final AdvancedSchemaSourceProvider<I> defaultDelegate;

    /**
     * Construct caching schema source provider with supplied delegate
     * which is used as default when missed.
     *
     * @param delegate
     */
    protected AbstractCachingSchemaSourceProvider(final AdvancedSchemaSourceProvider<I> delegate) {
        this.defaultDelegate = delegate;
    }

    @Override
    public Optional<O> getSchemaSource(final String moduleName, final Optional<String> revision) {
        checkNotNull(moduleName, "Module name should not be null.");
        checkNotNull(revision, "Revision should not be null");
        return getSchemaSource(SourceIdentifier.create(moduleName, revision));
    }

    @Override
    public Optional<O> getSchemaSource(final SourceIdentifier sourceIdentifier) {
        return getSchemaSourceImpl(sourceIdentifier, defaultDelegate);
    }

    /**
     * Actual implementation of schema source retrieval.
     *
     * <ul>
     * <li>lookups schema source in finalized implementation via {@link #getCachedSchemaSource(SourceIdentifier)}
     * <li>If source was found, returns to client code.
     * <li>Lookups shcema source in supplied <code>delegate</code>
     * <li>Result cached by finalized implementation via {@link #cacheSchemaSource(SourceIdentifier, Optional)}
     * <li>Result is returned to client code.
     * </ul>
     *
     * @param identifier Source identifier
     * @param delegate Delegate to lookup if there is a miss.
     * @return Optional of schema source, present if source was found. Absent otherwise.
     */
    protected final Optional<O> getSchemaSourceImpl(final SourceIdentifier identifier,
            final AdvancedSchemaSourceProvider<I> delegate) {
        checkNotNull(identifier, "Source identifier name should not be null.");

        Optional<O> cached = getCachedSchemaSource(identifier);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<I> live = delegate.getSchemaSource(identifier);
        return cacheSchemaSource(identifier, live);
    }

    abstract protected Optional<O> cacheSchemaSource(SourceIdentifier identifier, Optional<I> stream);

    abstract protected Optional<O> getCachedSchemaSource(SourceIdentifier identifier);

    @Override
    public AdvancedSchemaSourceProvider<I> getDelegate() {
        return defaultDelegate;
    }

    /**
     * Creates an instance of source provider, which uses this cache for caching
     * and delegate for lookup of missing sources.
     *
     * @param delegate Backing {@link SchemaSourceProvider} which should be used for lookup
     *   for sources not present in schema.
     * @return new instance of {@link SchemaSourceProvider} which first lookup in cache
     *   and then in delegate.
     */
    public SchemaSourceProvider<O> createInstanceFor(final SchemaSourceProvider<I> delegate) {
        checkNotNull(delegate, "Delegate should not be null");
        return new SchemaSourceProviderInstance(SchemaSourceProviders.toAdvancedSchemaSourceProvider(delegate));

    }

    private class SchemaSourceProviderInstance implements //
    AdvancedSchemaSourceProvider<O>,
    Delegator<AdvancedSchemaSourceProvider<I>> {

        private final AdvancedSchemaSourceProvider<I> delegate;

        protected SchemaSourceProviderInstance(final AdvancedSchemaSourceProvider<I> delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public Optional<O> getSchemaSource(final String moduleName, final Optional<String> revision) {
            return getSchemaSource(SourceIdentifier.create(moduleName, revision));
        }

        @Override
        public AdvancedSchemaSourceProvider<I> getDelegate() {
            return delegate;
        }

        @Override
        public Optional<O> getSchemaSource(final SourceIdentifier sourceIdentifier) {
            return getSchemaSourceImpl(sourceIdentifier, getDelegate());
        }
    }
}
