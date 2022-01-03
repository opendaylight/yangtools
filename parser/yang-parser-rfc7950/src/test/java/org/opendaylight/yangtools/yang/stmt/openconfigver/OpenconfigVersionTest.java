/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.openconfigver;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class OpenconfigVersionTest {
    private static final YangParserConfiguration SEMVER = YangParserConfiguration.builder()
        .importResolutionMode(ImportResolutionMode.OPENCONFIG_SEMVER)
        .build();

    @Test
    public void basicTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/basic", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module bar = context.findModules(XMLNamespace.of("bar")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void basicTest2() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/basic-2", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module bar = context.findModules(XMLNamespace.of("bar")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void basicTest3() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/basic-3", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
    }

    @Test
    public void basicImportTest1() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/basic-import-1", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void multipleModulesTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/multiple-modules", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("0.10.4"), bar.getSemanticVersion().get());
    }

    @Test
    public void basicImportErrTest1() {
        ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/basic-import-invalid-1", SEMVER));
        assertThat(ex.getCause().getMessage(),
            startsWith("Unable to find module compatible with requested import [bar(0.1.2)]."));
    }

    @Test
    public void basicImportErrTest2() {
        ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/basic-import-invalid-2", SEMVER));
        assertThat(ex.getCause().getMessage(),
            startsWith("Unable to find module compatible with requested import [bar(0.1.2)]."));
    }

    @Test
    public void nodeTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/node-test", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("2016.1.1"), foo.getSemanticVersion().get());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("2016.4.6"), bar.getSemanticVersion().get());

        QName root = QName.create("foo", "2016-01-01", "foo-root");
        QName container20160404 = QName.create("foo", "2016-01-01", "con20160404");
        SchemaNode findDataSchemaNode = context.findDataTreeChild(root, container20160404).orElseThrow();
        assertThat(findDataSchemaNode, instanceOf(ContainerSchemaNode.class));

        QName container20160405 = QName.create("foo", "2016-01-01", "con20160405");
        findDataSchemaNode = context.findDataTreeChild(root, container20160405).orElseThrow();
        assertThat(findDataSchemaNode, instanceOf(ContainerSchemaNode.class));

        QName container20160406 = QName.create("foo", "2016-01-01", "con20160406");
        findDataSchemaNode = context.findDataTreeChild(root, container20160406).orElseThrow();
        assertThat(findDataSchemaNode, instanceOf(ContainerSchemaNode.class));

        QName container20170406 = QName.create("foo", "2016-01-01", "con20170406");
        final Optional<DataSchemaNode> dataTreeChild = context.findDataTreeChild(root, container20170406);
        assertEquals(Optional.empty(), dataTreeChild);
    }
}
