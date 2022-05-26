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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

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
        final var schemaContext = REACTOR.newBuild().addSources(FOO_MODULE, IETF_RESTCONF_MODULE).buildEffective();
        assertNotNull(schemaContext);

        final var extensions = schemaContext.getExtensions();
        assertEquals(1, extensions.size());

        final var foo = schemaContext.findModuleStatement(FOO_QNAMEMODULE).orElseThrow();
        assertEquals(7, foo.effectiveSubstatements().size());

        final var yangDatas = foo.streamEffectiveSubstatements(YangDataEffectiveStatement.class)
            .collect(Collectors.toUnmodifiableList());
        assertEquals(2, yangDatas.size());
        assertEquals("my-yang-data-a", yangDatas.get(0).argument());
        assertEquals("my-yang-data-b", yangDatas.get(1).argument());
    }

    @Test
    public void testConfigStatementBeingIgnoredInYangDataBody() throws Exception {
        final var schemaContext = REACTOR.newBuild().addSources(BAZ_MODULE, IETF_RESTCONF_MODULE).buildEffective();
        assertNotNull(schemaContext);

        final var baz = schemaContext.findModule("baz", REVISION).orElseThrow().asEffectiveStatement();
        final var substatements = baz.effectiveSubstatements();
        assertEquals(6, substatements.size());

        final var myYangData = baz.findFirstEffectiveSubstatement(YangDataEffectiveStatement.class).orElseThrow();
        assertEquals("my-yang-data", myYangData.argument());

        final var contInMyYangData = myYangData.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class)
            .orElseThrow();
        assertThat(contInMyYangData, instanceOf(ContainerSchemaNode.class));

        final var contInYangData = (ContainerSchemaNode) contInMyYangData;
        assertEquals(QName.create(baz.localQNameModule(), "cont"), contInYangData.getQName());

        assertEquals(Optional.empty(), contInYangData.effectiveConfig());
        final var innerCont = (ContainerSchemaNode) contInYangData.getDataChildByName(
            QName.create(baz.localQNameModule(), "inner-cont"));
        assertEquals(Optional.empty(), innerCont.effectiveConfig());
        final var grpCont = (ContainerSchemaNode) contInYangData.getDataChildByName(
            QName.create(baz.localQNameModule(), "grp-cont"));
        assertEquals(Optional.empty(), grpCont.effectiveConfig());
    }

    @Test
    public void testIfFeatureStatementBeingIgnoredInYangDataBody() throws Exception {
        final var schemaContext = REACTOR.newBuild()
            .setSupportedFeatures(Set.of())
            .addSources(FOOBAR_MODULE, IETF_RESTCONF_MODULE)
            .buildEffective();
        assertNotNull(schemaContext);

        final var foobar = schemaContext.findModule("foobar", REVISION).orElseThrow().asEffectiveStatement();
        final var myYangDataNode = foobar.findFirstEffectiveSubstatement(YangDataEffectiveStatement.class)
            .orElseThrow();
        assertEquals("my-yang-data", myYangDataNode.argument());

        final var contInYangData = myYangDataNode.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class)
            .orElseThrow();
        assertThat(contInYangData, instanceOf(ContainerSchemaNode.class));
        final var cont = (ContainerSchemaNode) contInYangData;
        assertEquals(QName.create(foobar.localQNameModule(), "cont"), cont.getQName());
        assertThat(cont.getDataChildByName(QName.create(foobar.localQNameModule(), "inner-cont")),
            instanceOf(ContainerSchemaNode.class));
        assertThat(cont.getDataChildByName(QName.create(foobar.localQNameModule(), "grp-cont")),
            instanceOf(ContainerSchemaNode.class));
    }

    @Test
    public void testYangDataBeingIgnored() throws Exception {
        // yang-data statement is ignored if it does not appear as a top-level statement
        // i.e., it will not appear in the final SchemaContext
        final var schemaContext = REACTOR.newBuild().addSources(BAR_MODULE, IETF_RESTCONF_MODULE).buildEffective();
        assertNotNull(schemaContext);

        final var bar = schemaContext.findModule("bar", REVISION).orElseThrow();
        final var cont = (ContainerSchemaNode) bar.getDataChildByName(QName.create(bar.getQNameModule(), "cont"));
        assertNotNull(cont);

        final var extensions = schemaContext.getExtensions();
        assertEquals(1, extensions.size());

        final var unknownSchemaNodes = cont.getUnknownSchemaNodes();
        assertEquals(0, unknownSchemaNodes.size());
    }

    @Test
    public void testYangDataWithMissingTopLevelContainer() {
        final var build = REACTOR.newBuild().addSources(FOO_INVALID_1_MODULE, IETF_RESTCONF_MODULE);
        final var cause = assertThrows(ReactorException.class, () -> build.buildEffective()).getCause();
        assertThat(cause, instanceOf(MissingSubstatementException.class));
        assertThat(cause.getMessage(), startsWith("yang-data requires at least one substatement [at "));
    }

    @Test
    public void testYangDataWithTwoTopLevelContainers() {
        final var build = REACTOR.newBuild().addSources(FOO_INVALID_2_MODULE, IETF_RESTCONF_MODULE);
        final var cause = assertThrows(ReactorException.class, () -> build.buildEffective()).getCause();
        assertThat(cause, instanceOf(InvalidSubstatementException.class));
        assertThat(cause.getMessage(),
            startsWith("yang-data requires exactly one container data node definition, found ["));
    }
}
