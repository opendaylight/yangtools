/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6874Test {

    private static final StatementStreamSource ROOT_MODULE = sourceForResource(
        "/rfc7950/include-import-stmt-test/valid-11/root-module.yang");
    private static final StatementStreamSource CHILD_MODULE = sourceForResource(
        "/rfc7950/include-import-stmt-test/valid-11/child-module.yang");
    private static final StatementStreamSource CHILD_MODULE_1 = sourceForResource(
        "/rfc7950/include-import-stmt-test/valid-11/child-module-1.yang");
    private static final StatementStreamSource IMPORTED_MODULE = sourceForResource(
        "/rfc7950/include-import-stmt-test/valid-11/imported-module.yang");

    @Test
    public void valid11Test() throws Exception {
        final var schemaContext = StmtTestUtils.parseYangSources("/rfc7950/include-import-stmt-test/valid-11");
        assertNotNull(schemaContext);

        // Test for valid include statement
        final Module testModule = schemaContext.findModules("root-module").iterator().next();
        assertNotNull(testModule);

        // Test for valid import statement
        ModuleImport importStmt = testModule.getImports().iterator().next();
        assertEquals(Optional.of("Yang 1.1: Allow description and reference in include and import."),
            importStmt.getDescription());
        assertEquals(Optional.of("https://tools.ietf.org/html/rfc7950 section-7.1.5/6"),
            importStmt.getReference());
    }

    @Test
    public void invalid10IncludeStmtTest() {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> StmtTestUtils.parseYangSources("/rfc7950/include-import-stmt-test/invalid-include-10")).getCause();
        assertThat(ex, instanceOf(InvalidSubstatementException.class));
        assertThat(ex.getMessage(), anyOf(
            startsWith("DESCRIPTION is not valid for INCLUDE"),
            startsWith("REFERENCE is not valid for INCLUDE")));
    }

    @Test
    public void invalid10ImportStmtTest() throws Exception {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> StmtTestUtils.parseYangSources("/rfc7950/include-import-stmt-test/invalid-import-10")).getCause();
        assertThat(ex, instanceOf(InvalidSubstatementException.class));
        assertThat(ex.getMessage(), anyOf(
            startsWith("DESCRIPTION is not valid for IMPORT"),
            startsWith("REFERENCE is not valid for IMPORT")));
    }

    @Test
    public void descriptionAndReferenceTest11() throws ReactorException {
        RFC7950Reactors.defaultReactor().newBuild()
            .addSources(ROOT_MODULE, CHILD_MODULE, CHILD_MODULE_1, IMPORTED_MODULE)
            .build().getRootStatements().forEach(declaredStmt -> {
                if (declaredStmt instanceof ModuleStatement) {
                    declaredStmt.declaredSubstatements().forEach(subStmt -> {
                        if (subStmt instanceof IncludeStatement && "child-module".equals(subStmt.rawArgument())) {
                            subStmt.declaredSubstatements().forEach(Bug6874Test::verifyDescAndRef);
                        }
                    });
                }
            });
    }

    @SuppressWarnings("rawtypes")
    private static void verifyDescAndRef(final DeclaredStatement stmt) {
        if (stmt instanceof DescriptionStatement) {
            assertEquals("Yang 1.1: Allow description and reference in include and import.",
                ((DescriptionStatement) stmt).argument());
        }
        if (stmt instanceof ReferenceStatement) {
            assertEquals("https://tools.ietf.org/html/rfc7950 section-7.1.5/6", ((ReferenceStatement) stmt).argument());
        }
    }
}
