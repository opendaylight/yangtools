/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesSchemaNode;
import org.opendaylight.yangtools.rfc6241.model.api.NetconfConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

class NetconfTest {
    private static final QName FILTER = QName.create(NetconfConstants.RFC6241_MODULE, "filter");

    @Test
    void testResolution() throws Exception {
        final var reactor = RFC7950Reactors.defaultReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new GetFilterElementAttributesStatementSupport(YangParserConfiguration.DEFAULT))
            .build();
        final var context = reactor.newBuild()
            .addLibSources(YangStatementStreamSource.create(
                YangTextSource.forResource(NetconfTest.class, "/ietf-inet-types@2013-07-15.yang")))
            .addSource(YangStatementStreamSource.create(
                YangTextSource.forResource(NetconfTest.class, "/ietf-netconf@2011-06-01.yang")))
            .buildEffective();

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
        final var optFilter = def.getInput().findDataTreeChild(FILTER);
        assertEquals(expected, optFilter.isPresent());
        optFilter.ifPresent(filter -> {
            final var anyxmlFilter = assertInstanceOf(AnyxmlSchemaNode.class, filter);
            assertTrue(GetFilterElementAttributesSchemaNode.findIn(anyxmlFilter).isPresent());
        });
    }
}
