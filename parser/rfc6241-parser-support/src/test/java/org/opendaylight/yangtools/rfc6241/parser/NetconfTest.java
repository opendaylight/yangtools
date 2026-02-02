/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesEffectiveStatement;
import org.opendaylight.yangtools.rfc6241.model.api.NetconfConstants;
import org.opendaylight.yangtools.rfc6241.parser.dagger.Rfc6241Module;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class NetconfTest {
    private static final QName FILTER = QName.create(NetconfConstants.RFC6241_MODULE, "filter");

    @Test
    void testResolution() throws Exception {
        final var reactor = RFC7950Reactors.defaultReactorBuilder()
            .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
                Rfc6241Module.provideParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
            .build();
        final var context = assertDoesNotThrow(() -> reactor.newBuild(YangIRSourceModule.provideTextToIR())
            .addLibSource(new URLYangTextSource(NetconfTest.class.getResource("/ietf-inet-types@2013-07-15.yang")))
            .addSource(new URLYangTextSource(NetconfTest.class.getResource("/ietf-netconf@2011-06-01.yang")))
            .buildEffective());

        final var module = context.findModule(NetconfConstants.RFC6241_MODULE).orElseThrow();
        final var rpcs = module.getRpcs();
        assertEquals(13, rpcs.size());
        final var it = module.getRpcs().iterator();
        // get-config
        assertExtension(true, it.next());
        assertExtension(false, it.next());
        assertExtension(false, it.next());
        assertExtension(false, it.next());
        assertExtension(false, it.next());
        assertExtension(false, it.next());
        // get
        assertExtension(true, it.next());
        it.forEachRemaining(def -> assertExtension(false, def));
    }

    private static void assertExtension(final boolean expected, final RpcDefinition def) {
        final var filter = def.asEffectiveStatement().findDataTreeNode(AnyxmlEffectiveStatement.class, FILTER)
            .orElseThrow();
        assertThat(filter.findFirstEffectiveSubstatement(GetFilterElementAttributesEffectiveStatement.class))
            .isPresent();
    }
}
