/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.rfc8040.parser.dagger.Rfc8040Module;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.StringYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

abstract class AbstractYangDataTest {
    private static final YangTextToIRSourceTransformer TRANSFORMER = YangIRSourceModule.provideTextToIR();
    private static final YangIRSource IETF_RESTCONF_MODULE =
        sourceForResource("/yang-data-extension-test/ietf-restconf.yang");

    private static CrossSourceStatementReactor REACTOR;

    @BeforeAll
    static void createReactor() {
        REACTOR = RFC7950Reactors.vanillaReactorBuilder()
            .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
                Rfc8040Module.provideParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
            .build();
    }

    @AfterAll
    static void freeReactor() {
        REACTOR = null;
    }

    static final @NonNull BuildAction newBuild() throws Exception {
        return REACTOR.newBuild().addSource(IETF_RESTCONF_MODULE);
    }

    static final @NonNull YangIRSource sourceForYangText(final String yangText) {
        return assertDoesNotThrow(() -> TRANSFORMER.transformSource(
            new StringYangTextSource(SourceIdentifier.ofYangFileName("dummy.yang"), yangText)));
    }

    static final @NonNull YangIRSource sourceForResource(final String resourceName) {
        return assertDoesNotThrow(() -> TRANSFORMER.transformSource(
            new URLYangTextSource(AbstractYangDataTest.class.getResource(resourceName))));
    }
}
