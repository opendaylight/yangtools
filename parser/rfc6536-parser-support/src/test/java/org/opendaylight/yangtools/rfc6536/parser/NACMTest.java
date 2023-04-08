/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllSchemaNode;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteSchemaNode;
import org.opendaylight.yangtools.rfc6536.model.api.NACMConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class NACMTest {
    private static CrossSourceStatementReactor REACTOR;

    @BeforeClass
    public static void createReactor() {
        REACTOR = RFC7950Reactors.defaultReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new DefaultDenyAllStatementSupport(YangParserConfiguration.DEFAULT))
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new DefaultDenyWriteStatementSupport(YangParserConfiguration.DEFAULT))
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        REACTOR = null;
    }

    @Test
    public void testResolution() throws Exception {
        final var context = REACTOR.newBuild()
            .addSources(
                YangStatementStreamSource.create(YangTextSchemaSource.forResource("/ietf-netconf-acm@2012-02-22.yang")),
                YangStatementStreamSource.create(YangTextSchemaSource.forResource("/ietf-yang-types@2013-07-15.yang")))
            .buildEffective();

        final var module = context.findModule(NACMConstants.RFC6536_MODULE).orElseThrow();
        final var nacm = module.getDataChildByName(QName.create(NACMConstants.RFC6536_MODULE, "nacm"));
        assertTrue(DefaultDenyAllSchemaNode.findIn(nacm).isPresent());
        assertFalse(DefaultDenyWriteSchemaNode.findIn(nacm).isPresent());
    }
}
