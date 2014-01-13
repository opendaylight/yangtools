package org.opendaylight.yangtools.yang.model.util.repo;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.opendaylight.yangtools.concepts.Delegator;

import com.google.common.base.Optional;

public class SchemaSourceProviders {

    @SuppressWarnings("rawtypes")
    private static final SchemaSourceProvider NOOP_PROVIDER = new SchemaSourceProvider() {

        @Override
        public Optional getSchemaSource(String moduleName, Optional revision) {
            return Optional.absent();
        }

        @Override
        public Optional getSchemaSource(SourceIdentifier sourceIdentifier) {
            return Optional.absent();
        }

    };

    @SuppressWarnings("unchecked")
    public static <T> SchemaSourceProvider<T> noopProvider() {
        return (SchemaSourceProvider<T>) NOOP_PROVIDER;
    }

    public static SchemaSourceProvider<InputStream> inputStreamProviderfromStringProvider(
            SchemaSourceProvider<String> delegate) {
        return new StringToInputStreamSchemaSourceProvider(delegate);
    }

    private final static class StringToInputStreamSchemaSourceProvider implements //
            SchemaSourceProvider<InputStream>, Delegator<SchemaSourceProvider<String>> {

        private SchemaSourceProvider<String> delegate;

        public StringToInputStreamSchemaSourceProvider(SchemaSourceProvider<String> delegate) {
            this.delegate = delegate;
        }

        @Override
        public SchemaSourceProvider<String> getDelegate() {
            return delegate;
        }

        @Override
        public Optional<InputStream> getSchemaSource(SourceIdentifier sourceIdentifier) {
            Optional<String> potential = getDelegate().getSchemaSource(sourceIdentifier);
            if (potential.isPresent()) {
                String stringStream = potential.get();
                return Optional.<InputStream> of(new StringBufferInputStream(stringStream));
            }
            return Optional.absent();
        }

        @Override
        public Optional<InputStream> getSchemaSource(String moduleName, Optional<String> revision) {
            return getSchemaSource(SourceIdentifier.create(moduleName, revision));
        }
    }

}
