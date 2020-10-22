/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangSchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InMemorySchemaSourceCacheTest {
    public static final Class<YangSchemaSourceRepresentation> REPRESENTATION = YangSchemaSourceRepresentation.class;
    public static final long LIFETIME = 1000L;
    public static final TimeUnit UNITS = TimeUnit.MILLISECONDS;

    @Mock
    public SchemaSourceRegistry registry;
    @Mock
    public SchemaSourceRegistration<?> registration;

    @Before
    public void setUp() throws Exception {
        doNothing().when(registration).close();
        doReturn(registration).when(registry).registerSchemaSource(any(SchemaSourceProvider.class),
                any(PotentialSchemaSource.class));
    }

    @Test
    public void inMemorySchemaSourceCacheTest1() {
        final InMemorySchemaSourceCache<YangSchemaSourceRepresentation> inMemorySchemaSourceCache =
            InMemorySchemaSourceCache.createSoftCache(this.registry, REPRESENTATION);
        assertNotNull(inMemorySchemaSourceCache);
        inMemorySchemaSourceCache.close();
    }

    @Test
    public void inMemorySchemaSourceCacheTest2() {
        final InMemorySchemaSourceCache<YangSchemaSourceRepresentation> inMemorySchemaSourceCache =
            InMemorySchemaSourceCache.createSoftCache(this.registry, REPRESENTATION, LIFETIME, UNITS);
        assertNotNull(inMemorySchemaSourceCache);
        inMemorySchemaSourceCache.close();
    }

    @Test
    public void inMemorySchemaSourceCacheOfferAndGetSourcestest() throws Exception {
        final InMemorySchemaSourceCache<YangSchemaSourceRepresentation> inMemorySchemaSourceCache =
            InMemorySchemaSourceCache.createSoftCache(this.registry, REPRESENTATION);
        final String content = "content";
        final YangTextSchemaSource source = new TestingYangSource("test", "2012-12-12", content);
        inMemorySchemaSourceCache.offer(source);
        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("test", Revision.of("2012-12-12"));
        final ListenableFuture<? extends YangSchemaSourceRepresentation> checkedSource = inMemorySchemaSourceCache
                .getSource(sourceIdentifier);
        assertNotNull(checkedSource);
        final YangSchemaSourceRepresentation yangSchemaSourceRepresentation = checkedSource.get();
        assertNotNull(yangSchemaSourceRepresentation);
        assertEquals(sourceIdentifier, yangSchemaSourceRepresentation.getIdentifier());
        inMemorySchemaSourceCache.close();
    }

    @Test
    public void inMemorySchemaSourceCacheNullGetSourcestest() throws Exception {
        final InMemorySchemaSourceCache<YangSchemaSourceRepresentation> inMemorySchemaSourceCache =
            InMemorySchemaSourceCache.createSoftCache(this.registry, REPRESENTATION);
        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("test", Revision.of("2012-12-12"));
        final ListenableFuture<? extends YangSchemaSourceRepresentation> checkedSource =
            inMemorySchemaSourceCache.getSource(sourceIdentifier);
        assertNotNull(checkedSource);


        assertThrows(ExecutionException.class, () -> checkedSource.get());
        inMemorySchemaSourceCache.close();
    }

    @Test
    public void inMemorySchemaSourceCache3test() throws InterruptedException, ExecutionException {
        final InMemorySchemaSourceCache<YangSchemaSourceRepresentation> inMemorySchemaSourceCache =
            InMemorySchemaSourceCache.createSoftCache(this.registry, REPRESENTATION);
        final InMemorySchemaSourceCache<YangSchemaSourceRepresentation> inMemorySchemaSourceCache2 =
            InMemorySchemaSourceCache.createSoftCache(this.registry, REPRESENTATION, LIFETIME, UNITS);

        final String content = "content";
        final YangTextSchemaSource source = new TestingYangSource("test", "2012-12-12", content);
        inMemorySchemaSourceCache.offer(source);
        inMemorySchemaSourceCache2.offer(source);

        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("test", Revision.of("2012-12-12"));
        final ListenableFuture<? extends YangSchemaSourceRepresentation> checkedSource =
            inMemorySchemaSourceCache.getSource(sourceIdentifier);
        final ListenableFuture<? extends SchemaSourceRepresentation> checkedSource2 =
            inMemorySchemaSourceCache2.getSource(sourceIdentifier);
        assertNotNull(checkedSource);
        assertNotNull(checkedSource2);

        assertEquals(checkedSource.get(), checkedSource2.get());
        inMemorySchemaSourceCache.close();
        inMemorySchemaSourceCache2.close();
    }

    private class TestingYangSource extends YangTextSchemaSource {

        private final String content;

        protected TestingYangSource(final String name, final String revision, final String content) {
            super(RevisionSourceIdentifier.create(name, Revision.ofNullable(revision)));
            this.content = content;
        }

        @Override
        protected MoreObjects.ToStringHelper addToStringAttributes(final MoreObjects.ToStringHelper toStringHelper) {
            return toStringHelper;
        }

        @Override
        public InputStream openStream() throws IOException {
            return new ByteArrayInputStream(this.content.getBytes(StandardCharsets.UTF_8));
        }
    }

}
