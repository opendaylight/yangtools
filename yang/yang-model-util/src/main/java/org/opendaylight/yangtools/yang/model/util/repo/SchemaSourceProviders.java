package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Optional;

public class SchemaSourceProviders {

    @SuppressWarnings("rawtypes")
    private static final SchemaSourceProvider NOOP_PROVIDER = new SchemaSourceProvider() {

        @Override
        public Optional getSchemaSource(String moduleName, Optional revision) {
            return Optional.absent();
        }

    };

    @SuppressWarnings("unchecked")
    public static <T> SchemaSourceProvider<T> noopProvider() {
        return (SchemaSourceProvider<T>) NOOP_PROVIDER;
    }

}
