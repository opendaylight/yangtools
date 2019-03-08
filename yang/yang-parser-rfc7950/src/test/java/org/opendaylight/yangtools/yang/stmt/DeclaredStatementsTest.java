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
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.text.ParseException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class DeclaredStatementsTest {

    @Test
    public void testDeclaredAnyXml() throws ReactorException {
        final StatementStreamSource anyxmlStmtModule =
                sourceForResource("/declared-statements-test/anyxml-declared-test.yang");

        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(anyxmlStmtModule);
        assertNotNull(schemaContext);

        final Module testModule = schemaContext.findModules("anyxml-declared-test").iterator().next();
        assertNotNull(testModule);

        final AnyXmlSchemaNode anyxmlSchemaNode = (AnyXmlSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "foobar"));
        assertNotNull(anyxmlSchemaNode);
        final AnyxmlStatement anyxmlStatement = ((AnyxmlEffectiveStatement) anyxmlSchemaNode).getDeclared();

        final QName name = anyxmlStatement.getName();
        assertNotNull(name);

        final WhenStatement whenStatement = anyxmlStatement.getWhenStatement().get();
        final RevisionAwareXPath whenRevisionAwareXPath = whenStatement.getCondition();
        assertNotNull(whenRevisionAwareXPath);
        final DescriptionStatement whenStatementDescription = whenStatement.getDescription().get();
        assertTrue(whenStatement.getReference().isPresent());

        final Collection<? extends IfFeatureStatement> ifFeatureStatements = anyxmlStatement.getIfFeatures();
        assertNotNull(ifFeatureStatements);
        assertEquals(1, ifFeatureStatements.size());
        final Predicate<Set<QName>> ifFeaturePredicate = ifFeatureStatements.iterator().next().getIfFeaturePredicate();
        assertNotNull(ifFeaturePredicate);

        final Collection<? extends MustStatement> mustStatements = anyxmlStatement.getMustStatements();
        assertNotNull(mustStatements);
        assertEquals(1, mustStatements.size());
        final MustStatement mustStatement = mustStatements.iterator().next();
        final RevisionAwareXPath mustRevisionAwareXPath = mustStatement.getCondition();
        assertNotNull(mustRevisionAwareXPath);
        assertTrue(mustStatement.getErrorAppTagStatement().isPresent());
        assertTrue(mustStatement.getErrorMessageStatement().isPresent());
        assertTrue(mustStatement.getDescription().isPresent());
        assertTrue(mustStatement.getReference().isPresent());

        final ConfigStatement configStatement = anyxmlStatement.getConfig().get();
        assertFalse(configStatement.getValue());

        final StatusStatement statusStatement = anyxmlStatement.getStatus().get();
        final Status status = statusStatement.getValue();
        assertNotNull(status);

        final DescriptionStatement descriptionStatement = anyxmlStatement.getDescription().get();
        assertEquals("anyxml description", descriptionStatement.getText());

        final ReferenceStatement referenceStatement = anyxmlStatement.getReference().get();
        assertEquals("anyxml reference", referenceStatement.getText());

        assertTrue(anyxmlStatement.getMandatory().isPresent());
    }

    @Test
    public void testDeclaredChoice() throws ReactorException {
        final StatementStreamSource choiceStmtModule =
                sourceForResource("/declared-statements-test/choice-declared-test.yang");

        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(choiceStmtModule);
        assertNotNull(schemaContext);

        final Module testModule = schemaContext.findModules("choice-declared-test").iterator().next();
        assertNotNull(testModule);

        final ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-choice"));
        assertNotNull(choiceSchemaNode);
        final ChoiceStatement choiceStatement = ((ChoiceEffectiveStatement) choiceSchemaNode).getDeclared();

        final QName name = choiceStatement.getName();
        assertNotNull(name);

        final DefaultStatement defaultStatement = choiceStatement.getDefault().get();
        assertEquals("case-two", defaultStatement.getValue());

        assertTrue(choiceStatement.getConfig().isPresent());
        assertTrue(choiceStatement.getMandatory().isPresent());

        final Collection<? extends CaseStatement> caseStatements = choiceStatement.getCases();
        assertNotNull(caseStatements);
        assertEquals(3, caseStatements.size());
        final CaseStatement caseStatement = caseStatements.iterator().next();
        final QName caseStatementName = caseStatement.getName();
        assertNotNull(caseStatementName);
        final WhenStatement caseStatementWhen = caseStatement.getWhenStatement().get();
        final Collection<? extends IfFeatureStatement> caseStatementIfFeatures = caseStatement.getIfFeatures();
        assertNotNull(caseStatementIfFeatures);
        assertEquals(1, caseStatementIfFeatures.size());
        final Collection<? extends DataDefinitionStatement> caseStatementDataDefinitions =
                caseStatement.getDataDefinitions();
        assertNotNull(caseStatementDataDefinitions);
        assertEquals(1, caseStatementDataDefinitions.size());
        assertTrue(caseStatement.getStatus().isPresent());
        assertTrue(caseStatement.getDescription().isPresent());
        assertTrue(caseStatement.getReference().isPresent());

        final WhenStatement whenStatement = choiceStatement.getWhenStatement().get();

        final Collection<? extends IfFeatureStatement> ifFeatureStatements = choiceStatement.getIfFeatures();
        assertNotNull(ifFeatureStatements);
        assertEquals(1, ifFeatureStatements.size());

        assertTrue(choiceStatement.getStatus().isPresent());
        assertTrue(choiceStatement.getDescription().isPresent());
        assertTrue(choiceStatement.getReference().isPresent());
    }

    @Test
    public void testDeclaredAugment() throws ReactorException {
        final StatementStreamSource augmentStmtModule =
                sourceForResource("/declared-statements-test/augment-declared-test.yang");

        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(augmentStmtModule);
        assertNotNull(schemaContext);

        final Module testModule = schemaContext.findModules("augment-declared-test").iterator().next();
        assertNotNull(testModule);

        final Set<AugmentationSchemaNode> augmentationSchemas = testModule.getAugmentations();
        assertNotNull(augmentationSchemas);
        assertEquals(1, augmentationSchemas.size());

        final AugmentationSchemaNode augmentationSchema = augmentationSchemas.iterator().next();
        final AugmentStatement augmentStatement = ((AugmentEffectiveStatement) augmentationSchema).getDeclared();

        final SchemaNodeIdentifier targetNode = augmentStatement.getTargetNode();
        assertNotNull(targetNode);

        final Collection<? extends DataDefinitionStatement> augmentStatementDataDefinitions =
                augmentStatement.getDataDefinitions();
        assertNotNull(augmentStatementDataDefinitions);
        assertEquals(1, augmentStatementDataDefinitions.size());
    }

    @Test
    public void testDeclaredModuleAndSubmodule() throws ReactorException {
        final StatementStreamSource parentModule =
                sourceForResource("/declared-statements-test/parent-module-declared-test.yang");

        final StatementStreamSource childModule =
                sourceForResource("/declared-statements-test/child-module-declared-test.yang");

        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(parentModule, childModule);
        assertNotNull(schemaContext);

        final Module testModule = schemaContext.findModules("parent-module-declared-test").iterator().next();
        assertNotNull(testModule);

        final ModuleStatement moduleStatement = ((ModuleEffectiveStatement) testModule).getDeclared();

        final String moduleStatementName = moduleStatement.getName();
        assertNotNull(moduleStatementName);

        final YangVersionStatement moduleStatementYangVersion = moduleStatement.getYangVersion();
        assertNotNull(moduleStatementYangVersion);
        assertNotNull(moduleStatementYangVersion.getValue());

        final NamespaceStatement moduleStatementNamspace = moduleStatement.getNamespace();
        assertNotNull(moduleStatementNamspace);
        assertNotNull(moduleStatementNamspace.getUri());

        final PrefixStatement moduleStatementPrefix = moduleStatement.getPrefix();
        assertNotNull(moduleStatementPrefix);
        assertNotNull(moduleStatementPrefix.getValue());

        assertEquals(1, moduleStatement.getIncludes().size());
        final IncludeStatement includeStatement = moduleStatement.getIncludes().iterator().next();
        assertEquals("child-module-declared-test", includeStatement.getModule());

        final Set<Module> submodules = testModule.getSubmodules();
        assertNotNull(submodules);
        assertEquals(1, submodules.size());

        final Module submodule = submodules.iterator().next();
        final SubmoduleStatement submoduleStatement = ((SubmoduleEffectiveStatement) submodule).getDeclared();

        final String submoduleStatementName = submoduleStatement.getName();
        assertNotNull(submoduleStatementName);

        final YangVersionStatement submoduleStatementYangVersion = submoduleStatement.getYangVersion();
        assertNotNull(submoduleStatementYangVersion);

        final BelongsToStatement belongsToStatement = submoduleStatement.getBelongsTo();
        assertNotNull(belongsToStatement);
        assertNotNull(belongsToStatement.getModule());
        assertNotNull(belongsToStatement.getPrefix());
    }

    @Test
    public void testDeclaredModule() throws ReactorException, ParseException {
        final StatementStreamSource rootModule =
                sourceForResource("/declared-statements-test/root-module-declared-test.yang");

        final StatementStreamSource importedModule =
                sourceForResource("/declared-statements-test/imported-module-declared-test.yang");

        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(rootModule, importedModule);
        assertNotNull(schemaContext);

        final Revision revision = Revision.of("2016-09-28");
        final Module testModule = schemaContext.findModule("root-module-declared-test", revision).get();
        assertNotNull(testModule);

        final ModuleStatement moduleStatement = ((ModuleEffectiveStatement) testModule).getDeclared();

        assertEquals(1, moduleStatement.getImports().size());
        final ImportStatement importStatement = moduleStatement.getImports().iterator().next();
        assertEquals("imported-module-declared-test", importStatement.getModule());
        assertEquals("imdt", importStatement.getPrefix().getValue());
        assertEquals(revision, importStatement.getRevisionDate().getDate());

        assertEquals("test description", moduleStatement.getDescription().get().getText());
        assertEquals("test reference", moduleStatement.getReference().get().getText());
        assertEquals("test organization", moduleStatement.getOrganization().get().getText());
        assertEquals("test contact", moduleStatement.getContact().get().getText());

        assertEquals(1, moduleStatement.getRevisions().size());
        final RevisionStatement revisionStatement = moduleStatement.getRevisions().iterator().next();
        assertEquals(revision, revisionStatement.getDate());
        assertEquals("test description", revisionStatement.getDescription().get().getText());
        assertEquals("test reference", revisionStatement.getReference().get().getText());

        assertEquals(1, moduleStatement.getExtensions().size());
        final ExtensionStatement extensionStatement = moduleStatement.getExtensions().iterator().next();
        assertEquals(Status.CURRENT, extensionStatement.getStatus().get().getValue());
        assertEquals("test description", extensionStatement.getDescription().get().getText());
        assertEquals("test reference", extensionStatement.getReference().get().getText());
        final ArgumentStatement argumentStatement = extensionStatement.getArgument();
        assertEquals("ext-argument", argumentStatement.getName().getLocalName());
        assertTrue(argumentStatement.getYinElement().getValue());

        assertEquals(2, moduleStatement.getFeatures().size());
        final FeatureStatement featureStatement = moduleStatement.getFeatures().iterator().next();
        assertEquals(Status.CURRENT, featureStatement.getStatus().get().getValue());
        assertEquals("test description", featureStatement.getDescription().get().getText());
        assertEquals("test reference", featureStatement.getReference().get().getText());
        assertEquals("test-feature", featureStatement.getName().getLocalName());
        assertEquals(1, featureStatement.getIfFeatures().size());

        assertEquals(2, moduleStatement.getIdentities().size());
        IdentityStatement identityStatement = null;
        for (final IdentityStatement identity : moduleStatement.getIdentities()) {
            if (identity.getName().getLocalName().equals("test-id")) {
                identityStatement = identity;
            }
        }

        assertEquals("test-base-id", identityStatement.getBases().iterator().next().getName().getLocalName());
        assertEquals(Status.CURRENT, identityStatement.getStatus().get().getValue());
        assertEquals("test description", identityStatement.getDescription().get().getText());
        assertEquals("test reference", identityStatement.getReference().get().getText());
        assertEquals("test-id", identityStatement.getName().getLocalName());

        assertEquals(1, moduleStatement.getTypedefs().size());
        final TypedefStatement typedefStatement = moduleStatement.getTypedefs().iterator().next();
        assertEquals(Status.CURRENT, typedefStatement.getStatus().get().getValue());
        assertEquals("test description", typedefStatement.getDescription().get().getText());
        assertEquals("test reference", typedefStatement.getReference().get().getText());
        assertEquals("test-typedef", typedefStatement.getName().getLocalName());
        assertEquals("int32", typedefStatement.getType().getName());
        assertEquals("meter", typedefStatement.getUnits().get().getName());
    }

    @Test
    public void testDeclaredContainer() throws ReactorException {
        final StatementStreamSource containerStmtModule =
                sourceForResource("/declared-statements-test/container-declared-test.yang");

        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(containerStmtModule);
        assertNotNull(schemaContext);

        final Module testModule = schemaContext.findModules("container-declared-test").iterator().next();
        assertNotNull(testModule);

        final ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) testModule.getDataChildByName(
                QName.create(testModule.getQNameModule(), "test-container"));
        assertNotNull(containerSchemaNode);
        final ContainerStatement containerStatement =
                ((ContainerEffectiveStatement) containerSchemaNode).getDeclared();

        final QName name = containerStatement.getName();
        assertNotNull(name);

        final WhenStatement containerStatementWhen = containerStatement.getWhenStatement().get();

        final Collection<? extends IfFeatureStatement> containerStatementIfFeatures =
                containerStatement.getIfFeatures();
        assertNotNull(containerStatementIfFeatures);
        assertEquals(1, containerStatementIfFeatures.size());

        final Collection<? extends MustStatement> containerStatementMusts = containerStatement.getMustStatements();
        assertNotNull(containerStatementMusts);
        assertEquals(1, containerStatementMusts.size());

        final PresenceStatement containerStatementPresence = containerStatement.getPresence();
        assertNotNull(containerStatementPresence);
        assertNotNull(containerStatementPresence.getValue());

        assertTrue(containerStatement.getConfig().isPresent());
        assertTrue(containerStatement.getStatus().isPresent());
        assertTrue(containerStatement.getDescription().isPresent());
        assertTrue(containerStatement.getReference().isPresent());

        final Collection<? extends TypedefStatement> containerStatementTypedefs = containerStatement.getTypedefs();
        assertNotNull(containerStatementTypedefs);
        assertEquals(1, containerStatementTypedefs.size());

        final Collection<? extends GroupingStatement> containerStatementGroupings = containerStatement.getGroupings();
        assertNotNull(containerStatementGroupings);
        assertEquals(1, containerStatementGroupings.size());

        final Collection<? extends DataDefinitionStatement> containerStatementDataDefinitions =
                containerStatement.getDataDefinitions();

        assertNotNull(containerStatementDataDefinitions);
        assertEquals(1, containerStatementDataDefinitions.size());
    }
}
