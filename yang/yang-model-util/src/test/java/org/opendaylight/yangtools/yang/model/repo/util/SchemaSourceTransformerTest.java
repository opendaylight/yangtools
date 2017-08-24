/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Arrays;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangSchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.YinXmlSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;

@RunWith(MockitoJUnitRunner.class)
public class SchemaSourceTransformerTest {

    private SchemaSourceTransformer<YangSchemaSourceRepresentation, YinXmlSchemaSource> schema;
    private static final Class<YangSchemaSourceRepresentation> SRC_CLASS = YangSchemaSourceRepresentation.class;
    private static final Class<YinXmlSchemaSource> DST_CLASS = YinXmlSchemaSource.class;

    @Mock
    private SchemaRepository provider;

    @Mock
    private SchemaSourceRegistry consumer;

    @Mock
    private AsyncFunction<YangSchemaSourceRepresentation, YinXmlSchemaSource> function;

    @Test
    public void schemaSourceTransformerTest() {
        this.schema = new SchemaSourceTransformer<>(
                this.provider, SchemaSourceTransformerTest.SRC_CLASS, this.consumer,
                SchemaSourceTransformerTest.DST_CLASS, this.function);
        Assert.assertNotNull(this.schema);
    }

    @Test
    public void schemaSourceTransformerGetSourceTest() throws Exception {
        final Provider p = new Provider();
        final Registrator reg = new Registrator(p, SchemaSourceTransformerTest.SRC_CLASS,
                PotentialSchemaSource.Costs.IMMEDIATE);
        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("source");
        reg.register(sourceIdentifier);
        this.schema = new SchemaSourceTransformer<>(p,
                SchemaSourceTransformerTest.SRC_CLASS, this.consumer, SchemaSourceTransformerTest.DST_CLASS,
                this.function);
        final SchemaSourceProvider<YinXmlSchemaSource> provider = this.schema;
        final Future<? extends YinXmlSchemaSource> source = provider.getSource(sourceIdentifier);
        Assert.assertNotNull(source);
        source.cancel(true);
        Assert.assertTrue(source.isDone());
    }

    @Test
    public void schemaSourceRegAndUnregSchemaSourceTest() throws Exception {
        final SourceIdentifier sourceIdentifier = RevisionSourceIdentifier.create("source");
        final Foo<YangSchemaSourceRepresentation> foo = new Foo<>(sourceIdentifier,
                SchemaSourceTransformerTest.SRC_CLASS,
                PotentialSchemaSource.Costs.COMPUTATION);
        final Provider p = new Provider();

        final Registrator reg = new Registrator(p, SchemaSourceTransformerTest.SRC_CLASS,
                PotentialSchemaSource.Costs.IMMEDIATE);
        reg.register(sourceIdentifier);

        final Consumer c = new Consumer();
        this.schema = new SchemaSourceTransformer<>(p,
                SchemaSourceTransformerTest.SRC_CLASS, c, SchemaSourceTransformerTest.DST_CLASS, this.function);

        final SchemaSourceListener listener = this.schema;
        p.registerSchemaSourceListener(listener);

        final PotentialSchemaSource<?>[] potList = { foo.getPotentialSchemSource() };
        final Iterable<PotentialSchemaSource<?>> sources = Arrays.asList(potList);
        listener.schemaSourceRegistered(sources);
        final ListenableFuture<YinXmlSchemaSource> source = this.schema.getSource(sourceIdentifier);
        Assert.assertNotNull(source);

        listener.schemaSourceUnregistered(foo.getPotentialSchemSource());
        final ListenableFuture<YinXmlSchemaSource> source2 = this.schema.getSource(sourceIdentifier);
        Assert.assertNotNull(source2);
    }

    private class Foo<T extends SchemaSourceRepresentation> {

        final PotentialSchemaSource<T> src;

        Foo(final SourceIdentifier sourceIdentifier, final Class<T> representation, final Costs cost) {
            this.src = PotentialSchemaSource.create(sourceIdentifier, representation,
                    cost.getValue());
        }

        public PotentialSchemaSource<T> getPotentialSchemSource() {
            return this.src;
        }

    }

    private class Registrator extends AbstractSchemaSourceCache<YangSchemaSourceRepresentation> {

        Registrator(final SchemaSourceRegistry consumer, final Class<YangSchemaSourceRepresentation> srcClass,
                final Costs cost) {
            super(consumer, srcClass, cost);
        }

        @Override
        protected void offer(final YangSchemaSourceRepresentation source) {

        }

        @Override
        public ListenableFuture<? extends YangSchemaSourceRepresentation> getSource(
                final SourceIdentifier sourceIdentifier) {
            return Mockito.mock(ListenableFuture.class);
        }

    }

    private class Provider extends AbstractSchemaRepository {

        @Override
        public SchemaContextFactory createSchemaContextFactory(@Nonnull final SchemaSourceFilter filter) {
            return null;
        }

    }

    private class Consumer extends AbstractSchemaRepository {

        @Override
        public SchemaContextFactory createSchemaContextFactory(@Nonnull final SchemaSourceFilter filter) {
            return null;
        }

    }
}
