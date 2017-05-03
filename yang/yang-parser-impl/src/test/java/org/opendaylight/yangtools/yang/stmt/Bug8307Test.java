/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.ImmutableSet;
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
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class Bug8307Test {

    private static final StatementStreamSource FOO_MODULE = sourceForResource("/bugs/bug8307/foo.yang");
    private static final StatementStreamSource BAR_MODULE = sourceForResource("/bugs/bug8307/bar.yang");
    private static final StatementStreamSource BAZ_MODULE = sourceForResource("/bugs/bug8307/baz.yang");

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
        final CrossSourceStatementReactor.BuildAction reactor =
                YangInferencePipeline.RFC6020_REACTOR.newBuild(parserConfig);
        reactor.addSources(FOO_MODULE, BAR_MODULE, BAZ_MODULE);

        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooCont)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarCont)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBazCont)));
    }

    @Test
    public void testDeviationsSupportedInAllModules() throws Exception {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(FOO_MODULE, BAR_MODULE, BAZ_MODULE);

        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

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
        final CrossSourceStatementReactor.BuildAction reactor =
                YangInferencePipeline.RFC6020_REACTOR.newBuild(parserConfig);
        reactor.addSources(FOO_MODULE, BAR_MODULE, BAZ_MODULE);

        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooCont)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarCont)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBazCont)));
    }
}
