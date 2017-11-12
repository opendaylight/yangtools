/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.openconfigver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class OpenconfigVersionTest {
    @Test
    public void basicTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/basic",
            StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModules(new URI("foo")).iterator().next();
        Module bar = context.findModules(new URI("bar")).iterator().next();
        Module semVer = context.findModules(new URI("http://openconfig.net/yang/openconfig-ext")).iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void basicTest2() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/basic-2",
            StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModules(new URI("foo")).iterator().next();
        Module bar = context.findModules(new URI("bar")).iterator().next();
        Module semVer = context.findModules(new URI("http://openconfig.net/yang/openconfig-ext")).iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void basicTest3() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/basic-3",
            StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModules(new URI("foo")).iterator().next();
        Module semVer = context.findModules(new URI("http://openconfig.net/yang/openconfig-ext")).iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
    }

    @Test
    public void basicImportTest1() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/basic-import-1",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModules(new URI("foo")).iterator().next();
        Module semVer = context.findModules(new URI("http://openconfig.net/yang/openconfig-ext")).iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void multipleModulesTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/multiple-modules",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModules(new URI("foo")).iterator().next();
        Module semVer = context.findModules(new URI("http://openconfig.net/yang/openconfig-ext")).iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("0.10.4"), bar.getSemanticVersion().get());
    }

    @Test
    public void basicImportErrTest1() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/openconfig-version/basic-import-invalid-1",
                StatementParserMode.SEMVER_MODE);
            fail("Test should fail due to invalid openconfig version");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getCause().getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(0.1.2)]."));
        }
    }

    @Test
    public void basicImportErrTest2() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/openconfig-version/basic-import-invalid-2",
                StatementParserMode.SEMVER_MODE);
            fail("Test should fail due to invalid openconfig version");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getCause().getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(0.1.2)]."));
        }
    }

    @Test
    public void nodeTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/node-test",
            StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModules(new URI("foo")).iterator().next();
        Module semVer = context.findModules(new URI("http://openconfig.net/yang/openconfig-ext")).iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("2016.1.1"), foo.getSemanticVersion().get());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("2016.4.6"), bar.getSemanticVersion().get());

        QName root = QName.create("foo", "2016-01-01", "foo-root");
        QName container20160404 = QName.create("foo", "2016-01-01", "con20160404");
        SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, root, container20160404));
        assertTrue(findDataSchemaNode instanceof ContainerSchemaNode);

        QName container20160405 = QName.create("foo", "2016-01-01", "con20160405");
        findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, root, container20160405));
        assertTrue(findDataSchemaNode instanceof ContainerSchemaNode);

        QName container20160406 = QName.create("foo", "2016-01-01", "con20160406");
        findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, root, container20160406));
        assertTrue(findDataSchemaNode instanceof ContainerSchemaNode);

        QName container20170406 = QName.create("foo", "2016-01-01", "con20170406");
        findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, root, container20170406));
        assertNull(findDataSchemaNode);
    }
}
