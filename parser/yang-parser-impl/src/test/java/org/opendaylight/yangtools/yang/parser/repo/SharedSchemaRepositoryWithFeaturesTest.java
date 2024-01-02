/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class SharedSchemaRepositoryWithFeaturesTest {
    @Test
    public void testSharedSchemaRepositoryWithSomeFeaturesSupported() throws Exception {
        final var supportedFeatures = FeatureSet.of(QName.create("foobar-namespace", "test-feature-1"));

        final var sharedSchemaRepository = new SharedSchemaRepository("shared-schema-repo-with-features-test");

        final var foobar = getImmediateYangSourceProviderFromResource(
            "/if-feature-resolution-test/shared-schema-repository/foobar.yang");
        foobar.register(sharedSchemaRepository);
        foobar.setResult();

        final var testSchemaContextFuture = sharedSchemaRepository.createEffectiveModelContextFactory(
                SchemaContextFactoryConfiguration.builder().setSupportedFeatures(supportedFeatures).build())
                .createEffectiveModelContext(foobar.getId());
        assertTrue(testSchemaContextFuture.isDone());
        assertSchemaContext(testSchemaContextFuture.get(), 1);

        final var module = testSchemaContextFuture.get().findModules("foobar").iterator().next();
        assertNotNull(module);
        assertEquals(2, module.getChildNodes().size());

        final var testContainerA = assertInstanceOf(ContainerSchemaNode.class,
            module.dataChildByName(QName.create(module.getQNameModule(), "test-container-a")));
        assertInstanceOf(LeafSchemaNode.class,
            testContainerA.dataChildByName(QName.create(module.getQNameModule(), "test-leaf-a")));

        assertNull(module.dataChildByName(QName.create(module.getQNameModule(), "test-container-b")));

        final var testContainerC = assertInstanceOf(ContainerSchemaNode.class,
            module.dataChildByName(QName.create(module.getQNameModule(), "test-container-c")));
        assertInstanceOf(LeafSchemaNode.class,
            testContainerC.dataChildByName(QName.create(module.getQNameModule(), "test-leaf-c")));
    }

    @Test
    public void testSharedSchemaRepositoryWithAllFeaturesSupported() throws Exception {
        final var sharedSchemaRepository = new SharedSchemaRepository("shared-schema-repo-with-features-test");

        final var foobar = getImmediateYangSourceProviderFromResource(
            "/if-feature-resolution-test/shared-schema-repository/foobar.yang");
        foobar.register(sharedSchemaRepository);
        foobar.setResult();

        final var fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final var testSchemaContextFuture = fact.createEffectiveModelContext(foobar.getId());
        assertTrue(testSchemaContextFuture.isDone());
        assertSchemaContext(testSchemaContextFuture.get(), 1);

        final var module = testSchemaContextFuture.get().findModules("foobar").iterator().next();
        assertNotNull(module);
        assertEquals(3, module.getChildNodes().size());

        final var testContainerA = assertInstanceOf(ContainerSchemaNode.class,
            module.dataChildByName(QName.create(module.getQNameModule(), "test-container-a")));
        assertInstanceOf(LeafSchemaNode.class,
            testContainerA.dataChildByName(QName.create(module.getQNameModule(), "test-leaf-a")));

        final var testContainerB = assertInstanceOf(ContainerSchemaNode.class,
            module.dataChildByName(QName.create(module.getQNameModule(), "test-container-b")));
        assertInstanceOf(LeafSchemaNode.class,
            testContainerB.dataChildByName(QName.create(module.getQNameModule(), "test-leaf-b")));

        final var testContainerC = assertInstanceOf(ContainerSchemaNode.class,
            module.dataChildByName(QName.create(module.getQNameModule(), "test-container-c")));
        assertInstanceOf(LeafSchemaNode.class,
            testContainerC.dataChildByName(QName.create(module.getQNameModule(), "test-leaf-c")));
    }

    @Test
    public void testSharedSchemaRepositoryWithNoFeaturesSupported() throws Exception {
        final var sharedSchemaRepository = new SharedSchemaRepository("shared-schema-repo-with-features-test");

        final var foobar = getImmediateYangSourceProviderFromResource(
            "/if-feature-resolution-test/shared-schema-repository/foobar.yang");
        foobar.register(sharedSchemaRepository);
        foobar.setResult();

        final var testSchemaContextFuture = sharedSchemaRepository.createEffectiveModelContextFactory(
            SchemaContextFactoryConfiguration.builder().setSupportedFeatures(FeatureSet.of()).build())
            .createEffectiveModelContext(foobar.getId());
        assertTrue(testSchemaContextFuture.isDone());
        assertSchemaContext(testSchemaContextFuture.get(), 1);

        final var module = testSchemaContextFuture.get().findModules("foobar").iterator().next();
        assertNotNull(module);
        assertEquals(1, module.getChildNodes().size());

        final var testContainerC = assertInstanceOf(ContainerSchemaNode.class,
            module.dataChildByName(QName.create(module.getQNameModule(), "test-container-c")));
        assertInstanceOf(LeafSchemaNode.class,
            testContainerC.dataChildByName(QName.create(module.getQNameModule(), "test-leaf-c")));
    }

    private static SettableSchemaProvider<YangIRSchemaSource> getImmediateYangSourceProviderFromResource(
            final String resourceName) throws Exception {
        return SettableSchemaProvider.createImmediate(
            TextToIRTransformer.transformText(YangTextSchemaSource.forResource(resourceName)),
            YangIRSchemaSource.class);
    }

    private static void assertSchemaContext(final SchemaContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModules().size());
    }
}
