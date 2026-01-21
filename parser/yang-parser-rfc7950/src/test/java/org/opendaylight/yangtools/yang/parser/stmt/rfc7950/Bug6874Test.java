/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6874Test extends AbstractYangTest {
    private static final StatementStreamSource ROOT_MODULE = sourceForResource(
        "/rfc7950/include-import-stmt-test/valid-11/root-module.yang");
    private static final StatementStreamSource CHILD_MODULE = sourceForResource(
        "/rfc7950/include-import-stmt-test/valid-11/child-module.yang");
    private static final StatementStreamSource CHILD_MODULE_1 = sourceForResource(
        "/rfc7950/include-import-stmt-test/valid-11/child-module-1.yang");
    private static final StatementStreamSource IMPORTED_MODULE = sourceForResource(
        "/rfc7950/include-import-stmt-test/valid-11/imported-module.yang");

    @Test
    void valid11Test() {
        final var schemaContext = assertEffectiveModelDir("/rfc7950/include-import-stmt-test/valid-11");

        // Test for valid include statement
        final var testModule = schemaContext.findModules("root-module").iterator().next();
        assertNotNull(testModule);

        // Test for valid import statement
        var importStmt = testModule.getImports().iterator().next();
        assertEquals(Optional.of("Yang 1.1: Allow description and reference in include and import."),
            importStmt.getDescription());
        assertEquals(Optional.of("https://tools.ietf.org/html/rfc7950 section-7.1.5/6"), importStmt.getReference());
    }

    @Test
    void invalid10IncludeStmtTest() {
        assertInvalidSubstatementExceptionDir("/rfc7950/include-import-stmt-test/invalid-include-10", anyOf(
            startsWith("include statement does not allow description substatements [at "),
            startsWith("include statement does not allow reference substatements [at ")));
    }

    @Test
    void invalid10ImportStmtTest() {
        assertInvalidSubstatementExceptionDir("/rfc7950/include-import-stmt-test/invalid-import-10", anyOf(
            startsWith("import statement does not allow description substatements [at "),
            startsWith("import statement does not allow reference substatements [at ")));
    }

    @Test
    void descriptionAndReferenceTest11() throws ReactorException {
        RFC7950Reactors.defaultReactor().newBuild()
            .addSources(ROOT_MODULE, CHILD_MODULE, CHILD_MODULE_1, IMPORTED_MODULE)
            .build()
            .getRootStatements()
            .forEach(stmt -> {
                if (stmt instanceof ModuleStatement module) {
                    module.declaredSubstatements().forEach(subStmt -> {
                        if (subStmt instanceof IncludeStatement && "child-module".equals(subStmt.rawArgument())) {
                            subStmt.declaredSubstatements().forEach(Bug6874Test::verifyDescAndRef);
                        }
                    });
                }
            });
    }

    private static void verifyDescAndRef(final DeclaredStatement<?> stmt) {
        if (stmt instanceof DescriptionStatement description) {
            assertEquals("Yang 1.1: Allow description and reference in include and import.", description.argument());
        }
        if (stmt instanceof ReferenceStatement reference) {
            assertEquals("https://tools.ietf.org/html/rfc7950 section-7.1.5/6", reference.argument());
        }
    }
}
