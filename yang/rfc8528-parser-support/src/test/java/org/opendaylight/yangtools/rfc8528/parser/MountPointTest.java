/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class MountPointTest {
    private static final QNameModule EXAMPLE_USES = QNameModule.create(URI.create("http://example.org/example-uses"));
    private static final QName EXAMPLE_CONT = QName.create(EXAMPLE_USES, "cont");
    private static final QName EXAMPLE_GRP = QName.create(EXAMPLE_USES, "grp");
    private static final QName EXAMPLE_GRP_CONT = QName.create(EXAMPLE_USES, "grp-cont");
    private static final QName EXAMPLE_LIST = QName.create(EXAMPLE_USES, "list");

    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, MountPointStatementSupport.getInstance())
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void testMountPointResolution() throws ReactorException, IOException, YangSyntaxErrorException {
        final SchemaContext context = reactor.newBuild()
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

        DataSchemaNode child = context.findDataTreeChild(EXAMPLE_CONT).get();
        assertThat(child, instanceOf(ContainerSchemaNode.class));
        List<MountPointSchemaNode> mps = MountPointSchemaNode.streamAll((ContainerSchemaNode) child)
                .collect(Collectors.toList());
        assertEquals(2, mps.size());
        assertEquals(EXAMPLE_CONT, mps.get(0).getQName());
        assertEquals(EXAMPLE_CONT, mps.get(1).getQName());

        child = context.findDataTreeChild(EXAMPLE_GRP_CONT).get();
        assertThat(child, instanceOf(ContainerSchemaNode.class));
        mps = MountPointSchemaNode.streamAll((ContainerSchemaNode) child).collect(Collectors.toList());
        assertEquals(1, mps.size());
        assertEquals(EXAMPLE_GRP, mps.get(0).getQName());

        child = context.findDataTreeChild(EXAMPLE_LIST).get();
        assertThat(child, instanceOf(ListSchemaNode.class));
        mps = MountPointSchemaNode.streamAll((ListSchemaNode) child).collect(Collectors.toList());
        assertEquals(1, mps.size());
        assertEquals(EXAMPLE_LIST, mps.get(0).getQName());
    }
}
