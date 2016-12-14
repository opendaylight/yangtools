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

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.DescriptionStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.IncludeStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ModuleStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ReferenceStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6874Test {

    private static final YangStatementSourceImpl ROOT_MODULE = new YangStatementSourceImpl
            ("/rfc7950/include-import-stmt-test/valid-11/root-module.yang", false);
    private static final YangStatementSourceImpl CHILD_MODULE = new YangStatementSourceImpl
            ("/rfc7950/include-import-stmt-test/valid-11/child-module.yang", false);
    private static final YangStatementSourceImpl CHILD_MODULE_1 = new YangStatementSourceImpl
            ("/rfc7950/include-import-stmt-test/valid-11/child-module-1.yang", false);
    private static final YangStatementSourceImpl IMPORTED_MODULE = new YangStatementSourceImpl
            ("/rfc7950/include-import-stmt-test/valid-11/imported-module.yang", false);

    @Test
    public void valid11Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/rfc7950/include-import-stmt-test/valid-11");
        assertNotNull(schemaContext);

        // Test for valid include statement
        final Module testModule = schemaContext.findModuleByName("root-module", null);
        assertNotNull(testModule);

        // Test for valid import statement
        ModuleImport importStmt = testModule.getImports().iterator().next();
        assertEquals("Yang 1.1: Allow description and reference in include and import.", importStmt.getDescription());
        assertEquals("https://tools.ietf.org/html/rfc7950 section-7.1.5/6", importStmt.getReference());
    }

    @Test
    public void invalid10IncludeStmtTest() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/rfc7950/include-import-stmt-test/invalid-include-10");
            fail("Test must fail: DESCRIPTION/REFERENCE are not valid for INCLUDE in Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            final String msg = e.getCause().getMessage();
            assertTrue(msg.startsWith("DESCRIPTION is not valid for INCLUDE") || msg.startsWith("REFERENCE is not valid for INCLUDE"));
        }
    }

    @Test
    public void invalid10ImportStmtTest() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/rfc7950/include-import-stmt-test/invalid-import-10");
            fail("Test must fail: DESCRIPTION/REFERENCE are not valid for IMPORT in Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            final String msg = e.getCause().getMessage();
            assertTrue(msg.startsWith("DESCRIPTION is not valid for IMPORT") || msg.startsWith("REFERENCE is not valid for IMPORT"));
        }
    }

    @Test
    public void descriptionAndReferenceTest11() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_MODULE, CHILD_MODULE, CHILD_MODULE_1, IMPORTED_MODULE);

        reactor.build().getRootStatements().forEach(declaredStmt -> {
            if(declaredStmt instanceof ModuleStatementImpl) {
                declaredStmt.declaredSubstatements().forEach(subStmt -> {
                    if (subStmt instanceof IncludeStatementImpl &&
                            subStmt.rawArgument().equals("child-module")) {
                        subStmt.declaredSubstatements().forEach(includeSubStmt -> { verifyDescAndRef(includeSubStmt); });
                    }
                });
            }
        });
    }

    @SuppressWarnings("rawtypes")
    private void verifyDescAndRef (DeclaredStatement stmt) {
        if (stmt instanceof DescriptionStatementImpl) {
            assertEquals("Yang 1.1: Allow description and reference in include and import.", ((DescriptionStatementImpl) stmt).argument());
        }
        if (stmt instanceof ReferenceStatementImpl) {
            assertEquals("https://tools.ietf.org/html/rfc7950 section-7.1.5/6", ((ReferenceStatementImpl) stmt).argument());
        }
    }
}
