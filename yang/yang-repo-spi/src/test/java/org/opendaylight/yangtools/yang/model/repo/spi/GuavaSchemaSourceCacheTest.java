/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangSchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@Deprecated
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class GuavaSchemaSourceCacheTest {
    public static final Class<YangSchemaSourceRepresentation> REPRESENTATION = YangSchemaSourceRepresentation.class;
    public static final long LIFETIME = 1000L;
    public static final TimeUnit UNITS = TimeUnit.MILLISECONDS;

    @Mock
    public SchemaSourceRegistry registry;
    @Mock
    public Registration registration;

    @Before
    public void setUp() {
        doNothing().when(registration).close();
        doReturn(registration).when(registry).registerSchemaSource(any(SchemaSourceProvider.class),
            any(PotentialSchemaSource.class));
    }

    @Test
    public void inMemorySchemaSourceCacheTest1() {
        try (var cache = GuavaSchemaSourceCache.createSoftCache(registry, REPRESENTATION)) {
            assertNotNull(cache);
        }
    }

    @Test
    public void inMemorySchemaSourceCacheTest2() {
        try (var cache = GuavaSchemaSourceCache.createSoftCache(registry, REPRESENTATION, LIFETIME, UNITS)) {
            assertNotNull(cache);
        }
    }

    @Test
    public void inMemorySchemaSourceCacheOfferAndGetSourcestest() throws Exception {
        try (var cache = GuavaSchemaSourceCache.createSoftCache(registry, REPRESENTATION)) {
            final String content = "content";
            final YangTextSchemaSource source = new TestingYangSource("test", "2012-12-12", content);
            cache.offer(source);
            final var sourceIdentifier = new SourceIdentifier("test", "2012-12-12");
            final var checkedSource = cache .getSource(sourceIdentifier);
            assertNotNull(checkedSource);
            final var yangSchemaSourceRepresentation = checkedSource.get();
            assertNotNull(yangSchemaSourceRepresentation);
            assertEquals(sourceIdentifier, yangSchemaSourceRepresentation.getIdentifier());
        }
    }

    @Test
    public void inMemorySchemaSourceCacheNullGetSourcestest() throws Exception {
        try (var cache = GuavaSchemaSourceCache.createSoftCache(registry, REPRESENTATION)) {
            final var sourceIdentifier = new SourceIdentifier("test", "2012-12-12");
            final var checkedSource = cache.getSource(sourceIdentifier);
            assertNotNull(checkedSource);
            assertThrows(ExecutionException.class, () -> checkedSource.get());
        }
    }

    @Test
    public void inMemorySchemaSourceCache3test() throws InterruptedException, ExecutionException {
        try (var cache1 = GuavaSchemaSourceCache.createSoftCache(registry, REPRESENTATION)) {
            try (var cache2 = GuavaSchemaSourceCache.createSoftCache(registry, REPRESENTATION, LIFETIME, UNITS)) {
                final String content = "content";
                final YangTextSchemaSource source = new TestingYangSource("test", "2012-12-12", content);
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

    private static class TestingYangSource extends YangTextSchemaSource {
        private final String content;

        TestingYangSource(final String name, final String revision, final String content) {
            super(new SourceIdentifier(name, revision));
            this.content = content;
        }

        @Override
        public InputStream openStream() {
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Optional<String> getSymbolicName() {
            return Optional.empty();
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return toStringHelper;
        }
    }
}
