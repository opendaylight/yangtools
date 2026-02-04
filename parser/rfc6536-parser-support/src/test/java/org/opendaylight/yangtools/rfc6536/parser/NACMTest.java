/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMConstants;
import org.opendaylight.yangtools.rfc6536.parser.dagger.Rfc6536Module;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class NACMTest {
    private static final YangTextToIRSourceTransformer TRANSFORMER = YangIRSourceModule.provideTextToIR();

    @Test
    void testResolution() throws Exception {
        final var reactor = RFC7950Reactors.defaultReactorBuilder()
            .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
                Rfc6536Module.provideParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
            .build();

        final var context = reactor.newBuild()
            .addYangSource(TRANSFORMER, new URLYangTextSource(NACMTest.class.getResource(
                "/ietf-netconf-acm@2012-02-22.yang")))
            .addYangSource(TRANSFORMER, new URLYangTextSource(NACMTest.class.getResource(
                "/ietf-yang-types@2013-07-15.yang")))
            .buildEffective();

        final var module = context.findModule(NACMConstants.RFC6536_MODULE).orElseThrow();
        final var nacm = assertInstanceOf(ContainerSchemaNode.class,
            module.dataChildByName(QName.create(NACMConstants.RFC6536_MODULE, "nacm"))).asEffectiveStatement();
        assertThat(nacm.findFirstEffectiveSubstatement(DefaultDenyAllEffectiveStatement.class)).isPresent();
        assertThat(nacm.findFirstEffectiveSubstatement(DefaultDenyWriteEffectiveStatement.class)).isEmpty();
    }
}
