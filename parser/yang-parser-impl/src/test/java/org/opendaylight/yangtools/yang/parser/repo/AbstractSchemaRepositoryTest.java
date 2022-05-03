/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public abstract class AbstractSchemaRepositoryTest {
    static ListenableFuture<EffectiveModelContext> createSchemaContext(
        final SetMultimap<QNameModule, QNameModule> modulesWithSupportedDeviations, final String... resources)
            throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository();

        final List<SourceIdentifier> requiredSources = new ArrayList<>();
        for (final String resource : resources) {
            final SettableSchemaProvider<IRSchemaSource> yangSource = immediateYangSourceProviderFromResource(resource);
            yangSource.register(sharedSchemaRepository);
            yangSource.setResult();
            requiredSources.add(yangSource.getId());
        }

        final SchemaContextFactoryConfiguration config = SchemaContextFactoryConfiguration.builder()
            .setModulesDeviatedByModules(modulesWithSupportedDeviations).build();
        return sharedSchemaRepository.createEffectiveModelContextFactory(config).createEffectiveModelContext(
            requiredSources);
    }

    private static SettableSchemaProvider<IRSchemaSource> immediateYangSourceProviderFromResource(
            final String resourceName) throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToIRTransformer.transformText(yangSource),
                IRSchemaSource.class);
    }
}
