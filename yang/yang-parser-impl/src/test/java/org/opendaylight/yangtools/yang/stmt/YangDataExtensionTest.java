/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class YangDataExtensionTest {

    private static final StatementStreamSource FOO_MODULE = sourceForResource(
            "/yang-data-extension-test/foo.yang");
    private static final StatementStreamSource FOO_INVALID_1_MODULE = sourceForResource(
            "/yang-data-extension-test/foo-invalid-1.yang");
    private static final StatementStreamSource FOO_INVALID_2_MODULE = sourceForResource(
            "/yang-data-extension-test/foo-invalid-2.yang");
    private static final StatementStreamSource FOO_INVALID_3_MODULE = sourceForResource(
            "/yang-data-extension-test/foo-invalid-3.yang");
    private static final StatementStreamSource BAR_MODULE = sourceForResource(
            "/yang-data-extension-test/bar.yang");
    private static final StatementStreamSource IETF_RESTCONF_MODULE = sourceForResource(
            "/yang-data-extension-test/ietf-restconf.yang");

    private static Date revision;
    private static QNameModule fooModule;
    private static QName myYangDataA;
    private static QName myYangDataB;

    @BeforeClass
    public static void setup() throws ParseException {
        revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-06-01");
        fooModule = QNameModule.create(URI.create("foo"), revision);
        myYangDataA = QName.create(fooModule, "my-yang-data-a");
        myYangDataB = QName.create(fooModule, "my-yang-data-b");
    }

    @Test
    public void testYangData() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(FOO_MODULE, IETF_RESTCONF_MODULE);
        assertNotNull(schemaContext);

        final Set<ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(1, extensions.size());

        final Module foo = schemaContext.findModuleByName("foo", revision);
        assertNotNull(foo);

        final Set<YangDataSchemaNode> yangDataSchemaNodes = foo.getYangDataSchemaNodes();
        assertEquals(2, yangDataSchemaNodes.size());

        YangDataSchemaNode myYangDataANode = null;
        YangDataSchemaNode myYangDataBNode = null;
        for (final YangDataSchemaNode yangDataSchemaNode : yangDataSchemaNodes) {
            if (myYangDataA.equals(yangDataSchemaNode.getQName())) {
                myYangDataANode = yangDataSchemaNode;
            } else if (myYangDataB.equals(yangDataSchemaNode.getQName())) {
                myYangDataBNode = yangDataSchemaNode;
            }
        }

        assertNotNull(myYangDataANode);
        assertNotNull(myYangDataBNode);

        assertNotNull(myYangDataANode.getContainer());
        assertNotNull(myYangDataBNode.getContainer());
    }

    @Test
    public void testYangDataBeingIgnored() throws Exception {
        // yang-data statement is ignored if it does not appear as a top-level statement
        // i.e., it will not appear in the final SchemaContext
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(BAR_MODULE, IETF_RESTCONF_MODULE);
        assertNotNull(schemaContext);

        final Module bar = schemaContext.findModuleByName("bar", revision);
        assertNotNull(bar);
        final ContainerSchemaNode cont = (ContainerSchemaNode) bar.getDataChildByName(
                QName.create(bar.getQNameModule(), "cont"));
        assertNotNull(cont);

        final Set<ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(1, extensions.size());

        final List<UnknownSchemaNode> unknownSchemaNodes = cont.getUnknownSchemaNodes();
        assertEquals(0, unknownSchemaNodes.size());
    }

    @Test
    public void testYangDataWithMissingTopLevelContainer() {
        try {
            StmtTestUtils.parseYangSources(FOO_INVALID_1_MODULE, IETF_RESTCONF_MODULE);
            fail("Exception should have been thrown because of missing top-level container in yang-data statement.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof MissingSubstatementException);
            assertTrue(cause.getMessage().startsWith("YANG_DATA is missing CONTAINER. Minimal count is 1."));
        }
    }

    @Test
    public void testYangDataWithTwoTopLevelContainers() {
        try {
            StmtTestUtils.parseYangSources(FOO_INVALID_2_MODULE, IETF_RESTCONF_MODULE);
            fail("Exception should have been thrown because of two top-level containers in yang-data statement.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("Maximal count of CONTAINER for YANG_DATA is 1, detected 2."));
        }
    }

    @Test
    public void testYangDataWithInvalidToplevelNode() {
        try {
            StmtTestUtils.parseYangSources(FOO_INVALID_3_MODULE, IETF_RESTCONF_MODULE);
            fail("Exception should have been thrown because of invalid top-level node in yang-data statement.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("LEAF is not valid for YANG_DATA."));
        }
    }
}
