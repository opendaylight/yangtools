/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.tailf.common.parser;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class TailFActionExtensionTest {
    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION, YangVersion.VERSION_1,
                    TailFActionRFC6020StatementSupport.getInstance())
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void test() throws Exception {
        SchemaContext context = reactor.newBuild()
                .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/tailf-common.yang")))
                .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/foo.yang")))
                .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/foo-dev.yang")))
                .buildEffective();
    }
}
