/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

public class YangDataExtensionTest extends AbstractYangDataTest {

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

    private static final Revision REVISION = Revision.of("2017-06-01");
    private static final QNameModule FOO_QNAMEMODULE = QNameModule.create(XMLNamespace.of("foo"), REVISION);
    private static final QName MY_YANG_DATA_A = QName.create(FOO_QNAMEMODULE, "my-yang-data-a");
    private static final QName MY_YANG_DATA_B = QName.create(FOO_QNAMEMODULE, "my-yang-data-b");

    @Test
    public void testYangData() throws Exception {
        final SchemaContext schemaContext = REACTOR.newBuild().addSources(FOO_MODULE, IETF_RESTCONF_MODULE)
                .buildEffective();
        assertNotNull(schemaContext);

        final Collection<? extends ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(1, extensions.size());

        final Module foo = schemaContext.findModule(FOO_QNAMEMODULE).get();
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = foo.getUnknownSchemaNodes();
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

        assertNotNull(myYangDataANode.getContainerSchemaNode());
        assertNotNull(myYangDataBNode.getContainerSchemaNode());
    }

    @Test
    public void testConfigStatementBeingIgnoredInYangDataBody() throws Exception {
        final SchemaContext schemaContext = REACTOR.newBuild().addSources(BAZ_MODULE, IETF_RESTCONF_MODULE)
                .buildEffective();
        assertNotNull(schemaContext);

        final Module baz = schemaContext.findModule("baz", REVISION).get();
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = baz.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
        assertTrue(unknownSchemaNode instanceof YangDataSchemaNode);
        final YangDataSchemaNode myYangDataNode = (YangDataSchemaNode) unknownSchemaNode;
        assertNotNull(myYangDataNode);

        final ContainerSchemaNode contInYangData = myYangDataNode.getContainerSchemaNode();
        assertNotNull(contInYangData);
        assertEquals(Optional.empty(), contInYangData.effectiveConfig());
        final ContainerSchemaNode innerCont = (ContainerSchemaNode) contInYangData.findDataChildByName(
                QName.create(baz.getQNameModule(), "inner-cont")).get();
        assertNotNull(innerCont);
        assertEquals(Optional.empty(), innerCont.effectiveConfig());
        final ContainerSchemaNode grpCont = (ContainerSchemaNode) contInYangData.findDataChildByName(
                QName.create(baz.getQNameModule(), "grp-cont")).get();
        assertNotNull(grpCont);
        assertEquals(Optional.empty(), grpCont.effectiveConfig());
    }

    @Test
    public void testIfFeatureStatementBeingIgnoredInYangDataBody() throws Exception {
        final SchemaContext schemaContext = REACTOR.newBuild().setSupportedFeatures(ImmutableSet.of())
                .addSources(FOOBAR_MODULE, IETF_RESTCONF_MODULE).buildEffective();
        assertNotNull(schemaContext);

        final Module foobar = schemaContext.findModule("foobar", REVISION).get();
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = foobar.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
        assertTrue(unknownSchemaNode instanceof YangDataSchemaNode);
        final YangDataSchemaNode myYangDataNode = (YangDataSchemaNode) unknownSchemaNode;
        assertNotNull(myYangDataNode);

        final ContainerSchemaNode contInYangData = myYangDataNode.getContainerSchemaNode();
        assertNotNull(contInYangData);
        final ContainerSchemaNode innerCont = (ContainerSchemaNode) contInYangData.findDataChildByName(
                QName.create(foobar.getQNameModule(), "inner-cont")).get();
        assertNotNull(innerCont);
        final ContainerSchemaNode grpCont = (ContainerSchemaNode) contInYangData.findDataChildByName(
                QName.create(foobar.getQNameModule(), "grp-cont")).get();
        assertNotNull(grpCont);
    }

    @Test
    public void testYangDataBeingIgnored() throws Exception {
        // yang-data statement is ignored if it does not appear as a top-level statement
        // i.e., it will not appear in the final SchemaContext
        final SchemaContext schemaContext = REACTOR.newBuild().addSources(BAR_MODULE, IETF_RESTCONF_MODULE)
                .buildEffective();
        assertNotNull(schemaContext);

        final Module bar = schemaContext.findModule("bar", REVISION).get();
        final ContainerSchemaNode cont = (ContainerSchemaNode) bar.findDataChildByName(
                QName.create(bar.getQNameModule(), "cont")).get();
        assertNotNull(cont);

        final Collection<? extends ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(1, extensions.size());

        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = cont.getUnknownSchemaNodes();
        assertEquals(0, unknownSchemaNodes.size());
    }

    @Test
    public void testYangDataWithMissingTopLevelContainer() {
        final BuildAction build = REACTOR.newBuild().addSources(FOO_INVALID_1_MODULE, IETF_RESTCONF_MODULE);
        final ReactorException ex = assertThrows(ReactorException.class, () -> build.buildEffective());
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(MissingSubstatementException.class));
        assertThat(cause.getMessage(), startsWith("yang-data requires exactly one container"));
    }

    @Test
    public void testYangDataWithTwoTopLevelContainers() {
        final BuildAction build = REACTOR.newBuild().addSources(FOO_INVALID_2_MODULE, IETF_RESTCONF_MODULE);
        final ReactorException ex = assertThrows(ReactorException.class, () -> build.buildEffective());
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidSubstatementException.class));
        assertThat(cause.getMessage(), startsWith("yang-data requires exactly one data definition node, found 2"));
    }
}
