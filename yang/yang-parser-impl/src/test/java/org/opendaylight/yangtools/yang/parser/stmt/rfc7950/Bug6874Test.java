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

import com.google.common.collect.ImmutableList;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
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
            ("/rfc7950/include-stmt-test/valid-11/root-module.yang", false);
    private static final YangStatementSourceImpl CHILD_MODULE = new YangStatementSourceImpl
            ("/rfc7950/include-stmt-test/valid-11/child-module.yang", false);
    private static final YangStatementSourceImpl CHILD_MODULE_1 = new YangStatementSourceImpl
            ("/rfc7950/include-stmt-test/valid-11/child-module-1.yang", false);

    @Test
    public void valid11Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/rfc7950/include-stmt-test/valid-11");
        assertNotNull(schemaContext);

        final Module testModule = schemaContext.findModuleByName("root-module", null);
        assertNotNull(testModule);
        assertEquals("1.1", testModule.getYangVersion());
    }

    @Test
    public void invalid10Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/rfc7950/include-stmt-test/invalid-10");
            fail("Test must fail: DESCRIPTION/REFERENCE are not valid for INCLUDE in Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith("DESCRIPTION is not valid for INCLUDE") ||
                    e.getCause().getMessage().startsWith("REFERENCE is not valid for INCLUDE"));
        }
    }

    @Test
    public void descriptionAndReferenceTest11() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_MODULE, CHILD_MODULE, CHILD_MODULE_1);

        ImmutableList<DeclaredStatement<?>> rootStmts = reactor.build().getRootStatements();
        rootStmts.forEach(declaredStmt -> {
            if(declaredStmt.getClass().equals(ModuleStatementImpl.class)) {
                declaredStmt.declaredSubstatements().forEach(subStmt -> {
                    if (subStmt.getClass().equals(IncludeStatementImpl.class) &&
                            subStmt.argument().toString().equals("child-module")) {
                        subStmt.declaredSubstatements().forEach(includeSubStmt -> {
                            if (includeSubStmt.getClass().equals(DescriptionStatementImpl.class)) {
                                assertEquals("Test for Yang 1.1 description substatement support", ((DescriptionStatementImpl) includeSubStmt).argument());
                            }
                            if (includeSubStmt.getClass().equals(ReferenceStatementImpl.class)) {
                                assertEquals("RFC 7950: The YANG 1.1 Data Modeling Language", ((ReferenceStatementImpl) includeSubStmt).argument());
                            }
                        });
                    }
                });
            }
        });
    }
}
