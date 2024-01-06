/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

@ExtendWith(MockitoExtension.class)
class SoftSchemaSourceCacheTest {
    private static final Class<YangSourceRepresentation> REPRESENTATION = YangSourceRepresentation.class;

    @Mock
    private SchemaSourceRegistry registry;
    @Mock
    private Registration registration;

    @Test
    void inMemorySchemaSourceCacheTest() {
        try (var cache = new SoftSchemaSourceCache<>(registry, REPRESENTATION)) {
            assertNotNull(cache);
        }
    }

    @Test
    void inMemorySchemaSourceCacheOfferAndGetSourcestest() throws Exception {
        doNothing().when(registration).close();
        doReturn(registration).when(registry).registerSchemaSource(any(), any());

        try (var cache = new SoftSchemaSourceCache<>(registry, REPRESENTATION)) {
            final var content = "content";
            final var source = new TestingYangSource("test", "2012-12-12", content);
            cache.offer(source);
            final var sourceIdentifier = new SourceIdentifier("test", "2012-12-12");
            final var checkedSource = cache .getSource(sourceIdentifier);
            assertNotNull(checkedSource);
            final var yangSchemaSourceRepresentation = checkedSource.get();
            assertNotNull(yangSchemaSourceRepresentation);
            assertEquals(sourceIdentifier, yangSchemaSourceRepresentation.sourceId());
        }
    }

    @Test
    void inMemorySchemaSourceCacheNullGetSourcestest() throws Exception {
        try (var cache = new SoftSchemaSourceCache<>(registry, REPRESENTATION)) {
            final var sourceIdentifier = new SourceIdentifier("test", "2012-12-12");
            final var checkedSource = cache.getSource(sourceIdentifier);
            assertNotNull(checkedSource);
            assertThrows(ExecutionException.class, checkedSource::get);
        }
    }

    @Test
    void inMemorySchemaSourceCache3test() throws InterruptedException, ExecutionException {
        doNothing().when(registration).close();
        doReturn(registration).when(registry).registerSchemaSource(any(), any());

        try (var cache1 = new SoftSchemaSourceCache<>(registry, REPRESENTATION)) {
            try (var cache2 = new SoftSchemaSourceCache<>(registry, REPRESENTATION)) {
                final var content = "content";
                final var source = new TestingYangSource("test", "2012-12-12", content);
                cache1.offer(source);
                cache2.offer(source);

                final var sourceIdentifier = new SourceIdentifier("test", "2012-12-12");
                final var checkedSource = cache1.getSource(sourceIdentifier);
                final var checkedSource2 = cache2.getSource(sourceIdentifier);
                assertNotNull(checkedSource);
                assertNotNull(checkedSource2);

                assertEquals(checkedSource.get(), checkedSource2.get());
            }
        }
    }

    private static class TestingYangSource extends YangTextSource {
        private final String content;

        TestingYangSource(final String name, final String revision, final String content) {
            super(new SourceIdentifier(name, revision));
            this.content = content;
        }

        @Override
        public StringReader openStream() {
            return new StringReader(content);
        }

        @Override
        public String symbolicName() {
            return null;
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return toStringHelper;
        }
    }
}
