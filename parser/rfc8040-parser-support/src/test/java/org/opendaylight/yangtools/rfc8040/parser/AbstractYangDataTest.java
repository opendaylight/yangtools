/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

abstract class AbstractYangDataTest {
    static final StatementStreamSource IETF_RESTCONF_MODULE = sourceForResource(
        "/yang-data-extension-test/ietf-restconf.yang");

    static CrossSourceStatementReactor REACTOR;

    @BeforeAll
    static void createReactor() {
        REACTOR = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    new YangDataStatementSupport(YangParserConfiguration.DEFAULT))
                .addNamespaceSupport(ModelProcessingPhase.FULL_DECLARATION, YangDataStatementSupport.BEHAVIOUR)
                .build();
    }

    @AfterAll
    static void freeReactor() {
        REACTOR = null;
    }

    static StatementStreamSource sourceForResource(final String resourceName) {
        try {
            return YangStatementStreamSource.create(
                new URLYangTextSource(AbstractYangDataTest.class.getResource(resourceName)));
        } catch (IOException | YangSyntaxErrorException e) {
            throw new IllegalArgumentException("Failed to create source", e);
        }
    }
}
