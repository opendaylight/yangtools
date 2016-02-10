/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AnyXmlEffectiveStatementImpl;

public class DeclaredStatementsTest {

    @Test
    public void testDeclaredAnyXml() throws ReactorException {
        YangStatementSourceImpl anyxmlStmtModule =
                new YangStatementSourceImpl("/declared-statements-test/anyxml-declared-test.yang", false);

        SchemaContext schemaContext = StmtTestUtils.parseYangSources(anyxmlStmtModule);
        assertNotNull(schemaContext);

        Module testModule = schemaContext.findModuleByName("anyxml-declared-test", null);
        assertNotNull(testModule);

        AnyXmlSchemaNode anyxmlSchemaNode = (AnyXmlSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "foobar"));
        assertNotNull(anyxmlSchemaNode);
        AnyxmlStatement anyxmlStatement = ((AnyXmlEffectiveStatementImpl) anyxmlSchemaNode).getDeclared();

        QName name = anyxmlStatement.getName();
        assertNotNull(name);

        WhenStatement whenStatement = anyxmlStatement.getWhenStatement();
        assertNotNull(whenStatement);
        RevisionAwareXPath whenRevisionAwareXPath = whenStatement.getCondition();
        assertNotNull(whenRevisionAwareXPath);
        DescriptionStatement whenStatementDescription = whenStatement.getDescription();
        assertNotNull(whenStatementDescription);
        ReferenceStatement whenStatementReference = whenStatement.getReference();
        assertNotNull(whenStatementReference);

        Collection<? extends IfFeatureStatement> ifFeatureStatements = anyxmlStatement.getIfFeatures();
        assertNotNull(ifFeatureStatements);
        assertEquals(1, ifFeatureStatements.size());
        QName ifFeatureName = ifFeatureStatements.iterator().next().getName();
        assertNotNull(ifFeatureName);

        Collection<? extends MustStatement> mustStatements = anyxmlStatement.getMusts();
        assertNotNull(mustStatements);
        assertEquals(1, mustStatements.size());
        MustStatement mustStatement = mustStatements.iterator().next();
        RevisionAwareXPath mustRevisionAwareXPath = mustStatement.getCondition();
        assertNotNull(mustRevisionAwareXPath);
        ErrorAppTagStatement errorAppTagStatement = mustStatement.getErrorAppTagStatement();
        assertNotNull(errorAppTagStatement);
        ErrorMessageStatement errorMessageStatement = mustStatement.getErrorMessageStatement();
        assertNotNull(errorMessageStatement);
        DescriptionStatement mustStatementDescription = mustStatement.getDescription();
        assertNotNull(mustStatementDescription);
        ReferenceStatement mustStatementReference = mustStatement.getReference();
        assertNotNull(mustStatementReference);

        ConfigStatement configStatement = anyxmlStatement.getConfig();
        assertNotNull(configStatement);
        assertFalse(configStatement.getValue());

        StatusStatement statusStatement = anyxmlStatement.getStatus();
        assertNotNull(statusStatement);
        Status status = statusStatement.getValue();
        assertNotNull(status);

        DescriptionStatement descriptionStatement = anyxmlStatement.getDescription();
        assertNotNull(descriptionStatement);
        assertEquals("anyxml description", descriptionStatement.getText());

        ReferenceStatement referenceStatement = anyxmlStatement.getReference();
        assertNotNull(referenceStatement);
        assertEquals("anyxml reference", referenceStatement.getText());

        MandatoryStatement mandatoryStatement = anyxmlStatement.getMandatory();
        assertNotNull(mandatoryStatement);
    }
}
