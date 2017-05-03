/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;

public class Bug8307Test {

    private static final URI FOO_NS = URI.create("foo-ns");
    private static final URI BAR_NS = URI.create("bar-ns");
    private static final URI BAZ_NS = URI.create("baz-ns");

    private static Date revision;
    private static QNameModule foo;
    private static QName myFooCont;
    private static QNameModule bar;
    private static QName myBarCont;
    private static QNameModule baz;
    private static QName myBazCont;

    @BeforeClass
    public static void setup() throws ParseException {
        revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-04-26");
        foo = QNameModule.create(FOO_NS, revision);
        myFooCont = QName.create(foo, "my-foo-cont");
        bar = QNameModule.create(BAR_NS, revision);
        myBarCont = QName.create(bar, "my-bar-cont");
        baz = QNameModule.create(BAZ_NS, revision);
        myBazCont = QName.create(baz, "my-baz-cont");
    }

    @Test
    public void testDeviationsSupportedInSomeModules() throws Exception {
        final Set<QNameModule> modulesWithSupportedDeviations = ImmutableSet.of(foo, bar);

        final StatementParserConfiguration parserConfig = new StatementParserConfiguration.Builder(
                StatementParserMode.DEFAULT_MODE)
                .setModulesWithSupportedDeviations(Optional.of(modulesWithSupportedDeviations)).build();

        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-deviations-test");

        final SettableSchemaProvider<ASTSchemaSource> foo =
                getImmediateYangSourceProviderFromResource("/bugs/bug8307/foo.yang");
        foo.register(sharedSchemaRepository);
        foo.setResult();
        final SettableSchemaProvider<ASTSchemaSource> bar =
                getImmediateYangSourceProviderFromResource("/bugs/bug8307/bar.yang");
        bar.register(sharedSchemaRepository);
        bar.setResult();
        final SettableSchemaProvider<ASTSchemaSource> baz =
                getImmediateYangSourceProviderFromResource("/bugs/bug8307/baz.yang");
        baz.register(sharedSchemaRepository);
        baz.setResult();

        final SchemaContextFactory fact = sharedSchemaRepository
                .createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final CheckedFuture<SchemaContext, SchemaResolutionException> testSchemaContextFuture =
                fact.createSchemaContext(Lists.newArrayList(foo.getId(), bar.getId(), baz.getId()), parserConfig);
        assertTrue(testSchemaContextFuture.isDone());

        final SchemaContext schemaContext = testSchemaContextFuture.checkedGet();
        assertSchemaContext(schemaContext, 3);

        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooCont)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarCont)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBazCont)));
    }

    @Test
    public void testDeviationsSupportedInAllModules() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-deviations-test");

        final SettableSchemaProvider<ASTSchemaSource> foo =
                getImmediateYangSourceProviderFromResource("/bugs/bug8307/foo.yang");
        foo.register(sharedSchemaRepository);
        foo.setResult();
        final SettableSchemaProvider<ASTSchemaSource> bar =
                getImmediateYangSourceProviderFromResource("/bugs/bug8307/bar.yang");
        bar.register(sharedSchemaRepository);
        bar.setResult();
        final SettableSchemaProvider<ASTSchemaSource> baz =
                getImmediateYangSourceProviderFromResource("/bugs/bug8307/baz.yang");
        baz.register(sharedSchemaRepository);
        baz.setResult();

        final SchemaContextFactory fact = sharedSchemaRepository
                .createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final CheckedFuture<SchemaContext, SchemaResolutionException> testSchemaContextFuture =
                fact.createSchemaContext(Lists.newArrayList(foo.getId(), bar.getId(), baz.getId()));
        assertTrue(testSchemaContextFuture.isDone());

        final SchemaContext schemaContext = testSchemaContextFuture.checkedGet();
        assertSchemaContext(schemaContext, 3);

        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooCont)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarCont)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBazCont)));
    }

    @Test
    public void testDeviationsSupportedInNoModule() throws Exception {
        final Set<QNameModule> modulesWithSupportedDeviations = ImmutableSet.of();

        final StatementParserConfiguration parserConfig = new StatementParserConfiguration.Builder(
                StatementParserMode.DEFAULT_MODE)
                .setModulesWithSupportedDeviations(Optional.of(modulesWithSupportedDeviations)).build();

        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-deviations-test");

        final SettableSchemaProvider<ASTSchemaSource> foo =
                getImmediateYangSourceProviderFromResource("/bugs/bug8307/foo.yang");
        foo.register(sharedSchemaRepository);
        foo.setResult();
        final SettableSchemaProvider<ASTSchemaSource> bar =
                getImmediateYangSourceProviderFromResource("/bugs/bug8307/bar.yang");
        bar.register(sharedSchemaRepository);
        bar.setResult();
        final SettableSchemaProvider<ASTSchemaSource> baz =
                getImmediateYangSourceProviderFromResource("/bugs/bug8307/baz.yang");
        baz.register(sharedSchemaRepository);
        baz.setResult();

        final SchemaContextFactory fact = sharedSchemaRepository
                .createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);

        final CheckedFuture<SchemaContext, SchemaResolutionException> testSchemaContextFuture =
                fact.createSchemaContext(Lists.newArrayList(foo.getId(), bar.getId(), baz.getId()), parserConfig);
        assertTrue(testSchemaContextFuture.isDone());

        final SchemaContext schemaContext = testSchemaContextFuture.checkedGet();
        assertSchemaContext(schemaContext, 3);

        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooCont)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarCont)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBazCont)));
    }

    private static SettableSchemaProvider<ASTSchemaSource> getImmediateYangSourceProviderFromResource(
            final String resourceName) throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToASTTransformer.transformText(yangSource),
                ASTSchemaSource.class);
    }

    private static void assertSchemaContext(final SchemaContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModules().size());
    }
}
