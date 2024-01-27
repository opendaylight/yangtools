/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

class MountPointTest {
    private static final QNameModule EXAMPLE_USES = QNameModule.of("http://example.org/example-uses");
    private static final QName EXAMPLE_CONT = QName.create(EXAMPLE_USES, "cont");
    private static final QName EXAMPLE_GRP = QName.create(EXAMPLE_USES, "grp");
    private static final QName EXAMPLE_GRP_CONT = QName.create(EXAMPLE_USES, "grp-cont");
    private static final QName EXAMPLE_LIST = QName.create(EXAMPLE_USES, "list");

    @Test
    void testMountPointResolution() throws Exception {
        final var reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new MountPointStatementSupport(YangParserConfiguration.DEFAULT))
            .build();

        final var context = reactor.newBuild()
            .addLibSources(
                YangStatementStreamSource.create(
                    new URLYangTextSource(MountPointTest.class.getResource("/ietf-inet-types@2013-07-15.yang"))),
                YangStatementStreamSource.create(
                    new URLYangTextSource(MountPointTest.class.getResource("/ietf-yang-schema-mount@2019-01-14.yang"))),
                YangStatementStreamSource.create(
                    new URLYangTextSource(MountPointTest.class.getResource("/ietf-yang-types@2013-07-15.yang"))))
            .addSources(
                YangStatementStreamSource.create(
                    new URLYangTextSource(MountPointTest.class.getResource("/example-grp.yang"))),
                YangStatementStreamSource.create(
                    new URLYangTextSource(MountPointTest.class.getResource("/example-uses.yang"))))
            .buildEffective();

        assertEquals(5, context.getModules().size());

        var child = context.findDataTreeChild(EXAMPLE_CONT).orElseThrow();
        var mps = MountPointSchemaNode.streamAll(assertInstanceOf(ContainerSchemaNode.class, child)).toList();
        assertEquals(2, mps.size());
        assertEquals(EXAMPLE_CONT, mps.get(0).getQName());
        assertEquals(EXAMPLE_CONT, mps.get(1).getQName());

        child = context.findDataTreeChild(EXAMPLE_GRP_CONT).orElseThrow();
        mps = MountPointSchemaNode.streamAll(assertInstanceOf(ContainerSchemaNode.class, child)).toList();
        assertEquals(1, mps.size());
        assertEquals(EXAMPLE_GRP, mps.get(0).getQName());

        child = context.findDataTreeChild(EXAMPLE_LIST).orElseThrow();
        mps = MountPointSchemaNode.streamAll(assertInstanceOf(ListSchemaNode.class, child)).toList();
        assertEquals(1, mps.size());
        assertEquals(EXAMPLE_LIST, mps.get(0).getQName());
    }
}
