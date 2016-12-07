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

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.IncludeStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.stmt.IncludedStmtsTest;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncludedStmtsRfc7950Test extends IncludedStmtsTest {

    final Logger LOG = LoggerFactory.getLogger(IncludedStmtsRfc7950Test.class);
    private static final YangStatementSourceImpl ROOT_MODULE = new YangStatementSourceImpl
            ("/rfc7950/include-stmt-test/root-module.yang", false);
    private static final YangStatementSourceImpl CHILD_MODULE = new YangStatementSourceImpl
            ("/rfc7950/include-stmt-test/child-module.yang", false);

    @Override
    protected YangStatementSourceImpl getRootModule() {
        return ROOT_MODULE;
    }

    @Override
    protected YangStatementSourceImpl getChildModule() {
        return CHILD_MODULE;
    }

    @Test
    public void descriptionAndReferenceTest() throws ReactorException {
        final Module testModule = getEffectiveSchemaContext().findModuleByName("root-module", null);
        assertEquals("1.1", testModule.getYangVersion());
        assertNotNull(testModule);

// FIXME: Update tests to verify the description and reference from Effective schema context
//        DocumentedNode includeNode = (DocumentedNode) testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "child-module"));
//        assertNotNull(includeNode);
//        assertEquals("Test for Yang 1.1 description substatement support", includeNode.getDescription());
//        assertEquals("RFC 7950: The YANG 1.1 Data Modeling Language", includeNode.getReference());
    }
}
