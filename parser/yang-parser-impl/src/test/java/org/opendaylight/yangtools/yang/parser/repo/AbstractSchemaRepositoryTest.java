/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public abstract class AbstractSchemaRepositoryTest {
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
        final var sharedSchemaRepository = new SharedSchemaRepository();
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

    private static SettableSchemaProvider<YangIRSchemaSource> assertYangTextResource(final String resourceName) {
        final YangIRSchemaSource yangSource;
        try {
            yangSource = TextToIRTransformer.transformText(YangTextSource.forResource(resourceName));
        } catch (YangSyntaxErrorException | IOException e) {
            throw new AssertionError("Failed to parse " + resourceName, e);
        }
        return SettableSchemaProvider.createImmediate(yangSource, YangIRSchemaSource.class);
    }
}
