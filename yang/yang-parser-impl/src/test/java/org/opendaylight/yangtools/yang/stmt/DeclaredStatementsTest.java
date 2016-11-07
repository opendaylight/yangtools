/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AnyXmlEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AugmentEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ChoiceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ContainerEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ModuleEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.SubmoduleEffectiveStatementImpl;

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

    @Test
    public void testDeclaredChoice() throws ReactorException {
        YangStatementSourceImpl choiceStmtModule =
                new YangStatementSourceImpl("/declared-statements-test/choice-declared-test.yang", false);

        SchemaContext schemaContext = StmtTestUtils.parseYangSources(choiceStmtModule);
        assertNotNull(schemaContext);

        Module testModule = schemaContext.findModuleByName("choice-declared-test", null);
        assertNotNull(testModule);

        ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-choice"));
        assertNotNull(choiceSchemaNode);
        ChoiceStatement choiceStatement = ((ChoiceEffectiveStatementImpl) choiceSchemaNode).getDeclared();

        QName name = choiceStatement.getName();
        assertNotNull(name);

        DefaultStatement defaultStatement = choiceStatement.getDefault();
        assertNotNull(defaultStatement);
        assertEquals("case-two", defaultStatement.getValue());

        ConfigStatement configStatement = choiceStatement.getConfig();
        assertNotNull(configStatement);

        MandatoryStatement mandatoryStatement = choiceStatement.getMandatory();
        assertNotNull(mandatoryStatement);

        Collection<? extends CaseStatement> caseStatements = choiceStatement.getCases();
        assertNotNull(caseStatements);
        assertEquals(3, caseStatements.size());
        CaseStatement caseStatement = caseStatements.iterator().next();
        QName caseStatementName = caseStatement.getName();
        assertNotNull(caseStatementName);
        WhenStatement caseStatementWhen = caseStatement.getWhenStatement();
        assertNotNull(caseStatementWhen);
        Collection<? extends IfFeatureStatement> caseStatementIfFeatures = caseStatement.getIfFeatures();
        assertNotNull(caseStatementIfFeatures);
        assertEquals(1, caseStatementIfFeatures.size());
        Collection<? extends DataDefinitionStatement> caseStatementDataDefinitions = caseStatement.getDataDefinitions();
        assertNotNull(caseStatementDataDefinitions);
        assertEquals(1, caseStatementDataDefinitions.size());
        StatusStatement caseStatementStatus = caseStatement.getStatus();
        assertNotNull(caseStatementStatus);
        DescriptionStatement caseStatementDescription = caseStatement.getDescription();
        assertNotNull(caseStatementDescription);
        ReferenceStatement caseStatementReference = caseStatement.getReference();
        assertNotNull(caseStatementReference);

        WhenStatement whenStatement = choiceStatement.getWhenStatement();
        assertNotNull(whenStatement);

        Collection<? extends IfFeatureStatement> ifFeatureStatements = choiceStatement.getIfFeatures();
        assertNotNull(ifFeatureStatements);
        assertEquals(1, ifFeatureStatements.size());

        StatusStatement statusStatement = choiceStatement.getStatus();
        assertNotNull(statusStatement);

        DescriptionStatement descriptionStatement = choiceStatement.getDescription();
        assertNotNull(descriptionStatement);

        ReferenceStatement referenceStatement = choiceStatement.getReference();
        assertNotNull(referenceStatement);
    }

    @Test
    public void testDeclaredAugment() throws ReactorException {
        YangStatementSourceImpl augmentStmtModule =
                new YangStatementSourceImpl("/declared-statements-test/augment-declared-test.yang", false);

        SchemaContext schemaContext = StmtTestUtils.parseYangSources(augmentStmtModule);
        assertNotNull(schemaContext);

        Module testModule = schemaContext.findModuleByName("augment-declared-test", null);
        assertNotNull(testModule);

        Set<AugmentationSchema> augmentationSchemas = testModule.getAugmentations();
        assertNotNull(augmentationSchemas);
        assertEquals(1, augmentationSchemas.size());

        AugmentationSchema augmentationSchema = augmentationSchemas.iterator().next();
        AugmentStatement augmentStatement = ((AugmentEffectiveStatementImpl) augmentationSchema).getDeclared();

        SchemaNodeIdentifier targetNode = augmentStatement.getTargetNode();
        assertNotNull(targetNode);

        Collection<? extends DataDefinitionStatement> augmentStatementDataDefinitions =
                augmentStatement.getDataDefinitions();
        assertNotNull(augmentStatementDataDefinitions);
        assertEquals(1, augmentStatementDataDefinitions.size());
    }

    @Test
    public void testDeclaredModuleAndSubmodule() throws ReactorException {
        YangStatementSourceImpl parentModule =
                new YangStatementSourceImpl("/declared-statements-test/parent-module-declared-test.yang", false);

        YangStatementSourceImpl childModule =
                new YangStatementSourceImpl("/declared-statements-test/child-module-declared-test.yang", false);

        SchemaContext schemaContext = StmtTestUtils.parseYangSources(parentModule, childModule);
        assertNotNull(schemaContext);

        Module testModule = schemaContext.findModuleByName("parent-module-declared-test", null);
        assertNotNull(testModule);

        ModuleStatement moduleStatement = ((ModuleEffectiveStatementImpl) testModule).getDeclared();

        String moduleStatementName = moduleStatement.getName();
        assertNotNull(moduleStatementName);

        YangVersionStatement moduleStatementYangVersion = moduleStatement.getYangVersion();
        assertNotNull(moduleStatementYangVersion);
        assertNotNull(moduleStatementYangVersion.getValue());

        NamespaceStatement moduleStatementNamspace = moduleStatement.getNamespace();
        assertNotNull(moduleStatementNamspace);
        assertNotNull(moduleStatementNamspace.getUri());

        PrefixStatement moduleStatementPrefix= moduleStatement.getPrefix();
        assertNotNull(moduleStatementPrefix);
        assertNotNull(moduleStatementPrefix.getValue());

        assertEquals(1, moduleStatement.getIncludes().size());
        IncludeStatement includeStatement = moduleStatement.getIncludes().iterator().next();
        assertEquals("child-module-declared-test", includeStatement.getModule());

        Set<Module> submodules = testModule.getSubmodules();
        assertNotNull(submodules);
        assertEquals(1, submodules.size());

        Module submodule = submodules.iterator().next();
        SubmoduleStatement submoduleStatement = ((SubmoduleEffectiveStatementImpl) submodule).getDeclared();

        String submoduleStatementName = submoduleStatement.getName();
        assertNotNull(submoduleStatementName);

        YangVersionStatement submoduleStatementYangVersion = submoduleStatement.getYangVersion();
        assertNotNull(submoduleStatementYangVersion);

        BelongsToStatement belongsToStatement = submoduleStatement.getBelongsTo();
        assertNotNull(belongsToStatement);
        assertNotNull(belongsToStatement.getModule());
        assertNotNull(belongsToStatement.getPrefix());
    }

    @Test
    public void testDeclaredModule() throws ReactorException, ParseException {
        YangStatementSourceImpl rootModule =
                new YangStatementSourceImpl("/declared-statements-test/root-module-declared-test.yang", false);

        YangStatementSourceImpl importedModule =
                new YangStatementSourceImpl("/declared-statements-test/imported-module-declared-test.yang", false);

        SchemaContext schemaContext = StmtTestUtils.parseYangSources(rootModule, importedModule);
        assertNotNull(schemaContext);

        Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2016-09-28");

        Module testModule = schemaContext.findModuleByName("root-module-declared-test", revision);
        assertNotNull(testModule);

        ModuleStatement moduleStatement = ((ModuleEffectiveStatementImpl) testModule).getDeclared();

        assertEquals(1, moduleStatement.getImports().size());
        ImportStatement importStatement = moduleStatement.getImports().iterator().next();
        assertEquals("imported-module-declared-test", importStatement.getModule());
        assertEquals("imdt", importStatement.getPrefix().getValue());
        assertEquals(revision, importStatement.getRevisionDate().getDate());

        assertEquals("test description", moduleStatement.getDescription().getText());
        assertEquals("test reference", moduleStatement.getReference().getText());
        assertEquals("test organization", moduleStatement.getOrganization().getText());
        assertEquals("test contact", moduleStatement.getContact().getText());

        assertEquals(1, moduleStatement.getRevisions().size());
        RevisionStatement revisionStatement = moduleStatement.getRevisions().iterator().next();
        assertEquals(revision, revisionStatement.getDate());
        assertEquals("test description", revisionStatement.getDescription().getText());
        assertEquals("test reference", revisionStatement.getReference().getText());

        assertEquals(1, moduleStatement.getExtensions().size());
        ExtensionStatement extensionStatement = moduleStatement.getExtensions().iterator().next();
        assertEquals(Status.CURRENT, extensionStatement.getStatus().getValue());
        assertEquals("test description", extensionStatement.getDescription().getText());
        assertEquals("test reference", extensionStatement.getReference().getText());
        ArgumentStatement argumentStatement = extensionStatement.getArgument();
        assertEquals("ext-argument", argumentStatement.getName().getLocalName());
        assertTrue(argumentStatement.getYinElement().getValue());

        assertEquals(1, moduleStatement.getFeatures().size());
        FeatureStatement featureStatement = moduleStatement.getFeatures().iterator().next();
        assertEquals(Status.CURRENT, featureStatement.getStatus().getValue());
        assertEquals("test description", featureStatement.getDescription().getText());
        assertEquals("test reference", featureStatement.getReference().getText());
        assertEquals("test-feature", featureStatement.getName().getLocalName());
        assertEquals(1, featureStatement.getIfFeatures().size());

        assertEquals(2, moduleStatement.getIdentities().size());
        IdentityStatement identityStatement = null;
        for (IdentityStatement identity : moduleStatement.getIdentities()) {
            if (identity.getName().getLocalName().equals("test-id")) {
                identityStatement = identity;
            }
        }

        assertEquals("test-base-id", identityStatement.getBases().iterator().next().getName().getLocalName());
        assertEquals(Status.CURRENT, identityStatement.getStatus().getValue());
        assertEquals("test description", identityStatement.getDescription().getText());
        assertEquals("test reference", identityStatement.getReference().getText());
        assertEquals("test-id", identityStatement.getName().getLocalName());

        assertEquals(1, moduleStatement.getTypedefs().size());
        TypedefStatement typedefStatement = moduleStatement.getTypedefs().iterator().next();
        assertEquals(Status.CURRENT, typedefStatement.getStatus().getValue());
        assertEquals("test description", typedefStatement.getDescription().getText());
        assertEquals("test reference", typedefStatement.getReference().getText());
        assertEquals("test-typedef", typedefStatement.getName().getLocalName());
        assertEquals("int32", typedefStatement.getType().getName());
        assertEquals("meter", typedefStatement.getUnits().getName());
    }

    @Test
    public void testDeclaredContainer() throws ReactorException {
        YangStatementSourceImpl containerStmtModule =
                new YangStatementSourceImpl("/declared-statements-test/container-declared-test.yang", false);

        SchemaContext schemaContext = StmtTestUtils.parseYangSources(containerStmtModule);
        assertNotNull(schemaContext);

        Module testModule = schemaContext.findModuleByName("container-declared-test", null);
        assertNotNull(testModule);

        ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container"));
        assertNotNull(containerSchemaNode);
        ContainerStatement containerStatement = ((ContainerEffectiveStatementImpl) containerSchemaNode).getDeclared();

        QName name = containerStatement.getName();
        assertNotNull(name);

        WhenStatement containerStatementWhen = containerStatement.getWhenStatement();
        assertNotNull(containerStatementWhen);

        Collection<? extends IfFeatureStatement> containerStatementIfFeatures = containerStatement.getIfFeatures();
        assertNotNull(containerStatementIfFeatures);
        assertEquals(1, containerStatementIfFeatures.size());

        Collection<? extends MustStatement> containerStatementMusts = containerStatement.getMusts();
        assertNotNull(containerStatementMusts);
        assertEquals(1, containerStatementMusts.size());

        PresenceStatement containerStatementPresence = containerStatement.getPresence();
        assertNotNull(containerStatementPresence);
        assertNotNull(containerStatementPresence.getValue());

        ConfigStatement containerStatementConfig = containerStatement.getConfig();
        assertNotNull(containerStatementConfig);

        StatusStatement containerStatementStatus = containerStatement.getStatus();
        assertNotNull(containerStatementStatus);

        DescriptionStatement containerStatementDescription = containerStatement.getDescription();
        assertNotNull(containerStatementDescription);

        ReferenceStatement containerStatementReference = containerStatement.getReference();
        assertNotNull(containerStatementReference);

        Collection<? extends TypedefStatement> containerStatementTypedefs = containerStatement.getTypedefs();
        assertNotNull(containerStatementTypedefs);
        assertEquals(1, containerStatementTypedefs.size());

        Collection<? extends GroupingStatement> containerStatementGroupings = containerStatement.getGroupings();
        assertNotNull(containerStatementGroupings);
        assertEquals(1, containerStatementGroupings.size());

        Collection<? extends DataDefinitionStatement> containerStatementDataDefinitions =
                containerStatement.getDataDefinitions();

        assertNotNull(containerStatementDataDefinitions);
        assertEquals(1, containerStatementDataDefinitions.size());
    }
}
