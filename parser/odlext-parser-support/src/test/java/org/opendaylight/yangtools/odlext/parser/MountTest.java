/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.odlext.model.api.MountEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

class MountTest {
    private static final QNameModule FOO = QNameModule.create(XMLNamespace.of("foo"));

    @Test
    void test() throws Exception {
        final var reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new MountStatementSupport(YangParserConfiguration.DEFAULT))
            .build();
        final var foo = reactor.newBuild()
            .addSource(YangStatementStreamSource.create(YangTextSource.forResource(MountTest.class, "/yang-ext.yang")))
            .addSource(YangStatementStreamSource.create(YangTextSource.forResource(MountTest.class, "/mount.yang")))
            .buildEffective()
            .getModuleStatements()
            .get(FOO);

        assertTrue(foo.findDataTreeNode(QName.create(FOO, "foo")).orElseThrow()
            .findFirstEffectiveSubstatement(MountEffectiveStatement.class)
            .isPresent());

        assertTrue(foo.findDataTreeNode(QName.create(FOO, "bar")).orElseThrow()
            .findFirstEffectiveSubstatement(MountEffectiveStatement.class)
            .isPresent());
    }
}
