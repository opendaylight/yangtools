/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.FluentFuture;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToASTTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;

public class SchemaContextFactoryDeviationsTest {
    private static final String FOO = "/bug9195/foo.yang";
    private static final String BAR = "/bug9195/bar.yang";
    private static final String BAZ = "/bug9195/baz.yang";
    private static final String FOOBAR = "/bug9195/foobar.yang";
    private static final String BAR_INVALID = "/bug9195/bar-invalid.yang";
    private static final String BAZ_INVALID = "/bug9195/baz-invalid.yang";
    private static final URI FOO_NS = URI.create("foo-ns");
    private static final URI BAR_NS = URI.create("bar-ns");
    private static final URI BAZ_NS = URI.create("baz-ns");
    private static final Revision REVISION = Revision.of("2017-05-16");
    private static final QNameModule FOO_MODULE = QNameModule.create(FOO_NS, REVISION);
    private static final QName MY_FOO_CONT_A = QName.create(FOO_MODULE, "my-foo-cont-a");
    private static final QName MY_FOO_CONT_B = QName.create(FOO_MODULE, "my-foo-cont-b");
    private static final QName MY_FOO_CONT_C = QName.create(FOO_MODULE, "my-foo-cont-c");
    private static final QNameModule BAR_MODULE = QNameModule.create(BAR_NS, REVISION);
    private static final QName MY_BAR_CONT_A = QName.create(BAR_MODULE, "my-bar-cont-a");
    private static final QName MY_BAR_CONT_B = QName.create(BAR_MODULE, "my-bar-cont-b");
    private static final QNameModule BAZ_MODULE = QNameModule.create(BAZ_NS, REVISION);

    @Test
    public void testDeviationsSupportedInSomeModules() throws Exception {
        final SetMultimap<QNameModule, QNameModule> modulesWithSupportedDeviations =
                ImmutableSetMultimap.<QNameModule, QNameModule>builder()
                .put(FOO_MODULE, BAR_MODULE)
                .put(FOO_MODULE, BAZ_MODULE)
                .put(BAR_MODULE, BAZ_MODULE)
                .build();

        final FluentFuture<SchemaContext> lf = createSchemaContext(modulesWithSupportedDeviations, FOO, BAR, BAZ,
            FOOBAR);
        assertTrue(lf.isDone());
        final SchemaContext schemaContext = lf.get();
        assertNotNull(schemaContext);

        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_FOO_CONT_A)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_FOO_CONT_B)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_FOO_CONT_C)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_BAR_CONT_A)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_BAR_CONT_B)));
    }

    @Test
    public void testDeviationsSupportedInAllModules() throws Exception {
        final FluentFuture<SchemaContext> lf = createSchemaContext(null, FOO, BAR, BAZ, FOOBAR);
        assertTrue(lf.isDone());
        final SchemaContext schemaContext = lf.get();
        assertNotNull(schemaContext);

        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_FOO_CONT_A)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_FOO_CONT_B)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_FOO_CONT_C)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_BAR_CONT_A)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_BAR_CONT_B)));
    }

    @Test
    public void testDeviationsSupportedInNoModule() throws Exception {
        final FluentFuture<SchemaContext> lf = createSchemaContext(ImmutableSetMultimap.of(), FOO, BAR, BAZ, FOOBAR);
        assertTrue(lf.isDone());
        final SchemaContext schemaContext = lf.get();
        assertNotNull(schemaContext);

        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_FOO_CONT_A)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_FOO_CONT_B)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_FOO_CONT_C)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_BAR_CONT_A)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, MY_BAR_CONT_B)));
    }

    @Test
    public void shouldFailOnAttemptToDeviateTheSameModule2() throws Exception {
        final FluentFuture<SchemaContext> lf = createSchemaContext(null, BAR_INVALID, BAZ_INVALID);
        assertTrue(lf.isDone());
        try {
            lf.get();
            fail("Deviation that targets the same module as the one it is defined is forbidden.");
        } catch (final ExecutionException ex) {
            final Throwable cause = ex.getCause().getCause().getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage()
                    .startsWith("Deviation must not target the same module as the one it is defined in"));
        }
    }

    private static SettableSchemaProvider<ASTSchemaSource> getImmediateYangSourceProviderFromResource(
            final String resourceName) throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToASTTransformer.transformText(yangSource),
                ASTSchemaSource.class);
    }

    private static FluentFuture<SchemaContext> createSchemaContext(
            final SetMultimap<QNameModule, QNameModule> modulesWithSupportedDeviations, final String... resources)
            throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-with-deviations-test");

        final Collection<SourceIdentifier> requiredSources = new ArrayList<>();
        for (final String resource : resources) {
            final SettableSchemaProvider<ASTSchemaSource> yangSource = getImmediateYangSourceProviderFromResource(
                    resource);
            yangSource.register(sharedSchemaRepository);
            yangSource.setResult();
            requiredSources.add(yangSource.getId());
        }

        final SchemaContextFactoryConfiguration config = SchemaContextFactoryConfiguration.builder()
                .setModulesDeviatedByModules(modulesWithSupportedDeviations).build();
        final SchemaContextFactory fact = sharedSchemaRepository.createSchemaContextFactory(config);

        return fact.createSchemaContext(ImmutableList.copyOf(requiredSources));
    }
}
