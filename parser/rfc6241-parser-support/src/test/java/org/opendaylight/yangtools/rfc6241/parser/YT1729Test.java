/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.opendaylight.yangtools.rfc6241.model.api.NetconfConstants.RFC6241_MODULE;
import static org.opendaylight.yangtools.rfc6536.model.api.NACMConstants.RFC6536_REVISION;
import static org.opendaylight.yangtools.rfc6536.model.api.NACMConstants.RFC8341_REVISION;
import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_FILE_EXTENSION;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.rfc6241.parser.dagger.Rfc6241Module;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.parser.dagger.Rfc6536Module;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class YT1729Test {
    private static final @NonNull CrossSourceStatementReactor REACTOR = RFC7950Reactors.defaultReactorBuilder()
        .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
            Rfc6241Module.provideParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
        .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
            Rfc6536Module.provideParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
        .build();

    @ParameterizedTest
    @MethodSource
    void nacmImpliesDefaultDenyAll(final Revision expectedRevision, final List<Revision> nacmRevisions) {
        final var build = REACTOR.newBuild(YangIRSourceModule.provideTextToIR());
        for (var nacmRevision : nacmRevisions) {
            assertDoesNotThrow(() ->
                build.addSource(new URLYangTextSource(NetconfTest.class.getResource(
                    "/ietf-netconf-acm@" + nacmRevision + RFC6020_YANG_FILE_EXTENSION))));
        }

        final var context = assertDoesNotThrow(() -> build
            .addLibSource(new URLYangTextSource(NetconfTest.class.getResource("/ietf-inet-types@2013-07-15.yang")))
            .addLibSource(new URLYangTextSource(NetconfTest.class.getResource("/ietf-yang-types@2013-07-15.yang")))
            .addSource(new URLYangTextSource(NetconfTest.class.getResource("/ietf-netconf@2011-06-01.yang")))
            .buildEffective());

        final var module = context.findModuleStatement(RFC6241_MODULE).orElseThrow();
        assertDefaultDenyAll(expectedRevision, module, "delete-config");
        assertDefaultDenyAll(expectedRevision, module, "kill-session");
    }

    private static List<Arguments> nacmImpliesDefaultDenyAll() {
        return List.of(
            arguments(RFC6536_REVISION, List.of(RFC6536_REVISION)),
            arguments(RFC8341_REVISION, List.of(RFC8341_REVISION)),
            arguments(RFC8341_REVISION, List.of(RFC6536_REVISION, RFC8341_REVISION)));
    }

    private static void assertDefaultDenyAll(final Revision expectedRevision, final ModuleEffectiveStatement module,
            final String rpcName) {
        final var rpc = assertInstanceOf(RpcEffectiveStatement.class,
            module.findSchemaTreeNode(QName.create(RFC6241_MODULE, rpcName)).orElseThrow());
        final var defaultDenyAll = rpc.findFirstEffectiveSubstatement(DefaultDenyAllEffectiveStatement.class)
            .orElseThrow();
        assertEquals(expectedRevision, defaultDenyAll.statementDefinition().statementName().getModule().revision());
        assertNull(defaultDenyAll.declared());
    }
}
