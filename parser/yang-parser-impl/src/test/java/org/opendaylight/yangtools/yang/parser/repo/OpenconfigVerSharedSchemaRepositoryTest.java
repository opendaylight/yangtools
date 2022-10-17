/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class OpenconfigVerSharedSchemaRepositoryTest {
    @Test
    public void testSharedSchemaRepository() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("shared-schema-repo-test");

        final SettableSchemaProvider<YangIRSchemaSource> bar = getImmediateYangSourceProviderFromResource(
                "/openconfig-version/shared-schema-repository/bar@2016-01-01.yang");
        bar.register(sharedSchemaRepository);
        bar.setResult();
        final SettableSchemaProvider<YangIRSchemaSource> foo = getImmediateYangSourceProviderFromResource(
                "/openconfig-version/shared-schema-repository/foo.yang");
        foo.register(sharedSchemaRepository);
        foo.setResult();
        final SettableSchemaProvider<YangIRSchemaSource> semVer = getImmediateYangSourceProviderFromResource(
                "/openconfig-version/shared-schema-repository/openconfig-extensions.yang");
        semVer.register(sharedSchemaRepository);
        semVer.setResult();

        final EffectiveModelContextFactory fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final ListenableFuture<EffectiveModelContext> inetAndTopologySchemaContextFuture =
                fact.createEffectiveModelContext(bar.getId(), foo.getId(), semVer.getId());
        assertTrue(inetAndTopologySchemaContextFuture.isDone());
        assertSchemaContext(inetAndTopologySchemaContextFuture.get(), 3);

        final ListenableFuture<EffectiveModelContext> barSchemaContextFuture =
                fact.createEffectiveModelContext(bar.getId(), semVer.getId());
        assertTrue(barSchemaContextFuture.isDone());
        assertSchemaContext(barSchemaContextFuture.get(), 2);
    }

    private static void assertSchemaContext(final SchemaContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModules().size());
    }

    static SettableSchemaProvider<YangIRSchemaSource> getImmediateYangSourceProviderFromResource(
            final String resourceName) throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToIRTransformer.transformText(yangSource),
            YangIRSchemaSource.class);
    }
}
