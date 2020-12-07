/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class SharedSchemaRepositoryWithFeaturesTest {

    @Test
    public void testSharedSchemaRepositoryWithSomeFeaturesSupported() throws Exception {
        final Set<QName> supportedFeatures = ImmutableSet.of(QName.create("foobar-namespace", "test-feature-1"));

        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-with-features-test");

        final SettableSchemaProvider<IRSchemaSource> foobar = getImmediateYangSourceProviderFromResource(
            "/if-feature-resolution-test/shared-schema-repository/foobar.yang");
        foobar.register(sharedSchemaRepository);
        foobar.setResult();

        final ListenableFuture<EffectiveModelContext> testSchemaContextFuture =
                sharedSchemaRepository.createEffectiveModelContextFactory(
                SchemaContextFactoryConfiguration.builder().setSupportedFeatures(supportedFeatures).build())
                .createEffectiveModelContext(foobar.getId());
        assertTrue(testSchemaContextFuture.isDone());
        assertSchemaContext(testSchemaContextFuture.get(), 1);

        final Module module = testSchemaContextFuture.get().findModules("foobar").iterator().next();
        assertNotNull(module);
        assertEquals(2, module.getChildNodes().size());

        final ContainerSchemaNode testContainerA = (ContainerSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-container-a"));
        assertNotNull(testContainerA);
        final LeafSchemaNode testLeafA = (LeafSchemaNode) testContainerA.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf-a"));
        assertNotNull(testLeafA);

        final ContainerSchemaNode testContainerB = (ContainerSchemaNode) module.dataChildByName(
                QName.create(module.getQNameModule(), "test-container-b"));
        assertNull(testContainerB);

        final ContainerSchemaNode testContainerC = (ContainerSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-container-c"));
        assertNotNull(testContainerC);
        final LeafSchemaNode testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf-c"));
        assertNotNull(testLeafC);
    }

    @Test
    public void testSharedSchemaRepositoryWithAllFeaturesSupported() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-with-features-test");

        final SettableSchemaProvider<IRSchemaSource> foobar = getImmediateYangSourceProviderFromResource(
            "/if-feature-resolution-test/shared-schema-repository/foobar.yang");
        foobar.register(sharedSchemaRepository);
        foobar.setResult();

        final EffectiveModelContextFactory fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final ListenableFuture<EffectiveModelContext> testSchemaContextFuture =
                fact.createEffectiveModelContext(foobar.getId());
        assertTrue(testSchemaContextFuture.isDone());
        assertSchemaContext(testSchemaContextFuture.get(), 1);

        final Module module = testSchemaContextFuture.get().findModules("foobar").iterator().next();
        assertNotNull(module);
        assertEquals(3, module.getChildNodes().size());

        final ContainerSchemaNode testContainerA = (ContainerSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-container-a"));
        assertNotNull(testContainerA);
        final LeafSchemaNode testLeafA = (LeafSchemaNode) testContainerA.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf-a"));
        assertNotNull(testLeafA);

        final ContainerSchemaNode testContainerB = (ContainerSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-container-b"));
        assertNotNull(testContainerB);
        final LeafSchemaNode testLeafB = (LeafSchemaNode) testContainerB.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf-b"));
        assertNotNull(testLeafB);

        final ContainerSchemaNode testContainerC = (ContainerSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-container-c"));
        assertNotNull(testContainerC);
        final LeafSchemaNode testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf-c"));
        assertNotNull(testLeafC);
    }

    @Test
    public void testSharedSchemaRepositoryWithNoFeaturesSupported() throws Exception {
        final SharedSchemaRepository sharedSchemaRepository = new SharedSchemaRepository(
                "shared-schema-repo-with-features-test");

        final SettableSchemaProvider<IRSchemaSource> foobar = getImmediateYangSourceProviderFromResource(
            "/if-feature-resolution-test/shared-schema-repository/foobar.yang");
        foobar.register(sharedSchemaRepository);
        foobar.setResult();

        final ListenableFuture<EffectiveModelContext> testSchemaContextFuture =
                sharedSchemaRepository.createEffectiveModelContextFactory(
                    SchemaContextFactoryConfiguration.builder().setSupportedFeatures(ImmutableSet.of()).build())
                .createEffectiveModelContext(foobar.getId());
        assertTrue(testSchemaContextFuture.isDone());
        assertSchemaContext(testSchemaContextFuture.get(), 1);

        final Module module = testSchemaContextFuture.get().findModules("foobar").iterator().next();
        assertNotNull(module);
        assertEquals(1, module.getChildNodes().size());

        final ContainerSchemaNode testContainerC = (ContainerSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-container-c"));
        assertNotNull(testContainerC);
        final LeafSchemaNode testLeafC = (LeafSchemaNode) testContainerC.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf-c"));
        assertNotNull(testLeafC);
    }

    private static SettableSchemaProvider<IRSchemaSource> getImmediateYangSourceProviderFromResource(
            final String resourceName) throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToIRTransformer.transformText(yangSource),
            IRSchemaSource.class);
    }

    private static void assertSchemaContext(final SchemaContext schemaContext, final int moduleSize) {
        assertNotNull(schemaContext);
        assertEquals(moduleSize, schemaContext.getModules().size());
    }
}
