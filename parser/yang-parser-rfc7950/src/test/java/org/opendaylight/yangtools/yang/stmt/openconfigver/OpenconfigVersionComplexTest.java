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

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

@Deprecated(since = "8.0.4", forRemoval = true)
public class OpenconfigVersionComplexTest {
    private static final YangParserConfiguration SEMVER = YangParserConfiguration.builder()
        .importResolutionMode(ImportResolutionMode.OPENCONFIG_SEMVER)
        .build();

    @Test
    public void complexTest1() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/complex/complex-1",
            SEMVER);
        verifySchemaContextTest1(context);
    }

    @Test
    public void complexTest1Yang1_1() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/complex/complex-1-rfc7950",
            SEMVER);
        verifySchemaContextTest1(context);
    }

    private static void verifySchemaContextTest1(final SchemaContext context) {
        assertNotNull(context);

        final Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        final Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        // check module versions
        assertEquals(SemVer.valueOf("1.3.95"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("1.50.2"), foo.getSemanticVersion().get());

        final Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("1.2.6"), bar.getSemanticVersion().get());

        final Module foobar = StmtTestUtils.findImportedModule(context, bar, "foobar");
        assertEquals(SemVer.valueOf("2.26.465"), foobar.getSemanticVersion().get());

        // check imported components
        assertNotNull("This component should be present", context.findDataTreeChild(
            QName.create(bar.getQNameModule(), "root"),
            QName.create(bar.getQNameModule(), "test-container"),
            QName.create(bar.getQNameModule(), "number")).orElse(null));

        assertNotNull("This component should be present", context.findDataTreeChild(
            QName.create(bar.getQNameModule(), "should-present")).orElse(null));

        // check not imported components
        assertEquals("This component should not be present", Optional.empty(), context.findDataTreeChild(
            QName.create(bar.getQNameModule(), "root"),
            QName.create(bar.getQNameModule(), "test-container"),
            QName.create(bar.getQNameModule(), "oldnumber")));

        assertEquals("This component should not be present", Optional.empty(), context.findDataTreeChild(
            QName.create(bar.getQNameModule(), "should-not-be-present")));
    }

    @Test
    public void complexTest2() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/complex/complex-2", SEMVER);
        verifySchemaContextTest2(context);
    }

    @Test
    public void complexTest2Yang1_1() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/complex/complex-2-rfc7950",
            SEMVER);
        verifySchemaContextTest2(context);
    }

    private static void verifySchemaContextTest2(final SchemaContext context) {
        assertNotNull(context);

        final Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        final Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        // check module versions
        assertEquals(SemVer.valueOf("2.5.50"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("2.32.2"), foo.getSemanticVersion().get());

        final Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("4.9.8"), bar.getSemanticVersion().get());

        final Module foobar = StmtTestUtils.findImportedModule(context, bar, "foobar");
        assertEquals(SemVer.valueOf("7.13.99"), foobar.getSemanticVersion().get());

        // check used augmentations
        assertNotNull("This component should be present", context.findDataTreeChild(
            QName.create(foobar.getQNameModule(), "root"),
            QName.create(foobar.getQNameModule(), "test-container"),
            QName.create(bar.getQNameModule(), "should-present-leaf-1")).orElse(null));

        assertNotNull("This component should be present", context.findDataTreeChild(
            QName.create(foobar.getQNameModule(), "root"),
            QName.create(foobar.getQNameModule(), "test-container"),
            QName.create(bar.getQNameModule(), "should-present-leaf-2")).orElse(null));

        // check not used augmentations
        assertEquals("This component should not be present", Optional.empty(), context.findDataTreeChild(
            QName.create(foobar.getQNameModule(), "root"),
            QName.create(foobar.getQNameModule(), "test-container"),
            QName.create(bar.getQNameModule(), "should-not-be-present-leaf-1")));
        assertEquals("This component should not be present", Optional.empty(), context.findDataTreeChild(
            QName.create(foobar.getQNameModule(), "root"),
            QName.create(foobar.getQNameModule(), "test-container"),
            QName.create(bar.getQNameModule(), "should-not-be-present-leaf-2")));

        // check if correct foobar module was included
        assertNotNull("This component should be present", context.findDataTreeChild(
            QName.create(foobar.getQNameModule(), "root"),
            QName.create(foobar.getQNameModule(), "included-correct-mark")).orElse(null));

        assertEquals("This component should not be present", Optional.empty(), context.findDataTreeChild(
            QName.create(foobar.getQNameModule(), "root"),
            QName.create(foobar.getQNameModule(), "included-not-correct-mark")));
    }
}
