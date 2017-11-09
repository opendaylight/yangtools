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

import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
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
    private static final StatementStreamSource BAZ_MODULE = sourceForResource(
            "/yang-data-extension-test/baz.yang");
    private static final StatementStreamSource FOOBAR_MODULE = sourceForResource(
            "/yang-data-extension-test/foobar.yang");
    private static final StatementStreamSource IETF_RESTCONF_MODULE = sourceForResource(
            "/yang-data-extension-test/ietf-restconf.yang");

    private static final Revision REVISION = Revision.of("2017-06-01");
    private static final QNameModule FOO_QNAMEMODULE = QNameModule.create(URI.create("foo"), REVISION);
    private static final QName MY_YANG_DATA_A = QName.create(FOO_QNAMEMODULE, "my-yang-data-a");
    private static final QName MY_YANG_DATA_B = QName.create(FOO_QNAMEMODULE, "my-yang-data-b");

    @Test
    public void testYangData() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(FOO_MODULE, IETF_RESTCONF_MODULE);
        assertNotNull(schemaContext);

        final Set<ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(1, extensions.size());

        final Module foo = schemaContext.findModule(FOO_QNAMEMODULE).get();
        final List<UnknownSchemaNode> unknownSchemaNodes = foo.getUnknownSchemaNodes();
        assertEquals(2, unknownSchemaNodes.size());

        YangDataSchemaNode myYangDataANode = null;
        YangDataSchemaNode myYangDataBNode = null;
        for (final UnknownSchemaNode unknownSchemaNode : unknownSchemaNodes) {
            assertTrue(unknownSchemaNode instanceof YangDataSchemaNode);
            final YangDataSchemaNode yangDataSchemaNode = (YangDataSchemaNode) unknownSchemaNode;
            if (MY_YANG_DATA_A.equals(yangDataSchemaNode.getQName())) {
                myYangDataANode = yangDataSchemaNode;
            } else if (MY_YANG_DATA_B.equals(yangDataSchemaNode.getQName())) {
                myYangDataBNode = yangDataSchemaNode;
            }
        }

        assertNotNull(myYangDataANode);
        assertNotNull(myYangDataBNode);

        assertNotNull(myYangDataANode.getContainer());
        assertNotNull(myYangDataBNode.getContainer());
    }

    @Test
    public void testConfigStatementBeingIgnoredInYangDataBody() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(BAZ_MODULE, IETF_RESTCONF_MODULE);
        assertNotNull(schemaContext);

        final Module baz = schemaContext.findModule("baz", REVISION).get();
        final List<UnknownSchemaNode> unknownSchemaNodes = baz.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
        assertTrue(unknownSchemaNode instanceof YangDataSchemaNode);
        final YangDataSchemaNode myYangDataNode = (YangDataSchemaNode) unknownSchemaNode;
        assertNotNull(myYangDataNode);

        final ContainerSchemaNode contInYangData = myYangDataNode.getContainer();
        assertNotNull(contInYangData);
        assertTrue(contInYangData.isConfiguration());
        final ContainerSchemaNode innerCont = (ContainerSchemaNode) contInYangData.getDataChildByName(
                QName.create(baz.getQNameModule(), "inner-cont"));
        assertNotNull(innerCont);
        assertTrue(innerCont.isConfiguration());
        final ContainerSchemaNode grpCont = (ContainerSchemaNode) contInYangData.getDataChildByName(
                QName.create(baz.getQNameModule(), "grp-cont"));
        assertNotNull(grpCont);
        assertTrue(grpCont.isConfiguration());
    }

    @Test
    public void testIfFeatureStatementBeingIgnoredInYangDataBody() throws Exception {
        final SchemaContext schemaContext = DefaultReactors.defaultReactor().newBuild()
                .setSupportedFeatures(ImmutableSet.of())
                .addSources(FOOBAR_MODULE, IETF_RESTCONF_MODULE)
                .buildEffective();
        assertNotNull(schemaContext);

        final Module foobar = schemaContext.findModule("foobar", REVISION).get();
        final List<UnknownSchemaNode> unknownSchemaNodes = foobar.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
        assertTrue(unknownSchemaNode instanceof YangDataSchemaNode);
        final YangDataSchemaNode myYangDataNode = (YangDataSchemaNode) unknownSchemaNode;
        assertNotNull(myYangDataNode);

        final ContainerSchemaNode contInYangData = myYangDataNode.getContainer();
        assertNotNull(contInYangData);
        final ContainerSchemaNode innerCont = (ContainerSchemaNode) contInYangData.getDataChildByName(
                QName.create(foobar.getQNameModule(), "inner-cont"));
        assertNotNull(innerCont);
        final ContainerSchemaNode grpCont = (ContainerSchemaNode) contInYangData.getDataChildByName(
                QName.create(foobar.getQNameModule(), "grp-cont"));
        assertNotNull(grpCont);
    }

    @Test
    public void testYangDataBeingIgnored() throws Exception {
        // yang-data statement is ignored if it does not appear as a top-level statement
        // i.e., it will not appear in the final SchemaContext
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(BAR_MODULE, IETF_RESTCONF_MODULE);
        assertNotNull(schemaContext);

        final Module bar = schemaContext.findModule("bar", REVISION).get();
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
