/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
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
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(
            "/rfc7950/include-import-stmt-test/valid-11");
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
    public void invalid10IncludeStmtTest() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/rfc7950/include-import-stmt-test/invalid-include-10");
            fail("Test must fail: DESCRIPTION/REFERENCE are not valid for INCLUDE in Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            final String msg = e.getCause().getMessage();
            assertTrue(msg.startsWith("DESCRIPTION is not valid for INCLUDE")
                || msg.startsWith("REFERENCE is not valid for INCLUDE"));
        }
    }

    @Test
    public void invalid10ImportStmtTest() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/rfc7950/include-import-stmt-test/invalid-import-10");
            fail("Test must fail: DESCRIPTION/REFERENCE are not valid for IMPORT in Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            final String msg = e.getCause().getMessage();
            assertTrue(msg.startsWith("DESCRIPTION is not valid for IMPORT")
                || msg.startsWith("REFERENCE is not valid for IMPORT"));
        }
    }

    @Test
    public void descriptionAndReferenceTest11() throws ReactorException {
        DefaultReactors.defaultReactor().newBuild()
            .addSources(ROOT_MODULE, CHILD_MODULE, CHILD_MODULE_1, IMPORTED_MODULE)
            .build().getRootStatements().forEach(declaredStmt -> {
                if (declaredStmt instanceof ModuleStatement) {
                    declaredStmt.declaredSubstatements().forEach(subStmt -> {
                        if (subStmt instanceof IncludeStatement && subStmt.rawArgument().equals("child-module")) {
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
