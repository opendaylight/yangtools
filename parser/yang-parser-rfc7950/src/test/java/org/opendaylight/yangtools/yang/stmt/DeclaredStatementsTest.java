/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.Submodule;
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
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
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

class DeclaredStatementsTest extends AbstractYangTest {
    @Test
    void testDeclaredAnyXml() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/anyxml-declared-test.yang");

        final Module testModule = schemaContext.findModules("anyxml-declared-test").iterator().next();
        assertNotNull(testModule);

        final AnyxmlSchemaNode anyxmlSchemaNode = (AnyxmlSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "foobar"));
        assertNotNull(anyxmlSchemaNode);
        final AnyxmlStatement anyxmlStatement = ((AnyxmlEffectiveStatement) anyxmlSchemaNode).getDeclared();

        final QName name = anyxmlStatement.argument();
        assertNotNull(name);

        final WhenStatement whenStatement = anyxmlStatement.getWhenStatement().orElseThrow();
        assertNotNull(whenStatement.argument());
        final DescriptionStatement whenStatementDescription = whenStatement.getDescription().orElseThrow();
        assertTrue(whenStatement.getReference().isPresent());

        final var ifFeatureStatements = anyxmlStatement.getIfFeatures();
        assertNotNull(ifFeatureStatements);
        assertEquals(1, ifFeatureStatements.size());
        final var ifFeaturePredicate = ifFeatureStatements.iterator().next().argument();
        assertNotNull(ifFeaturePredicate);

        final var mustStatements = anyxmlStatement.getMustStatements();
        assertNotNull(mustStatements);
        assertEquals(1, mustStatements.size());
        final MustStatement mustStatement = mustStatements.iterator().next();
        assertNotNull(mustStatement.argument());
        assertTrue(mustStatement.getErrorAppTagStatement().isPresent());
        assertTrue(mustStatement.getErrorMessageStatement().isPresent());
        assertTrue(mustStatement.getDescription().isPresent());
        assertTrue(mustStatement.getReference().isPresent());

        final ConfigStatement configStatement = anyxmlStatement.getConfig().orElseThrow();
        assertFalse(configStatement.argument());

        final StatusStatement statusStatement = anyxmlStatement.getStatus().orElseThrow();
        final Status status = statusStatement.argument();
        assertNotNull(status);

        final DescriptionStatement descriptionStatement = anyxmlStatement.getDescription().orElseThrow();
        assertEquals("anyxml description", descriptionStatement.argument());

        final ReferenceStatement referenceStatement = anyxmlStatement.getReference().orElseThrow();
        assertEquals("anyxml reference", referenceStatement.argument());

        assertTrue(anyxmlStatement.getMandatory().isPresent());
    }

    @Test
    void testDeclaredChoice() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/choice-declared-test.yang");

        final Module testModule = schemaContext.findModules("choice-declared-test").iterator().next();
        assertNotNull(testModule);

        final ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-choice"));
        assertNotNull(choiceSchemaNode);
        final ChoiceStatement choiceStatement = ((ChoiceEffectiveStatement) choiceSchemaNode).getDeclared();

        final QName name = choiceStatement.argument();
        assertNotNull(name);

        final DefaultStatement defaultStatement = choiceStatement.getDefault().orElseThrow();
        assertEquals("case-two", defaultStatement.argument());

        assertTrue(choiceStatement.getConfig().isPresent());
        assertTrue(choiceStatement.getMandatory().isPresent());

        final var caseStatements = choiceStatement.getCases();
        assertNotNull(caseStatements);
        assertEquals(3, caseStatements.size());
        final CaseStatement caseStatement = caseStatements.iterator().next();
        final QName caseStatementName = caseStatement.argument();
        assertNotNull(caseStatementName);
        final WhenStatement caseStatementWhen = caseStatement.getWhenStatement().orElseThrow();
        final var caseStatementIfFeatures = caseStatement.getIfFeatures();
        assertNotNull(caseStatementIfFeatures);
        assertEquals(1, caseStatementIfFeatures.size());
        final var caseStatementDataDefinitions = caseStatement.getDataDefinitions();
        assertNotNull(caseStatementDataDefinitions);
        assertEquals(1, caseStatementDataDefinitions.size());
        assertTrue(caseStatement.getStatus().isPresent());
        assertTrue(caseStatement.getDescription().isPresent());
        assertTrue(caseStatement.getReference().isPresent());

        final WhenStatement whenStatement = choiceStatement.getWhenStatement().orElseThrow();

        final var ifFeatureStatements = choiceStatement.getIfFeatures();
        assertNotNull(ifFeatureStatements);
        assertEquals(1, ifFeatureStatements.size());

        assertTrue(choiceStatement.getStatus().isPresent());
        assertTrue(choiceStatement.getDescription().isPresent());
        assertTrue(choiceStatement.getReference().isPresent());
    }

    @Test
    void testDeclaredAugment() throws ReactorException {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/augment-declared-test.yang");

        final var testModule = schemaContext.findModules("augment-declared-test").iterator().next();
        assertNotNull(testModule);

        final var augmentationSchemas = testModule.getAugmentations();
        assertNotNull(augmentationSchemas);
        assertEquals(1, augmentationSchemas.size());

        final AugmentationSchemaNode augmentationSchema = augmentationSchemas.iterator().next();
        final AugmentStatement augmentStatement = ((AugmentEffectiveStatement) augmentationSchema).getDeclared();

        final SchemaNodeIdentifier targetNode = augmentStatement.argument();
        assertNotNull(targetNode);

        final var augmentStatementDataDefinitions = augmentStatement.getDataDefinitions();
        assertNotNull(augmentStatementDataDefinitions);
        assertEquals(1, augmentStatementDataDefinitions.size());
    }

    @Test
    void testDeclaredModuleAndSubmodule() throws ReactorException {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/parent-module-declared-test.yang",
            "/declared-statements-test/child-module-declared-test.yang");

        final Module testModule = schemaContext.findModules("parent-module-declared-test").iterator().next();
        assertNotNull(testModule);

        final ModuleStatement moduleStatement = ((ModuleEffectiveStatement) testModule).getDeclared();
        assertNotNull(moduleStatement.argument());

        final YangVersionStatement moduleStatementYangVersion = moduleStatement.getYangVersion();
        assertNotNull(moduleStatementYangVersion);
        assertNotNull(moduleStatementYangVersion.argument());

        final NamespaceStatement moduleStatementNamspace = moduleStatement.getNamespace();
        assertNotNull(moduleStatementNamspace);
        assertNotNull(moduleStatementNamspace.argument());

        final PrefixStatement moduleStatementPrefix = moduleStatement.getPrefix();
        assertNotNull(moduleStatementPrefix);
        assertNotNull(moduleStatementPrefix.argument());

        assertEquals(1, moduleStatement.getIncludes().size());
        final IncludeStatement includeStatement = moduleStatement.getIncludes().iterator().next();
        assertEquals(Unqualified.of("child-module-declared-test"), includeStatement.argument());

        final var submodules = testModule.getSubmodules();
        assertNotNull(submodules);
        assertEquals(1, submodules.size());

        final Submodule submodule = submodules.iterator().next();
        final SubmoduleStatement submoduleStatement = ((SubmoduleEffectiveStatement) submodule).getDeclared();

        assertNotNull(submoduleStatement.argument());

        final YangVersionStatement submoduleStatementYangVersion = submoduleStatement.getYangVersion();
        assertNotNull(submoduleStatementYangVersion);

        final BelongsToStatement belongsToStatement = submoduleStatement.getBelongsTo();
        assertNotNull(belongsToStatement);
        assertNotNull(belongsToStatement.argument());
        assertNotNull(belongsToStatement.getPrefix());
    }

    @Test
    void testDeclaredModule() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/root-module-declared-test.yang",
            "/declared-statements-test/imported-module-declared-test.yang");

        final Revision revision = Revision.of("2016-09-28");
        final Module testModule = schemaContext.findModule("root-module-declared-test", revision).orElseThrow();
        assertNotNull(testModule);

        final ModuleStatement moduleStatement = ((ModuleEffectiveStatement) testModule).getDeclared();

        assertEquals(1, moduleStatement.getImports().size());
        final ImportStatement importStatement = moduleStatement.getImports().iterator().next();
        assertEquals(Unqualified.of("imported-module-declared-test"), importStatement.argument());
        assertEquals("imdt", importStatement.getPrefix().argument());
        assertEquals(revision, importStatement.getRevisionDate().argument());

        assertEquals("test description", moduleStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", moduleStatement.getReference().orElseThrow().argument());
        assertEquals("test organization", moduleStatement.getOrganization().orElseThrow().argument());
        assertEquals("test contact", moduleStatement.getContact().orElseThrow().argument());

        assertEquals(1, moduleStatement.getRevisions().size());
        final RevisionStatement revisionStatement = moduleStatement.getRevisions().iterator().next();
        assertEquals(revision, revisionStatement.argument());
        assertEquals("test description", revisionStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", revisionStatement.getReference().orElseThrow().argument());

        assertEquals(1, moduleStatement.getExtensions().size());
        final ExtensionStatement extensionStatement = moduleStatement.getExtensions().iterator().next();
        assertEquals(Status.CURRENT, extensionStatement.getStatus().orElseThrow().argument());
        assertEquals("test description", extensionStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", extensionStatement.getReference().orElseThrow().argument());
        final ArgumentStatement argumentStatement = extensionStatement.getArgument();
        assertEquals("ext-argument", argumentStatement.argument().getLocalName());
        assertTrue(argumentStatement.getYinElement().argument());

        assertEquals(2, moduleStatement.getFeatures().size());
        final FeatureStatement featureStatement = moduleStatement.getFeatures().iterator().next();
        assertEquals(Status.CURRENT, featureStatement.getStatus().orElseThrow().argument());
        assertEquals("test description", featureStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", featureStatement.getReference().orElseThrow().argument());
        assertEquals("test-feature", featureStatement.argument().getLocalName());
        assertEquals(1, featureStatement.getIfFeatures().size());

        assertEquals(2, moduleStatement.getIdentities().size());
        IdentityStatement identityStatement = moduleStatement.getIdentities().stream()
            .filter(identity -> identity.argument().getLocalName().equals("test-id"))
            .findFirst()
            .orElseThrow();

        assertEquals("test-base-id", identityStatement.getBases().iterator().next().argument().getLocalName());
        assertEquals(Status.CURRENT, identityStatement.getStatus().orElseThrow().argument());
        assertEquals("test description", identityStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", identityStatement.getReference().orElseThrow().argument());
        assertEquals("test-id", identityStatement.argument().getLocalName());

        assertEquals(1, moduleStatement.getTypedefs().size());
        final TypedefStatement typedefStatement = moduleStatement.getTypedefs().iterator().next();
        assertEquals(Status.CURRENT, typedefStatement.getStatus().orElseThrow().argument());
        assertEquals("test description", typedefStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", typedefStatement.getReference().orElseThrow().argument());
        assertEquals("test-typedef", typedefStatement.argument().getLocalName());
        assertEquals("int32", typedefStatement.getType().rawArgument());
        assertEquals("meter", typedefStatement.getUnits().orElseThrow().argument());
    }

    @Test
    void testDeclaredContainer() throws ReactorException {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/container-declared-test.yang");

        final Module testModule = schemaContext.findModules("container-declared-test").iterator().next();
        assertNotNull(testModule);

        final ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container"));
        assertNotNull(containerSchemaNode);
        final ContainerStatement containerStatement =
            ((ContainerEffectiveStatement) containerSchemaNode).getDeclared();

        final QName name = containerStatement.argument();
        assertNotNull(name);

        final WhenStatement containerStatementWhen = containerStatement.getWhenStatement().orElseThrow();

        final var containerStatementIfFeatures = containerStatement.getIfFeatures();
        assertNotNull(containerStatementIfFeatures);
        assertEquals(1, containerStatementIfFeatures.size());

        final var containerStatementMusts = containerStatement.getMustStatements();
        assertNotNull(containerStatementMusts);
        assertEquals(1, containerStatementMusts.size());

        final PresenceStatement containerStatementPresence = containerStatement.getPresence();
        assertNotNull(containerStatementPresence);
        assertNotNull(containerStatementPresence.argument());

        assertTrue(containerStatement.getConfig().isPresent());
        assertTrue(containerStatement.getStatus().isPresent());
        assertTrue(containerStatement.getDescription().isPresent());
        assertTrue(containerStatement.getReference().isPresent());

        final var containerStatementTypedefs = containerStatement.getTypedefs();
        assertNotNull(containerStatementTypedefs);
        assertEquals(1, containerStatementTypedefs.size());

        final var containerStatementGroupings = containerStatement.getGroupings();
        assertNotNull(containerStatementGroupings);
        assertEquals(1, containerStatementGroupings.size());

        final var containerStatementDataDefinitions = containerStatement.getDataDefinitions();

        assertNotNull(containerStatementDataDefinitions);
        assertEquals(1, containerStatementDataDefinitions.size());
    }
}
