/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class OpenconfigVerSharedSchemaRepositoryTest {
    @Test
    public void testSharedSchemaRepository() throws Exception {
        final var sharedSchemaRepository = new SharedSchemaRepository("shared-schema-repo-test");

        final var bar = getImmediateYangSourceProviderFromResource(
                "/openconfig-version/shared-schema-repository/bar@2016-01-01.yang");
        bar.register(sharedSchemaRepository);
        bar.setResult();
        final var foo = getImmediateYangSourceProviderFromResource(
                "/openconfig-version/shared-schema-repository/foo.yang");
        foo.register(sharedSchemaRepository);
        foo.setResult();
        final var semVer = getImmediateYangSourceProviderFromResource(
                "/openconfig-version/shared-schema-repository/openconfig-extensions.yang");
        semVer.register(sharedSchemaRepository);
        semVer.setResult();

        final var fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final var inetAndTopologySchemaContextFuture =
                fact.createEffectiveModelContext(bar.getId(), foo.getId(), semVer.getId());
        assertTrue(inetAndTopologySchemaContextFuture.isDone());
        assertSchemaContext(inetAndTopologySchemaContextFuture.get(), 3);

        final var barSchemaContextFuture = fact.createEffectiveModelContext(bar.getId(), semVer.getId());
        assertTrue(barSchemaContextFuture.isDone());
        assertSchemaContext(barSchemaContextFuture.get(), 2);
    }

    private static void assertSchemaContext(final EffectiveModelContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModules().size());
    }

    static SettableSchemaProvider<YangIRSchemaSource> getImmediateYangSourceProviderFromResource(
            final String resourceName) throws Exception {
        return SettableSchemaProvider.createImmediate(
            TextToIRTransformer.transformText(YangTextSource.forResource(resourceName)), YangIRSchemaSource.class);
    }
}
