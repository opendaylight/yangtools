/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class YangDataExtensionTest extends AbstractYangDataTest {
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
    private static final QNameModule FOO_QNAMEMODULE = QNameModule.of(XMLNamespace.of("foo"), REVISION);

    @Test
    void testYangData() throws Exception {
        final var schemaContext = REACTOR.newBuild().addSources(FOO_MODULE, IETF_RESTCONF_MODULE).buildEffective();
        assertNotNull(schemaContext);

        final var extensions = schemaContext.getExtensions();
        assertEquals(1, extensions.size());

        final var unknownSchemaNodes = schemaContext.findModule(FOO_QNAMEMODULE).orElseThrow().getUnknownSchemaNodes();
        assertEquals(2, unknownSchemaNodes.size());
        final var it = unknownSchemaNodes.iterator();
        assertEquals("my-yang-data-a", assertInstanceOf(YangDataSchemaNode.class, it.next()).getNodeParameter());
        assertEquals("my-yang-data-b", assertInstanceOf(YangDataSchemaNode.class, it.next()).getNodeParameter());
        assertFalse(it.hasNext());
    }

    @Test
    void testConfigStatementBeingIgnoredInYangDataBody() throws Exception {
        final var schemaContext = REACTOR.newBuild().addSources(BAZ_MODULE, IETF_RESTCONF_MODULE).buildEffective();
        assertNotNull(schemaContext);

        final var baz = schemaContext.findModule("baz", REVISION).orElseThrow();
        final var unknownSchemaNodes = baz.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final var myYangDataNode = assertInstanceOf(YangDataSchemaNode.class, unknownSchemaNodes.iterator().next());

        final var yangDataChildren = myYangDataNode.getChildNodes();
        assertEquals(1, yangDataChildren.size());

        final var contInYangData = assertInstanceOf(ContainerSchemaNode.class, yangDataChildren.iterator().next());
        assertEquals(Optional.empty(), contInYangData.effectiveConfig());
        final var innerCont = assertInstanceOf(ContainerSchemaNode.class,
            contInYangData.dataChildByName(QName.create(baz.getQNameModule(), "inner-cont")));
        assertEquals(Optional.empty(), innerCont.effectiveConfig());
        final var grpCont = assertInstanceOf(ContainerSchemaNode.class,
            contInYangData.dataChildByName(QName.create(baz.getQNameModule(), "grp-cont")));
        assertEquals(Optional.empty(), grpCont.effectiveConfig());
    }

    @Test
    void testIfFeatureStatementBeingIgnoredInYangDataBody() throws Exception {
        final var schemaContext = REACTOR.newBuild()
            .setSupportedFeatures(FeatureSet.of())
            .addSources(FOOBAR_MODULE, IETF_RESTCONF_MODULE)
            .buildEffective();
        assertNotNull(schemaContext);

        final var foobar = schemaContext.findModule("foobar", REVISION).orElseThrow();
        final var unknownSchemaNodes = foobar.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final var myYangDataNode = assertInstanceOf(YangDataSchemaNode.class, unknownSchemaNodes.iterator().next());

        final var yangDataChildren = myYangDataNode.getChildNodes();
        assertEquals(1, yangDataChildren.size());

        final var contInYangData = assertInstanceOf(ContainerSchemaNode.class, yangDataChildren.iterator().next());
        assertInstanceOf(ContainerSchemaNode.class,
            contInYangData.dataChildByName(QName.create(foobar.getQNameModule(), "inner-cont")));
        assertInstanceOf(ContainerSchemaNode.class,
            contInYangData.dataChildByName(QName.create(foobar.getQNameModule(), "grp-cont")));
    }

    @Test
    void testYangDataBeingIgnored() throws Exception {
        // yang-data statement is ignored if it does not appear as a top-level statement
        // i.e., it will not appear in the final SchemaContext
        final var schemaContext = REACTOR.newBuild().addSources(BAR_MODULE, IETF_RESTCONF_MODULE).buildEffective();
        assertNotNull(schemaContext);

        final var bar = schemaContext.findModule("bar", REVISION).orElseThrow();
        final var cont = assertInstanceOf(ContainerSchemaNode.class,
            bar.dataChildByName(QName.create(bar.getQNameModule(), "cont")));

        final var extensions = schemaContext.getExtensions();
        assertEquals(1, extensions.size());

        final var unknownSchemaNodes = cont.getUnknownSchemaNodes();
        assertEquals(0, unknownSchemaNodes.size());
    }

    @Test
    void testYangDataWithMissingTopLevelContainer() {
        final var build = REACTOR.newBuild().addSources(FOO_INVALID_1_MODULE, IETF_RESTCONF_MODULE);
        final var ex = assertThrows(ReactorException.class, () -> build.buildEffective());
        final var cause = assertInstanceOf(MissingSubstatementException.class, ex.getCause());
        assertThat(cause.getMessage(), startsWith("yang-data requires at least one substatement [at "));
    }

    @Test
    void testYangDataWithTwoTopLevelContainers() {
        final var build = REACTOR.newBuild().addSources(FOO_INVALID_2_MODULE, IETF_RESTCONF_MODULE);
        final var ex = assertThrows(ReactorException.class, () -> build.buildEffective());
        final var cause = assertInstanceOf(InvalidSubstatementException.class, ex.getCause());
        assertThat(cause.getMessage(),
            startsWith("yang-data requires exactly one container data node definition, found ["));
    }
}
