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

        final var whenStatement = anyxmlStatement.getWhenStatement().orElseThrow();
        assertNotNull(whenStatement.argument());
        final var whenStatementDescription = whenStatement.getDescription().orElseThrow();
        assertNotNull(whenStatementDescription.argument());
        assertTrue(whenStatement.getReference().isPresent());

        final var ifFeatureStatements = anyxmlStatement.getIfFeatures();
        assertNotNull(ifFeatureStatements);
        assertEquals(1, ifFeatureStatements.size());
        final var ifFeaturePredicate = ifFeatureStatements.iterator().next().argument();
        assertNotNull(ifFeaturePredicate);

        final var mustStatements = anyxmlStatement.getMustStatements();
        assertNotNull(mustStatements);
        assertEquals(1, mustStatements.size());
        final var mustStatement = mustStatements.iterator().next();
        assertNotNull(mustStatement.argument());
        assertTrue(mustStatement.getErrorAppTagStatement().isPresent());
        assertTrue(mustStatement.getErrorMessageStatement().isPresent());
        assertTrue(mustStatement.getDescription().isPresent());
        assertTrue(mustStatement.getReference().isPresent());

        final var configStatement = anyxmlStatement.getConfig().orElseThrow();
        assertFalse(configStatement.argument());

        final var statusStatement = anyxmlStatement.getStatus().orElseThrow();
        final var status = statusStatement.argument();
        assertNotNull(status);

        final var descriptionStatement = anyxmlStatement.getDescription().orElseThrow();
        assertEquals("anyxml description", descriptionStatement.argument());

        final var referenceStatement = anyxmlStatement.getReference().orElseThrow();
        assertEquals("anyxml reference", referenceStatement.argument());

        assertTrue(anyxmlStatement.getMandatory().isPresent());
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

        final var defaultStatement = choiceStatement.getDefault().orElseThrow();
        assertEquals("case-two", defaultStatement.argument());

        assertTrue(choiceStatement.getConfig().isPresent());
        assertTrue(choiceStatement.getMandatory().isPresent());

        final var caseStatements = choiceStatement.getCases();
        assertNotNull(caseStatements);
        assertEquals(3, caseStatements.size());
        final var caseStatement = caseStatements.iterator().next();
        final var caseStatementName = caseStatement.argument();
        assertNotNull(caseStatementName);
        assertNotNull(caseStatement.getWhenStatement().orElseThrow());
        final var caseStatementIfFeatures = caseStatement.getIfFeatures();
        assertNotNull(caseStatementIfFeatures);
        assertEquals(1, caseStatementIfFeatures.size());
        final var caseStatementDataDefinitions = caseStatement.getDataDefinitions();
        assertNotNull(caseStatementDataDefinitions);
        assertEquals(1, caseStatementDataDefinitions.size());
        assertTrue(caseStatement.getStatus().isPresent());
        assertTrue(caseStatement.getDescription().isPresent());
        assertTrue(caseStatement.getReference().isPresent());

        assertNotNull(choiceStatement.getWhenStatement().orElseThrow());

        final var ifFeatureStatements = choiceStatement.getIfFeatures();
        assertNotNull(ifFeatureStatements);
        assertEquals(1, ifFeatureStatements.size());

        assertTrue(choiceStatement.getStatus().isPresent());
        assertTrue(choiceStatement.getDescription().isPresent());
        assertTrue(choiceStatement.getReference().isPresent());
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

        final var augmentStatementDataDefinitions = augmentStatement.getDataDefinitions();
        assertNotNull(augmentStatementDataDefinitions);
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

        final var moduleStatementYangVersion = moduleStatement.getYangVersion();
        assertNotNull(moduleStatementYangVersion);
        assertNotNull(moduleStatementYangVersion.argument());

        final var moduleStatementNamspace = moduleStatement.getNamespace();
        assertNotNull(moduleStatementNamspace);
        assertNotNull(moduleStatementNamspace.argument());

        final var moduleStatementPrefix = moduleStatement.getPrefix();
        assertNotNull(moduleStatementPrefix);
        assertNotNull(moduleStatementPrefix.argument());

        assertEquals(1, moduleStatement.getIncludes().size());
        final var includeStatement = moduleStatement.getIncludes().iterator().next();
        assertEquals(Unqualified.of("child-module-declared-test"), includeStatement.argument());

        final var submodules = testModule.getSubmodules();
        assertNotNull(submodules);
        assertEquals(1, submodules.size());

        final var submodule = submodules.iterator().next();
        final var submoduleStatement = submodule.asEffectiveStatement().requireDeclared();

        assertNotNull(submoduleStatement.argument());

        final var submoduleStatementYangVersion = submoduleStatement.getYangVersion();
        assertNotNull(submoduleStatementYangVersion);

        final var belongsToStatement = submoduleStatement.getBelongsTo();
        assertNotNull(belongsToStatement);
        assertNotNull(belongsToStatement.argument());
        assertNotNull(belongsToStatement.getPrefix());
    }

    @Test
    void testDeclaredModule() {
        final var schemaContext = assertEffectiveModel("/declared-statements-test/root-module-declared-test.yang",
            "/declared-statements-test/imported-module-declared-test.yang");

        final var revision = Revision.of("2016-09-28");
        final var testModule = schemaContext.findModule("root-module-declared-test", revision).orElseThrow();
        assertNotNull(testModule);

        final var moduleStatement = testModule.asEffectiveStatement().requireDeclared();

        assertEquals(1, moduleStatement.getImports().size());
        final var importStatement = moduleStatement.getImports().iterator().next();
        assertEquals(Unqualified.of("imported-module-declared-test"), importStatement.argument());
        assertEquals("imdt", importStatement.getPrefix().argument());
        assertEquals(revision, importStatement.getRevisionDate().argument());

        assertEquals("test description", moduleStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", moduleStatement.getReference().orElseThrow().argument());
        assertEquals("test organization", moduleStatement.getOrganization().orElseThrow().argument());
        assertEquals("test contact", moduleStatement.getContact().orElseThrow().argument());

        assertEquals(1, moduleStatement.getRevisions().size());
        final var revisionStatement = moduleStatement.getRevisions().iterator().next();
        assertEquals(revision, revisionStatement.argument());
        assertEquals("test description", revisionStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", revisionStatement.getReference().orElseThrow().argument());

        assertEquals(1, moduleStatement.getExtensions().size());
        final var extensionStatement = moduleStatement.getExtensions().iterator().next();
        assertEquals(Status.CURRENT, extensionStatement.getStatus().orElseThrow().argument());
        assertEquals("test description", extensionStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", extensionStatement.getReference().orElseThrow().argument());
        final var argumentStatement = extensionStatement.getArgument();
        assertEquals("ext-argument", argumentStatement.argument().getLocalName());
        assertTrue(argumentStatement.yinElement().argument());

        assertEquals(2, moduleStatement.getFeatures().size());
        final var featureStatement = moduleStatement.getFeatures().iterator().next();
        assertEquals(Status.CURRENT, featureStatement.getStatus().orElseThrow().argument());
        assertEquals("test description", featureStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", featureStatement.getReference().orElseThrow().argument());
        assertEquals("test-feature", featureStatement.argument().getLocalName());
        assertEquals(1, featureStatement.getIfFeatures().size());

        assertEquals(2, moduleStatement.getIdentities().size());
        final var identityStatement = moduleStatement.getIdentities().stream()
            .filter(identity -> identity.argument().getLocalName().equals("test-id"))
            .findFirst()
            .orElseThrow();

        assertEquals("test-base-id", identityStatement.getBases().iterator().next().argument().getLocalName());
        assertEquals(Status.CURRENT, identityStatement.getStatus().orElseThrow().argument());
        assertEquals("test description", identityStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", identityStatement.getReference().orElseThrow().argument());
        assertEquals("test-id", identityStatement.argument().getLocalName());

        assertEquals(1, moduleStatement.getTypedefs().size());
        final var typedefStatement = moduleStatement.getTypedefs().iterator().next();
        assertEquals(Status.CURRENT, typedefStatement.getStatus().orElseThrow().argument());
        assertEquals("test description", typedefStatement.getDescription().orElseThrow().argument());
        assertEquals("test reference", typedefStatement.getReference().orElseThrow().argument());
        assertEquals("test-typedef", typedefStatement.argument().getLocalName());
        assertEquals("int32", typedefStatement.getType().rawArgument());
        assertEquals("meter", typedefStatement.getUnits().orElseThrow().argument());
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

        final var containerStatementWhen = containerStatement.getWhenStatement().orElseThrow();
        assertNotNull(containerStatementWhen);

        final var containerStatementIfFeatures = containerStatement.getIfFeatures();
        assertNotNull(containerStatementIfFeatures);
        assertEquals(1, containerStatementIfFeatures.size());

        final var containerStatementMusts = containerStatement.getMustStatements();
        assertNotNull(containerStatementMusts);
        assertEquals(1, containerStatementMusts.size());

        final var containerStatementPresence = containerStatement.getPresence();
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
