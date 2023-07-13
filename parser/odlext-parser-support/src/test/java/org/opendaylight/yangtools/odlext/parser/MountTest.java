/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.odlext.model.api.MountEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class MountTest {
    private static final QNameModule FOO = QNameModule.create(XMLNamespace.of("foo"));

    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new MountStatementSupport(YangParserConfiguration.DEFAULT))
            .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void test() throws Exception {
        final ModuleEffectiveStatement foo = reactor.newBuild()
            .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/yang-ext.yang")))
            .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/mount.yang")))
            .buildEffective()
            .getModuleStatements()
            .get(FOO);

        final Optional<MountEffectiveStatement> fooMount = foo.findDataTreeNode(QName.create(FOO, "foo")).orElseThrow()
            .findFirstEffectiveSubstatement(MountEffectiveStatement.class);
        assertTrue(fooMount.isPresent());

        final Optional<MountEffectiveStatement> barMount = foo.findDataTreeNode(QName.create(FOO, "bar")).orElseThrow()
            .findFirstEffectiveSubstatement(MountEffectiveStatement.class);
        assertTrue(barMount.isPresent());

    }
}
