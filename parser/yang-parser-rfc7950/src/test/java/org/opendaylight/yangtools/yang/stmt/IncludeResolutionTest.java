/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

class IncludeResolutionTest {
    @Test
    void includeTest() throws Exception {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/semantic-statement-parser/include-arg-parsing/root-module.yang"))
            .addSource(sourceForResource("/semantic-statement-parser/include-arg-parsing/submodule-1.yang"))
            .addSource(sourceForResource("/semantic-statement-parser/include-arg-parsing/submodule-2.yang"))
            .buildDeclared());
    }

    @Test
    void missingIncludedSourceTest() {
        assertNull(assertFailedSourceLinkage(() -> RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/semantic-statement-parser/include-arg-parsing/error-module.yang"))
            .buildDeclared(), "Included submodule foo was not found [at error-module:5:5]")
            .getCause());
    }

    @Test
    void missingIncludedSourceTest2() {
        var cause = assertFailedSourceLinkage(() -> RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/semantic-statement-parser/include-arg-parsing/error-submodule.yang"))
            .addSource(sourceForResource("/semantic-statement-parser/include-arg-parsing/error-submodule-root.yang"))
            .buildDeclared(), "Included submodule foo was not found [at error-submodule:6:5]");
        assertNull(cause.getCause());
    }

    @Test
    void missingIncludedSourceTest3() {
        assertNull(assertFailedSourceLinkage(() -> RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/semantic-statement-parser/include-arg-parsing/missing-parent.yang"))
            .buildDeclared(), "Module foo from belongs-to was not found [at missing-parent:2:5]").getCause());
    }

    private static InferenceException assertFailedSourceLinkage(final Callable<?> callable, final String startStr) {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class, callable::call);
        assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, ex.getPhase());
        var cause = assertInstanceOf(InferenceException.class, ex.getCause());
        assertThat(cause.getMessage()).startsWith(startStr);
        return cause;
    }
}
