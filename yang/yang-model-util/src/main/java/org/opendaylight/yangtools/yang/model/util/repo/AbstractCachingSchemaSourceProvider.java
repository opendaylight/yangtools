package org.opendaylight.yangtools.yang.model.util.repo;

import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.concepts.Registration;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.*;

public abstract class AbstractCachingSchemaSourceProvider<I, O> implements SchemaSourceProvider<O>,
        Delegator<SchemaSourceProvider<I>> {

    private final SchemaSourceProvider<I> defaultDelegate;

    protected AbstractCachingSchemaSourceProvider(SchemaSourceProvider<I> delegate) {
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
            SchemaSourceProvider<I> delegate) {
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

    public SchemaSourceProvider<I> getDelegate() {
        return defaultDelegate;
    }

    public SchemaSourceProvider<O> createInstanceFor(SchemaSourceProvider<I> delegate) {
        checkNotNull(delegate, "Delegate should not be null");
        return new SchemaSourceProviderInstance(delegate);
    }

    private class SchemaSourceProviderInstance implements //
    SchemaSourceProvider<O>, 
    Delegator<SchemaSourceProvider<I>> {

        private final SchemaSourceProvider<I> delegate;

        protected SchemaSourceProviderInstance(SchemaSourceProvider<I> delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public Optional<O> getSchemaSource(String moduleName, Optional<String> revision) {
            return getSchemaSource(SourceIdentifier.create(moduleName, revision));
        }

        @Override
        public SchemaSourceProvider<I> getDelegate() {
            return delegate;
        }

        @Override
        public Optional<O> getSchemaSource(SourceIdentifier sourceIdentifier) {
            return getSchemaSourceImpl(sourceIdentifier, getDelegate());
        }
    }
}
