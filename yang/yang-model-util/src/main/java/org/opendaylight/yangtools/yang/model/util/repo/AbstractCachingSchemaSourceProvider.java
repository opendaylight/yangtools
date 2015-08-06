/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.concepts.Delegator;


/**
 *
 * Abstract caching schema provider with support of multiple context
 * per backing {@link SchemaSourceProvider}.
 *
 * @param <I> Input Schema Source Representation
 * @param <O> Output Schema Source Representation
 *
 * @deprecated Replaced with {@link org.opendaylight.yangtools.yang.model.repo.util.AbstractSchemaSourceCache}
 */
@Deprecated
public abstract class AbstractCachingSchemaSourceProvider<I, O> implements AdvancedSchemaSourceProvider<O>,
        Delegator<AdvancedSchemaSourceProvider<I>> {

    private final AdvancedSchemaSourceProvider<I> defaultDelegate;

    /**
     * Construct caching schema source provider with supplied delegate.
     *
     * Default delegate is is used to retrieve schema source when cache does not
     * contain requested sources.
     *
     * @param delegate SchemaSourceProvided used to look up and retrieve schema source
     * when cache does not contain requested sources.
     */
    protected AbstractCachingSchemaSourceProvider(final AdvancedSchemaSourceProvider<I> delegate) {
        this.defaultDelegate = delegate;
    }

    @Override
    public Optional<O> getSchemaSource(final String moduleName, final Optional<String> revision) {
        Preconditions.checkNotNull(moduleName, "Module name should not be null.");
        Preconditions.checkNotNull(revision, "Revision should not be null");
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
     * <li>look up cached schema source via {@link #getCachedSchemaSource(SourceIdentifier)}
     * <li>If source was found in cache, returns source to client code.
     * <li>If source was not found in cache, Look up schema source in supplied <code>delegate</code>
     * <li>Updates cache with schema from delegate by {@link #cacheSchemaSource(SourceIdentifier, Optional)}
     * <li>Result is returned to client code.
     * </ul>
     *
     * @param identifier Source identifier
     * @param delegate Delegate to lookup if there is a miss.
     * @return Optional of schema source, present if source was found. Absent otherwise.
     */
    protected final Optional<O> getSchemaSourceImpl(final SourceIdentifier identifier,
            final AdvancedSchemaSourceProvider<I> delegate) {
        Preconditions.checkNotNull(identifier, "Source identifier name should not be null.");

        Optional<O> cached = getCachedSchemaSource(identifier);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<I> live = delegate.getSchemaSource(identifier);
        return cacheSchemaSource(identifier, live);
    }

    /**
     * Caches supplied result and returns cached result which should be returned to client.
     *
     * <p>
     * Implementations of cache are required to cache schema source if possible.
     * They are not required to cache {@link Optional#absent()}.
     *
     * Implementations are required to transform source representation if <code>O</code> and <code>I</code>
     * are different.
     *
     * This method SHOULD NOT fail and should recover from Runtime exceptions
     * by not caching source and only transforming it.
     *
     * @param identifier Source Identifier for which schema SHOULD be cached
     * @param input Optional of schema source, representing one returned from delegate.
     * @return Optional of schema source, representing result returned from this cache.
     */
    protected abstract Optional<O> cacheSchemaSource(SourceIdentifier identifier, Optional<I> input);

    /**
     * Returns cached schema source of {@link Optional#absent()} if source is not present in cache.
     *
     * <p>
     * Implementations of cache MUST return cached schema source, if it is present in cache,
     * otherwise source will be requested from deleate and then cache will be updated
     * via {@link #cacheSchemaSource(SourceIdentifier, Optional)}.
     *
     * @param identifier Source Identifier for which schema should be retrieved.
     * @return Cached schema source.
     */
    protected abstract Optional<O> getCachedSchemaSource(SourceIdentifier identifier);

    @Override
    public AdvancedSchemaSourceProvider<I> getDelegate() {
        return defaultDelegate;
    }

    /**
     * Creates an lightweight instance of source provider, which uses this cache for caching
     * and supplied additional delegate for lookup of not cached sources.
     * <p>
     *
     * @param delegate Backing {@link SchemaSourceProvider} which should be used for lookup
     *   for sources not present in schema.
     * @return new instance of {@link SchemaSourceProvider} which first lookup in cache
     *   and then in delegate.
     *
     */
    @Beta
    public SchemaSourceProvider<O> createInstanceFor(final SchemaSourceProvider<I> delegate) {
        return new SchemaSourceProviderInstance(SchemaSourceProviders.toAdvancedSchemaSourceProvider(delegate));

    }

    /**
     *
     * Lightweight instance of source provider, which is associated with parent
     * {@link AbstractCachingSchemaSourceProvider}, but uses
     * different delegate for retrieving not cached sources.
     *
     */
    @Beta
    private class SchemaSourceProviderInstance implements AdvancedSchemaSourceProvider<O>, Delegator<AdvancedSchemaSourceProvider<I>> {

        private final AdvancedSchemaSourceProvider<I> delegate;

        protected SchemaSourceProviderInstance(final AdvancedSchemaSourceProvider<I> delegate) {
            super();
            this.delegate = Preconditions.checkNotNull(delegate, "Delegate should not be null");
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
