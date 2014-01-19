/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import org.opendaylight.yangtools.concepts.Delegator;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.*;

public abstract class AbstractCachingSchemaSourceProvider<I, O> implements AdvancedSchemaSourceProvider<O>,
        Delegator<AdvancedSchemaSourceProvider<I>> {

    public class CompatibilitySchemaSourceProviderInstance implements SchemaSourceProvider<O> {

        @Override
        public Optional<O> getSchemaSource(String moduleName, Optional<String> revision) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private final AdvancedSchemaSourceProvider<I> defaultDelegate;

    protected AbstractCachingSchemaSourceProvider(AdvancedSchemaSourceProvider<I> delegate) {
        this.defaultDelegate = delegate;
    }

    @Override
    public Optional<O> getSchemaSource(String moduleName, Optional<String> revision) {
        checkNotNull(moduleName, "Module name should not be null.");
        checkNotNull(revision, "Revision should not be null");
        return getSchemaSource(SourceIdentifier.create(moduleName, revision));
    }
    
    @Override
    public Optional<O> getSchemaSource(SourceIdentifier sourceIdentifier) {
        return getSchemaSourceImpl(sourceIdentifier, defaultDelegate);
    }

    protected final Optional<O> getSchemaSourceImpl(SourceIdentifier identifier,
            AdvancedSchemaSourceProvider<I> delegate) {
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

    public AdvancedSchemaSourceProvider<I> getDelegate() {
        return defaultDelegate;
    }

    public SchemaSourceProvider<O> createInstanceFor(SchemaSourceProvider<I> delegate) {
        checkNotNull(delegate, "Delegate should not be null");
        return new SchemaSourceProviderInstance(SchemaSourceProviders.toAdvancedSchemaSourceProvider(delegate));
            
    }

    private class SchemaSourceProviderInstance implements //
    AdvancedSchemaSourceProvider<O>, 
    Delegator<AdvancedSchemaSourceProvider<I>> {

        private final AdvancedSchemaSourceProvider<I> delegate;

        protected SchemaSourceProviderInstance(AdvancedSchemaSourceProvider<I> delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public Optional<O> getSchemaSource(String moduleName, Optional<String> revision) {
            return getSchemaSource(SourceIdentifier.create(moduleName, revision));
        }

        @Override
        public AdvancedSchemaSourceProvider<I> getDelegate() {
            return delegate;
        }

        @Override
        public Optional<O> getSchemaSource(SourceIdentifier sourceIdentifier) {
            return getSchemaSourceImpl(sourceIdentifier, getDelegate());
        }
    }
}
