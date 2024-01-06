/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.parser;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagEffectiveStatement;
import org.opendaylight.yangtools.rfc8819.model.api.Tag;
import org.opendaylight.yangtools.yang.model.spi.source.ResourceYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class ModuleTagTest {
    private static CrossSourceStatementReactor reactor;

    @BeforeAll
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                        new ModuleTagStatementSupport(YangParserConfiguration.DEFAULT))
                .build();
    }

    @AfterAll
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void testModuleTagSupportExtension() throws ReactorException {
        final var moduleTags = reactor.newBuild()
            .addSources(
                moduleFromResources("/example-tag-module.yang"),
                moduleFromResources("/ietf-module-tags.yang"),
                moduleFromResources("/ietf-yang-types.yang"),
                moduleFromResources("/ietf-module-tags-state.yang"))
            .buildEffective()
            .getModuleStatements().values().stream()
            .flatMap(module -> module.streamEffectiveSubstatements(ModuleTagEffectiveStatement.class))
            .map(ModuleTagEffectiveStatement::argument)
            .collect(Collectors.toList());

        assertEquals(List.of(
            new Tag("ietf:tag-outside-cntr"),
            new Tag("ietf:some-other-tag"),
            new Tag("ietf:network-element-class"),
            new Tag("vendor:some-other-tag"),
            new Tag("user:some-other-tag"),
            new Tag("different:some-other-tag"),
            new Tag("somestring"),
            new Tag("sometag:somestring")), moduleTags);
    }

    @Test
    public void throwExceptionWhenTagParentIsNotModuleOrSubmodule() {
        final var action = reactor.newBuild()
            .addSources(
                moduleFromResources("/foo-tag-module.yang"),
                moduleFromResources("/ietf-module-tags.yang"),
                moduleFromResources("/ietf-yang-types.yang"),
                moduleFromResources("/ietf-module-tags-state.yang"));

        final var cause = assertThrows(ReactorException.class, action::buildEffective).getCause();
        assertInstanceOf(SourceException.class, cause);
        assertThat(cause.getMessage(),
            startsWith("Tags may only be defined at root of either a module or a submodule [at "));
    }

    private static YangStatementStreamSource moduleFromResources(final String resourceName) {
        try {
            return YangStatementStreamSource.create(new ResourceYangTextSource(ModuleTagTest.class, resourceName));
        } catch (final YangSyntaxErrorException | IOException e) {
            throw new IllegalStateException("Failed to find resource " + resourceName, e);
        }
    }
}
