/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class MountPointTest {
    private static final QNameModule EXAMPLE_USES =
        QNameModule.create(XMLNamespace.of("http://example.org/example-uses"));
    private static final QName EXAMPLE_CONT = QName.create(EXAMPLE_USES, "cont");
    private static final QName EXAMPLE_GRP = QName.create(EXAMPLE_USES, "grp");
    private static final QName EXAMPLE_GRP_CONT = QName.create(EXAMPLE_USES, "grp-cont");
    private static final QName EXAMPLE_LIST = QName.create(EXAMPLE_USES, "list");

    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new MountPointStatementSupport(YangParserConfiguration.DEFAULT))
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void testMountPointResolution() throws Exception {
        final var context = reactor.newBuild()
                .addLibSources(
                    YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                            "/ietf-inet-types@2013-07-15.yang")),
                    YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                            "/ietf-yang-schema-mount@2019-01-14.yang")),
                    YangStatementStreamSource.create(YangTextSchemaSource.forResource(
                            "/ietf-yang-types@2013-07-15.yang")))
                .addSources(
                    YangStatementStreamSource.create(YangTextSchemaSource.forResource("/example-grp.yang")),
                    YangStatementStreamSource.create(YangTextSchemaSource.forResource("/example-uses.yang")))
                .buildEffective();

        assertEquals(5, context.getModules().size());

        var child = context.findDataTreeChild(EXAMPLE_CONT).orElseThrow();
        assertThat(child, instanceOf(ContainerSchemaNode.class));
        var mps = MountPointSchemaNode.streamAll((ContainerSchemaNode) child).toList();
        assertEquals(2, mps.size());
        assertEquals(EXAMPLE_CONT, mps.get(0).getQName());
        assertEquals(EXAMPLE_CONT, mps.get(1).getQName());

        child = context.findDataTreeChild(EXAMPLE_GRP_CONT).orElseThrow();
        assertThat(child, instanceOf(ContainerSchemaNode.class));
        mps = MountPointSchemaNode.streamAll((ContainerSchemaNode) child).toList();
        assertEquals(1, mps.size());
        assertEquals(EXAMPLE_GRP, mps.get(0).getQName());

        child = context.findDataTreeChild(EXAMPLE_LIST).orElseThrow();
        assertThat(child, instanceOf(ListSchemaNode.class));
        mps = MountPointSchemaNode.streamAll((ListSchemaNode) child).toList();
        assertEquals(1, mps.size());
        assertEquals(EXAMPLE_LIST, mps.get(0).getQName());
    }
}
