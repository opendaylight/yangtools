/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.spi.source.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.YangSchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.YinXmlSchemaSource;

@ExtendWith(MockitoExtension.class)
class SchemaSourceTransformerTest {
    public static final Class<YangSchemaSourceRepresentation> SRC_CLASS = YangSchemaSourceRepresentation.class;
    public static final Class<YinXmlSchemaSource> DST_CLASS = YinXmlSchemaSource.class;

    @Mock
    public SchemaRepository provider;

    @Mock
    public SchemaSourceRegistry consumer;

    @Mock
    public AsyncFunction<YangSchemaSourceRepresentation, YinXmlSchemaSource> function;

    public SchemaSourceTransformer<YangSchemaSourceRepresentation, YinXmlSchemaSource> schema;

    @Test
    void schemaSourceTransformerTest() {
        schema = new SchemaSourceTransformer<>(
                provider, SchemaSourceTransformerTest.SRC_CLASS, consumer,
                SchemaSourceTransformerTest.DST_CLASS, function);
        assertNotNull(schema);
    }

    @Test
    void schemaSourceTransformerGetSourceTest() {
        final var p = new Provider();
        final var reg = new Registrator(p, SchemaSourceTransformerTest.SRC_CLASS,
                PotentialSchemaSource.Costs.IMMEDIATE);
        final var sourceIdentifier = new SourceIdentifier("source");
        reg.register(sourceIdentifier);
        schema = new SchemaSourceTransformer<>(p,
                SchemaSourceTransformerTest.SRC_CLASS, consumer, SchemaSourceTransformerTest.DST_CLASS,
                function);
        final var prov = schema;
        final var source = prov.getSource(sourceIdentifier);
        assertNotNull(source);
        source.cancel(true);
        assertTrue(source.isDone());
    }

    @Test
    void schemaSourceRegAndUnregSchemaSourceTest() {
        final var sourceIdentifier = new SourceIdentifier("source");
        final var foo = new Foo<>(sourceIdentifier,
                SchemaSourceTransformerTest.SRC_CLASS,
                PotentialSchemaSource.Costs.COMPUTATION);
        final var p = new Provider();

        final var reg = new Registrator(p, SchemaSourceTransformerTest.SRC_CLASS,
                PotentialSchemaSource.Costs.IMMEDIATE);
        reg.register(sourceIdentifier);

        final var c = new Consumer();
        schema = new SchemaSourceTransformer<>(p, SchemaSourceTransformerTest.SRC_CLASS, c,
            SchemaSourceTransformerTest.DST_CLASS, function);

        final var listener = schema;
        p.registerSchemaSourceListener(listener);

        final var potList = new PotentialSchemaSource<?>[]{ foo.getPotentialSchemSource() };
        final var sources = Arrays.asList(potList);
        listener.schemaSourceRegistered(sources);
        final var source = schema.getSource(sourceIdentifier);
        assertNotNull(source);

        listener.schemaSourceUnregistered(foo.getPotentialSchemSource());
        final var source2 = schema.getSource(sourceIdentifier);
        assertNotNull(source2);
    }

    private static class Foo<T extends SchemaSourceRepresentation> {
        final PotentialSchemaSource<T> src;

        Foo(final SourceIdentifier sourceIdentifier, final Class<T> representation, final Costs cost) {
            src = PotentialSchemaSource.create(sourceIdentifier, representation, cost.getValue());
        }

        public PotentialSchemaSource<T> getPotentialSchemSource() {
            return src;
        }
    }

    private static class Registrator extends AbstractSchemaSourceCache<YangSchemaSourceRepresentation> {
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
            return SettableFuture.create();
        }
    }

    private static final class Provider extends AbstractSchemaRepository {
        @Override
        public EffectiveModelContextFactory createEffectiveModelContextFactory(
                final SchemaContextFactoryConfiguration config) {
            return mock(EffectiveModelContextFactory.class);
        }
    }

    private static final class Consumer extends AbstractSchemaRepository {
        @Override
        public EffectiveModelContextFactory createEffectiveModelContextFactory(
                final SchemaContextFactoryConfiguration config) {
            return mock(EffectiveModelContextFactory.class);
        }
    }
}
