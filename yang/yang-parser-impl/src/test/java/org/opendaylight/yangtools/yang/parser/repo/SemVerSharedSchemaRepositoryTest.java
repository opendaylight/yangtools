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

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;

public class SemVerSharedSchemaRepositoryTest {

    @Test
    public void testSemVerSharedSchemaRepository() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "sem-ver-shared-schema-repo-test");

        final SettableSchemaProvider<ASTSchemaSource> bar = getImmediateYangSourceProviderFromResource(
            "/semantic-version/semver-shared-schema-repository/bar@2016-01-01.yang");
        bar.register(sharedSchemaRepository);
        bar.setResult();
        final SettableSchemaProvider<ASTSchemaSource> foo = getImmediateYangSourceProviderFromResource(
            "/semantic-version/semver-shared-schema-repository/foo.yang");
        foo.register(sharedSchemaRepository);
        foo.setResult();
        final SettableSchemaProvider<ASTSchemaSource> semVer = getImmediateYangSourceProviderFromResource(
            "/semantic-version/semver-shared-schema-repository/semantic-version.yang");
        semVer.register(sharedSchemaRepository);
        semVer.setResult();

        final SchemaContextFactory fact = sharedSchemaRepository
                .createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final CheckedFuture<SchemaContext, SchemaResolutionException> inetAndTopologySchemaContextFuture = fact
                .createSchemaContext(Lists.newArrayList(bar.getId(), foo.getId(), semVer.getId()),
                    StatementParserMode.SEMVER_MODE);
        assertTrue(inetAndTopologySchemaContextFuture.isDone());
        assertSchemaContext(inetAndTopologySchemaContextFuture.checkedGet(), 3);

        final CheckedFuture<SchemaContext, SchemaResolutionException> barSchemaContextFuture = fact
                .createSchemaContext(Lists.newArrayList(bar.getId(), semVer.getId()), StatementParserMode.SEMVER_MODE);
        assertTrue(barSchemaContextFuture.isDone());
        assertSchemaContext(barSchemaContextFuture.checkedGet(), 2);
    }

    @Test
    public void testSharedSchemaRepository() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository("shared-schema-repo-test");

        final SettableSchemaProvider<ASTSchemaSource> bar = getImmediateYangSourceProviderFromResource(
            "/semantic-version/shared-schema-repository/bar@2016-01-01.yang");
        bar.register(sharedSchemaRepository);
        bar.setResult();
        final SettableSchemaProvider<ASTSchemaSource> foo = getImmediateYangSourceProviderFromResource(
            "/semantic-version/shared-schema-repository/foo.yang");
        foo.register(sharedSchemaRepository);
        foo.setResult();
        final SettableSchemaProvider<ASTSchemaSource> semVer = getImmediateYangSourceProviderFromResource(
            "/semantic-version/shared-schema-repository/semantic-version.yang");
        semVer.register(sharedSchemaRepository);
        semVer.setResult();

        final SchemaContextFactory fact = sharedSchemaRepository
                .createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final CheckedFuture<SchemaContext, SchemaResolutionException> inetAndTopologySchemaContextFuture = fact
                .createSchemaContext(Lists.newArrayList(bar.getId(), foo.getId(), semVer.getId()));
        assertTrue(inetAndTopologySchemaContextFuture.isDone());
        assertSchemaContext(inetAndTopologySchemaContextFuture.checkedGet(), 3);

        final CheckedFuture<SchemaContext, SchemaResolutionException> barSchemaContextFuture = fact
                .createSchemaContext(Lists.newArrayList(bar.getId(), semVer.getId()));
        assertTrue(barSchemaContextFuture.isDone());
        assertSchemaContext(barSchemaContextFuture.checkedGet(), 2);
    }

    private static void assertSchemaContext(final SchemaContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModules().size());
    }

    static SettableSchemaProvider<ASTSchemaSource> getImmediateYangSourceProviderFromResource(final String resourceName)
            throws Exception {
        final ResourceYangSource yangSource = new ResourceYangSource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToASTTransformer.transformText(yangSource),
            ASTSchemaSource.class);
    }
}
