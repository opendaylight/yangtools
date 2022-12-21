/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class IncludeResolutionTest {
    private static final StatementStreamSource ROOT = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/root-module.yang");
    private static final StatementStreamSource SUBMODULE1 = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/submodule-1.yang");
    private static final StatementStreamSource SUBMODULE2 = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/submodule-2.yang");
    private static final StatementStreamSource ERROR_MODULE = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/error-module.yang");
    private static final StatementStreamSource ERROR_SUBMODULE = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/error-submodule.yang");
    private static final StatementStreamSource ERROR_SUBMODULE_ROOT = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/error-submodule-root.yang");
    private static final StatementStreamSource MISSING_PARENT_MODULE = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/missing-parent.yang");

    @Test
    void includeTest() throws ReactorException {
        var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(ROOT, SUBMODULE1, SUBMODULE2)
            .build();
        assertNotNull(result);
    }

    @Test
    void missingIncludedSourceTest() {
        var reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(ERROR_MODULE);
        assertNull(assertFailedSourceLinkage(reactor::build, "Included submodule 'foo' was not found [at ").getCause());
    }

    @Test
    void missingIncludedSourceTest2() {
        var reactor = RFC7950Reactors.defaultReactor().newBuild().addSources(ERROR_SUBMODULE, ERROR_SUBMODULE_ROOT);
        var cause = assertFailedSourceLinkage(reactor::build, "Included submodule 'foo' was not found [at ");
        assertNull(cause.getCause());
    }

    @Test
    void missingIncludedSourceTest3() throws SourceException, ReactorException {
        var reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(MISSING_PARENT_MODULE);
        assertNull(assertFailedSourceLinkage(reactor::build,
            "Module 'Unqualified{localName=foo}' from belongs-to was not found [at ").getCause());
    }

    private static InferenceException assertFailedSourceLinkage(final Callable<?> callable, final String startStr) {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class, callable::call);
        assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, ex.getPhase());
        var cause = assertInstanceOf(InferenceException.class, ex.getCause());
        assertThat(cause.getMessage(), startsWith(startStr));
        return cause;
    }
}
