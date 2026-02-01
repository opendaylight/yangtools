/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.dagger.yang.parser.vanilla.DaggerVanillaYangParserComponent;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;

abstract class AbstractSchemaRepositoryTest {
    static final @NonNull YangParserFactory PARSER_FACTORY = DaggerVanillaYangParserComponent.create().parserFactory();
    static final @NonNull YangTextToIRSourceTransformer TEXT_TO_IR =
        ServiceLoader.load(YangTextToIRSourceTransformer.class).findFirst().orElseThrow();

    static @NonNull EffectiveModelContext assertModelContext(
            final SetMultimap<QNameModule, QNameModule> modulesWithSupportedDeviations, final String... resources) {
        final var future = createModelContext(modulesWithSupportedDeviations, resources);
        try {
            return Futures.getDone(future);
        } catch (ExecutionException e) {
            throw new AssertionError("Failed to create context", e);
        }
    }

    static @NonNull ExecutionException assertExecutionException(
            final SetMultimap<QNameModule, QNameModule> modulesWithSupportedDeviations, final String... resources) {
        final var future = createModelContext(modulesWithSupportedDeviations, resources);
        return assertThrows(ExecutionException.class, () -> Futures.getDone(future));
    }

    static ListenableFuture<EffectiveModelContext> createModelContext(
            final SetMultimap<QNameModule, QNameModule> modulesWithSupportedDeviations, final String... resources) {
        final var sharedSchemaRepository = new SharedSchemaRepository(PARSER_FACTORY, "test");
        final var requiredSources = Arrays.stream(resources)
            .map(resource -> {
                final var yangSource = assertYangTextResource(resource);
                yangSource.register(sharedSchemaRepository);
                yangSource.setResult();
                return yangSource.getId();
            })
            .collect(Collectors.toUnmodifiableList());

        return sharedSchemaRepository
            .createEffectiveModelContextFactory(SchemaContextFactoryConfiguration.builder()
                .setModulesDeviatedByModules(modulesWithSupportedDeviations)
                .build())
            .createEffectiveModelContext(requiredSources);
    }

    static final SettableSchemaProvider<YangIRSource> assertYangTextResource(final String resourceName) {
        return SettableSchemaProvider.createImmediate(assertDoesNotThrow(() -> TEXT_TO_IR.transformSource(
            new URLYangTextSource(AbstractSchemaRepositoryTest.class.getResource(resourceName)))),
            YangIRSource.class);
    }

    static final void assertSchemaContext(final EffectiveModelContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModuleStatements().size());
    }
}
