/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class Bug8307Test {

    private static final StatementStreamSource FOO_MODULE = sourceForResource("/bugs/bug8307/foo.yang");
    private static final StatementStreamSource BAR_MODULE = sourceForResource("/bugs/bug8307/bar.yang");
    private static final StatementStreamSource BAZ_MODULE = sourceForResource("/bugs/bug8307/baz.yang");
    private static final StatementStreamSource FOOBAR_MODULE = sourceForResource("/bugs/bug8307/foobar.yang");
    private static final StatementStreamSource FOO_INVALID_MODULE = sourceForResource("/bugs/bug8307/foo-invalid.yang");
    private static final StatementStreamSource BAR_INVALID_MODULE = sourceForResource("/bugs/bug8307/bar-invalid.yang");
    private static final StatementStreamSource BAZ_INVALID_MODULE = sourceForResource("/bugs/bug8307/baz-invalid.yang");

    private static final URI FOO_NS = URI.create("foo-ns");
    private static final URI BAR_NS = URI.create("bar-ns");
    private static final URI BAZ_NS = URI.create("baz-ns");

    private static final Revision REVISION = Revision.of("2017-05-16");
    private static QNameModule foo;
    private static QName myFooContA;
    private static QName myFooContB;
    private static QName myFooContC;
    private static QNameModule bar;
    private static QName myBarContA;
    private static QName myBarContB;
    private static QNameModule baz;
    private static QName myBazCont;

    @BeforeClass
    public static void setup() {
        foo = QNameModule.create(FOO_NS, REVISION);
        myFooContA = QName.create(foo, "my-foo-cont-a");
        myFooContB = QName.create(foo, "my-foo-cont-b");
        myFooContC = QName.create(foo, "my-foo-cont-c");
        bar = QNameModule.create(BAR_NS, REVISION);
        myBarContA = QName.create(bar, "my-bar-cont-a");
        myBarContB = QName.create(bar, "my-bar-cont-b");
        baz = QNameModule.create(BAZ_NS, REVISION);
        myBazCont = QName.create(baz, "my-baz-cont");
    }

    @Test
    public void testDeviationsSupportedInSomeModules() throws Exception {
        final Map<QNameModule, Set<QNameModule>> modulesWithSupportedDeviations = ImmutableMap.of(
                foo, ImmutableSet.of(bar, baz), bar, ImmutableSet.of(baz));

        final CrossSourceStatementReactor.BuildAction reactor = YangParserFactoryImpl.defaultParser();
        reactor.addSources(FOO_MODULE, BAR_MODULE, BAZ_MODULE, FOOBAR_MODULE);
        reactor.setModulesWithSupportedDeviations(modulesWithSupportedDeviations);

        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooContA)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooContB)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooContC)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarContA)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarContB)));
    }

    @Test
    public void testDeviationsSupportedInAllModules() throws Exception {
        final CrossSourceStatementReactor.BuildAction reactor = YangParserFactoryImpl.defaultParser();
        reactor.addSources(FOO_MODULE, BAR_MODULE, BAZ_MODULE, FOOBAR_MODULE);

        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooContA)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooContB)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooContC)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarContA)));
        assertNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarContB)));
    }

    @Test
    public void testDeviationsSupportedInNoModule() throws Exception {
        final CrossSourceStatementReactor.BuildAction reactor = YangParserFactoryImpl.defaultParser();
        reactor.addSources(FOO_MODULE, BAR_MODULE, BAZ_MODULE, FOOBAR_MODULE);
        reactor.setModulesWithSupportedDeviations(ImmutableMap.of());

        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooContA)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooContB)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myFooContC)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarContA)));
        assertNotNull(SchemaContextUtil.findDataSchemaNode(schemaContext, SchemaPath.create(true, myBarContB)));
    }

    @Test
    public void shouldFailOnAttemptToDeviateTheSameModule() {
        final CrossSourceStatementReactor.BuildAction reactor = YangParserFactoryImpl.defaultParser();
        reactor.addSources(FOO_INVALID_MODULE);

        try {
            reactor.buildEffective();
            fail("Deviation that targets the same module as the one it is defined is forbidden.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith(
                    "Deviation must not target the same module as the one it is defined in"));
        }
    }

    @Test
    public void shouldFailOnAttemptToDeviateTheSameModule2() {
        final CrossSourceStatementReactor.BuildAction reactor = YangParserFactoryImpl.defaultParser();
        reactor.addSources(BAR_INVALID_MODULE, BAZ_INVALID_MODULE);

        try {
            reactor.buildEffective();
            fail("Deviation that targets the same module as the one it is defined is forbidden.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith(
                    "Deviation must not target the same module as the one it is defined in"));
        }
    }
}
