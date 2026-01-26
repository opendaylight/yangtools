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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;

class DeclaredStatementsTest extends AbstractYangTest {
    @Test
    void testDeclaredAnyXml() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/anyxml-declared-test.yang");

        final var testModule = schemaContext.findModules("anyxml-declared-test").iterator().next();
        assertNotNull(testModule);

        final var anyxmlSchemaNode = assertInstanceOf(AnyxmlSchemaNode.class,
            testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "foobar")));
        final var anyxmlStatement = anyxmlSchemaNode.asEffectiveStatement().declared();
        assertNotNull(anyxmlStatement);

        final var name = anyxmlStatement.argument();
        assertNotNull(name);

        final var whenStatement = anyxmlStatement.getWhenStatement();
        assertNotNull(whenStatement.argument());
        final var whenStatementDescription = whenStatement.getDescriptionStatement();
        assertNotNull(whenStatementDescription.argument());
        assertNotNull(whenStatement.referenceStatement());

        final var ifFeatureStatements = anyxmlStatement.ifFeatureStatements();
        assertEquals(1, ifFeatureStatements.size());
        final var ifFeaturePredicate = ifFeatureStatements.iterator().next().argument();
        assertNotNull(ifFeaturePredicate);

        final var mustStatements = anyxmlStatement.mustStatements();
        assertEquals(1, mustStatements.size());
        final var mustStatement = mustStatements.iterator().next();
        assertNotNull(mustStatement.argument());
        assertNotNull(mustStatement.getErrorAppTagStatement());
        assertNotNull(mustStatement.getErrorMessageStatement());
        assertNotNull(mustStatement.descriptionStatement());
        assertNotNull(mustStatement.referenceStatement());

        final var configStatement = anyxmlStatement.getConfigStatement();
        assertFalse(configStatement.argument());

        final var statusStatement = anyxmlStatement.getStatusStatement();
        final var status = statusStatement.argument();
        assertNotNull(status);

        final var descriptionStatement = anyxmlStatement.getDescriptionStatement();
        assertEquals("anyxml description", descriptionStatement.argument());

        final var referenceStatement = anyxmlStatement.getReferenceStatement();
        assertEquals("anyxml reference", referenceStatement.argument());

        assertNotNull(anyxmlStatement.mandatoryStatement());
    }

    @Test
    void testDeclaredChoice() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/choice-declared-test.yang");

        final var testModule = schemaContext.findModules("choice-declared-test").iterator().next();
        assertNotNull(testModule);

        final var choiceSchemaNode = assertInstanceOf(ChoiceSchemaNode.class,
            testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "test-choice")));
        assertNotNull(choiceSchemaNode);
        final var choiceStatement = choiceSchemaNode.asEffectiveStatement().requireDeclared();

        final var name = choiceStatement.argument();
        assertNotNull(name);

        final var defaultStatement = choiceStatement.getDefaultStatement();
        assertEquals("case-two", defaultStatement.argument());

        assertNotNull(choiceStatement.configStatement());
        assertNotNull(choiceStatement.mandatoryStatement());

        final var caseStatements = choiceStatement.caseStatements();
        assertEquals(3, caseStatements.size());
        final var caseStatement = caseStatements.iterator().next();
        final var caseStatementName = caseStatement.argument();
        assertNotNull(caseStatementName);
        assertNotNull(caseStatement.whenStatement());
        final var caseStatementIfFeatures = caseStatement.ifFeatureStatements();
        assertEquals(1, caseStatementIfFeatures.size());
        final var caseStatementDataDefinitions = caseStatement.dataDefinitionStatements();
        assertEquals(1, caseStatementDataDefinitions.size());
        assertNotNull(caseStatement.statusStatement());
        assertNotNull(caseStatement.descriptionStatement());
        assertNotNull(caseStatement.referenceStatement());

        assertNotNull(choiceStatement.whenStatement());

        final var ifFeatureStatements = choiceStatement.ifFeatureStatements();
        assertEquals(1, ifFeatureStatements.size());

        assertNotNull(choiceStatement.statusStatement());
        assertNotNull(choiceStatement.descriptionStatement());
        assertNotNull(choiceStatement.referenceStatement());
    }

    @Test
    void testDeclaredAugment() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/augment-declared-test.yang");

        final var testModule = schemaContext.findModules("augment-declared-test").iterator().next();
        assertNotNull(testModule);

        final var augmentationSchemas = testModule.getAugmentations();
        assertNotNull(augmentationSchemas);
        assertEquals(1, augmentationSchemas.size());

        final var augmentationSchema = augmentationSchemas.iterator().next();
        final var augmentStatement = augmentationSchema.asEffectiveStatement().requireDeclared();

        final var targetNode = augmentStatement.argument();
        assertNotNull(targetNode);

        final var augmentStatementDataDefinitions = augmentStatement.dataDefinitionStatements();
        assertEquals(1, augmentStatementDataDefinitions.size());
    }

    @Test
    void testDeclaredModuleAndSubmodule() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/parent-module-declared-test.yang",
            "/declared-statements-test/child-module-declared-test.yang");

        final var testModule = schemaContext.findModules("parent-module-declared-test").iterator().next();
        assertNotNull(testModule);

        final var moduleStatement = testModule.asEffectiveStatement().requireDeclared();
        assertNotNull(moduleStatement.argument());

        final var moduleStatementYangVersion = moduleStatement.yangVersionStatement();
        assertNotNull(moduleStatementYangVersion);
        assertNotNull(moduleStatementYangVersion.argument());

        final var moduleStatementNamspace = moduleStatement.namespaceStatement();
        assertNotNull(moduleStatementNamspace);
        assertNotNull(moduleStatementNamspace.argument());

        final var moduleStatementPrefix = moduleStatement.prefixStatement();
        assertNotNull(moduleStatementPrefix);
        assertNotNull(moduleStatementPrefix.argument());

        final var moduleIncludes = moduleStatement.includeStatements();
        assertEquals(1, moduleIncludes.size());
        final var includeStatement = moduleIncludes.iterator().next();
        assertEquals(Unqualified.of("child-module-declared-test"), includeStatement.argument());

        final var submodules = testModule.getSubmodules();
        assertNotNull(submodules);
        assertEquals(1, submodules.size());

        final var submodule = submodules.iterator().next();
        final var submoduleStatement = submodule.asEffectiveStatement().requireDeclared();

        assertNotNull(submoduleStatement.argument());

        final var submoduleStatementYangVersion = submoduleStatement.yangVersionStatement();
        assertNotNull(submoduleStatementYangVersion);

        final var belongsToStatement = submoduleStatement.belongsToStatement();
        assertNotNull(belongsToStatement);
        assertNotNull(belongsToStatement.argument());
        assertNotNull(belongsToStatement.prefixStatement());
    }

    @Test
    void testDeclaredModule() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/root-module-declared-test.yang",
            "/declared-statements-test/imported-module-declared-test.yang");

        final var revision = Revision.of("2016-09-28");
        final var testModule = schemaContext.findModule("root-module-declared-test", revision).orElseThrow();
        assertNotNull(testModule);

        final var moduleStatement = testModule.asEffectiveStatement().requireDeclared();

        final var moduleImports = moduleStatement.importStatements();
        assertEquals(1, moduleImports.size());
        final var importStatement = moduleImports.iterator().next();
        assertEquals(Unqualified.of("imported-module-declared-test"), importStatement.argument());
        assertEquals("imdt", importStatement.getPrefixStatement().argument());
        assertEquals(revision, importStatement.getRevisionDateStatement().argument());

        assertEquals("test description", moduleStatement.getDescriptionStatement().argument());
        assertEquals("test reference", moduleStatement.getReferenceStatement().argument());
        assertEquals("test organization", moduleStatement.getOrganizationStatement().argument());
        assertEquals("test contact", moduleStatement.getContactStatement().argument());

        final var moduleRevisions = moduleStatement.revisionStatements();
        assertEquals(1, moduleRevisions.size());
        final var revisionStatement = moduleRevisions.iterator().next();
        assertEquals(revision, revisionStatement.argument());
        assertEquals("test description", revisionStatement.getDescriptionStatement().argument());
        assertEquals("test reference", revisionStatement.getReferenceStatement().argument());

        final var moduleExtensions = moduleStatement.extensionStatements();
        assertEquals(1, moduleExtensions.size());
        final var extensionStatement = moduleExtensions.iterator().next();
        assertEquals(Status.CURRENT, extensionStatement.getStatusStatement().argument());
        assertEquals("test description", extensionStatement.getDescriptionStatement().argument());
        assertEquals("test reference", extensionStatement.getReferenceStatement().argument());
        final var argumentStatement = extensionStatement.getArgumentStatement();
        assertEquals("ext-argument", argumentStatement.argument().getLocalName());
        assertTrue(argumentStatement.getYinElementStatement().argument());

        final var moduleFeatures = moduleStatement.featureStatements();
        assertEquals(2, moduleFeatures.size());
        final var featureStatement = moduleFeatures.iterator().next();
        assertEquals(Status.CURRENT, featureStatement.getStatusStatement().argument());
        assertEquals("test description", featureStatement.getDescriptionStatement().argument());
        assertEquals("test reference", featureStatement.getReferenceStatement().argument());
        assertEquals("test-feature", featureStatement.argument().getLocalName());
        assertEquals(1, featureStatement.ifFeatureStatements().size());

        final var moduleIdentities = moduleStatement.identityStatements();
        assertEquals(2, moduleIdentities.size());
        final var identityStatement = moduleIdentities.stream()
            .filter(identity -> identity.argument().getLocalName().equals("test-id"))
            .findFirst()
            .orElseThrow();

        assertEquals("test-base-id", identityStatement.baseStatements().iterator().next().argument().getLocalName());
        assertEquals(Status.CURRENT, identityStatement.getStatusStatement().argument());
        assertEquals("test description", identityStatement.getDescriptionStatement().argument());
        assertEquals("test reference", identityStatement.getReferenceStatement().argument());
        assertEquals("test-id", identityStatement.argument().getLocalName());

        assertEquals(1, moduleStatement.typedefStatements().size());
        final var typedefStatement = moduleStatement.typedefStatements().iterator().next();
        assertEquals(Status.CURRENT, typedefStatement.getStatusStatement().argument());
        assertEquals("test description", typedefStatement.getDescriptionStatement().argument());
        assertEquals("test reference", typedefStatement.getReferenceStatement().argument());
        assertEquals("test-typedef", typedefStatement.argument().getLocalName());
        assertEquals("int32", typedefStatement.getTypeStatement().rawArgument());
        assertEquals("meter", typedefStatement.getUnitsStatement().argument());
    }

    @Test
    void testDeclaredContainer() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/container-declared-test.yang");

        final var testModule = schemaContext.findModules("container-declared-test").iterator().next();
        assertNotNull(testModule);

        final var containerSchemaNode = assertInstanceOf(ContainerSchemaNode.class,
            testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "test-container")));
        final var containerStatement = containerSchemaNode.asEffectiveStatement().requireDeclared();

        final var name = containerStatement.argument();
        assertNotNull(name);

        assertNotNull(containerStatement.whenStatement());
        final var containerStatementIfFeatures = containerStatement.ifFeatureStatements();
        assertEquals(1, containerStatementIfFeatures.size());

        final var containerStatementMusts = containerStatement.mustStatements();
        assertEquals(1, containerStatementMusts.size());

        final var containerStatementPresence = containerStatement.presenceStatement();
        assertNotNull(containerStatementPresence);
        assertNotNull(containerStatementPresence.argument());

        assertNotNull(containerStatement.configStatement());
        assertNotNull(containerStatement.statusStatement());
        assertNotNull(containerStatement.descriptionStatement());
        assertNotNull(containerStatement.referenceStatement());

        final var containerStatementTypedefs = containerStatement.typedefStatements();
        assertEquals(1, containerStatementTypedefs.size());

        final var containerStatementGroupings = containerStatement.groupingStatements();
        assertEquals(1, containerStatementGroupings.size());

        final var containerStatementDataDefinitions = containerStatement.dataDefinitionStatements();
        assertEquals(1, containerStatementDataDefinitions.size());
    }
}
