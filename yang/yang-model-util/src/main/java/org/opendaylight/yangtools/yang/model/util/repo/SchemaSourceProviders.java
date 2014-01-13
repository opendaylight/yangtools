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

import com.google.common.base.Optional;

public class SchemaSourceProviders {

    @SuppressWarnings("rawtypes")
    private static final SchemaSourceProvider NOOP_PROVIDER = new AdvancedSchemaSourceProvider() {

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
            AdvancedSchemaSourceProvider<String> delegate) {
        return new StringToInputStreamSchemaSourceProvider(delegate);
    }

    public static <O> AdvancedSchemaSourceProvider<O> toAdvancedSchemaSourceProvider(SchemaSourceProvider<O> schemaSourceProvider) {
        if (schemaSourceProvider instanceof AdvancedSchemaSourceProvider<?>) {
            return (AdvancedSchemaSourceProvider<O>) schemaSourceProvider;
        }
        return new SchemaSourceCompatibilityWrapper<O>(schemaSourceProvider);
    }

    private final static class StringToInputStreamSchemaSourceProvider implements //
            AdvancedSchemaSourceProvider<InputStream>, Delegator<AdvancedSchemaSourceProvider<String>> {

        private AdvancedSchemaSourceProvider<String> delegate;

        public StringToInputStreamSchemaSourceProvider(AdvancedSchemaSourceProvider<String> delegate) {
            this.delegate = delegate;
        }

        @Override
        public AdvancedSchemaSourceProvider<String> getDelegate() {
            return delegate;
        }

        @Override
        public Optional<InputStream> getSchemaSource(SourceIdentifier sourceIdentifier) {
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
        public Optional<InputStream> getSchemaSource(String moduleName, Optional<String> revision) {
            return getSchemaSource(SourceIdentifier.create(moduleName, revision));
        }
    }

    private final static class SchemaSourceCompatibilityWrapper<O> implements //
            AdvancedSchemaSourceProvider<O>, //
            Delegator<SchemaSourceProvider<O>> {

        private final SchemaSourceProvider<O> delegate;

        public SchemaSourceCompatibilityWrapper(SchemaSourceProvider<O> delegate) {
            this.delegate = delegate;
        }

        @Override
        public SchemaSourceProvider<O> getDelegate() {
            return delegate;
        }
        
        @Override
        public Optional<O> getSchemaSource(SourceIdentifier sourceIdentifier) {
            
            final String moduleName = sourceIdentifier.getName();
            Optional<String> revision = Optional.fromNullable(sourceIdentifier.getRevision());
            return delegate.getSchemaSource(moduleName, revision);
        }
        
        @Override
        public Optional<O> getSchemaSource(String moduleName, Optional<String> revision) {
            return delegate.getSchemaSource(moduleName, revision);
        }
    }

}
