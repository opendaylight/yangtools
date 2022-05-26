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

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
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
            assertThat(unknownSchemaNode, instanceOf(YangDataSchemaNode.class));
            final YangDataSchemaNode yangDataSchemaNode = (YangDataSchemaNode) unknownSchemaNode;
            if ("my-yang-data-a".equals(yangDataSchemaNode.getNodeParameter())) {
                myYangDataANode = yangDataSchemaNode;
            } else if ("my-yang-data-b".equals(yangDataSchemaNode.getNodeParameter())) {
                myYangDataBNode = yangDataSchemaNode;
            }
        }

        assertNotNull(myYangDataANode);
        assertNotNull(myYangDataBNode);
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
        assertThat(unknownSchemaNode, instanceOf(YangDataSchemaNode.class));
        final YangDataSchemaNode myYangDataNode = (YangDataSchemaNode) unknownSchemaNode;
        assertNotNull(myYangDataNode);

        final Collection<? extends DataSchemaNode> yangDataChildren = myYangDataNode.getChildNodes();
        assertEquals(1, yangDataChildren.size());

        final DataSchemaNode childInYangData = yangDataChildren.iterator().next();
        assertThat(childInYangData, instanceOf(ContainerSchemaNode.class));
        final ContainerSchemaNode contInYangData = (ContainerSchemaNode) childInYangData;
        assertEquals(Optional.empty(), contInYangData.effectiveConfig());
        final ContainerSchemaNode innerCont = (ContainerSchemaNode) contInYangData.getDataChildByName(
                QName.create(baz.getQNameModule(), "inner-cont"));
        assertNotNull(innerCont);
        assertEquals(Optional.empty(), innerCont.effectiveConfig());
        final ContainerSchemaNode grpCont = (ContainerSchemaNode) contInYangData.getDataChildByName(
                QName.create(baz.getQNameModule(), "grp-cont"));
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
        assertThat(unknownSchemaNode, instanceOf(YangDataSchemaNode.class));
        final YangDataSchemaNode myYangDataNode = (YangDataSchemaNode) unknownSchemaNode;
        assertNotNull(myYangDataNode);

        final Collection<? extends DataSchemaNode> yangDataChildren = myYangDataNode.getChildNodes();
        assertEquals(1, yangDataChildren.size());

        final DataSchemaNode childInYangData = yangDataChildren.iterator().next();
        assertThat(childInYangData, instanceOf(ContainerSchemaNode.class));
        final ContainerSchemaNode contInYangData = (ContainerSchemaNode) childInYangData;
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
        final SchemaContext schemaContext = REACTOR.newBuild().addSources(BAR_MODULE, IETF_RESTCONF_MODULE)
                .buildEffective();
        assertNotNull(schemaContext);

        final Module bar = schemaContext.findModule("bar", REVISION).get();
        final ContainerSchemaNode cont = (ContainerSchemaNode) bar.getDataChildByName(
                QName.create(bar.getQNameModule(), "cont"));
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
        assertThat(cause.getMessage(), startsWith("yang-data requires at least one substatement [at "));
    }

    @Test
    public void testYangDataWithTwoTopLevelContainers() {
        final BuildAction build = REACTOR.newBuild().addSources(FOO_INVALID_2_MODULE, IETF_RESTCONF_MODULE);
        final ReactorException ex = assertThrows(ReactorException.class, () -> build.buildEffective());
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidSubstatementException.class));
        assertThat(cause.getMessage(),
            startsWith("yang-data requires exactly one container data node definition, found ["));
    }
}
