/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;
import static org.junit.Assert.assertNotNull;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.binding.data.codec.impl.MissingClassInLoadingStrategyException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * This sets of tests are designed in way, that schema context contains models for all
 * augmentations, but backing class loading strategy is not aware of some of the classes, and
 * becames aware of them after codec was used.
 *
 * Idea of this suite is to test that codecs will work even if situation like this happens.
 *
 */
public class AugmentationClassDiscoveredAfterCodecTest {

    private SchemaContext schemaContext;
    private BindingRuntimeContext runtimeContext;
    private ClassExcludingClassLoadingStrategy mockedContext;
    private BindingNormalizedNodeCodecRegistry registry;

    @Before
    public void setup() {
        final ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(BindingReflections.loadModuleInfos());
        mockedContext = new ClassExcludingClassLoadingStrategy(ctx);
        schemaContext = ctx.tryToCreateSchemaContext().get();
        runtimeContext = BindingRuntimeContext.create(mockedContext, schemaContext);
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(runtimeContext);
    }

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY = BA_TOP_LEVEL_LIST
            .augmentation(TreeLeafOnlyAugment.class);



    @Test(expected = MissingClassInLoadingStrategyException.class)
    public void testCorrectExceptionThrown() {
        materializeWithExclusions(TreeLeafOnlyAugment.class, TreeComplexUsesAugment.class);
        registry.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY);
    }


    @Test
    public void testUsingBindingInstanceIdentifier() {
        materializeWithExclusions(TreeLeafOnlyAugment.class, TreeComplexUsesAugment.class);
        mockedContext.includeClass(TreeLeafOnlyAugment.class);
        final YangInstanceIdentifier domYY = registry.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY);
        assertNotNull(domYY);
    }

    @Test
    public void testUsingBindingData() {
        materializeWithExclusions(TreeLeafOnlyAugment.class, TreeComplexUsesAugment.class);
        mockedContext.includeClass(TreeLeafOnlyAugment.class);
        final TopLevelList data =
                new TopLevelListBuilder()
                        .setKey(TOP_FOO_KEY)
                        .addAugmentation(TreeLeafOnlyAugment.class,
                                new TreeLeafOnlyAugmentBuilder().setSimpleValue("foo").build()).build();
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> domData =
                registry.toNormalizedNode(BA_TOP_LEVEL_LIST, data);
        assertNotNull(domData);
    }


    private void materializeWithExclusions(final Class<?>... clzToExclude) {
        for (final Class<?> clz : clzToExclude) {
            mockedContext.excludeClass(clz);
        }
        registry.toYangInstanceIdentifier(BA_TOP_LEVEL_LIST);
    }

    private static class ClassExcludingClassLoadingStrategy extends GeneratedClassLoadingStrategy {

        private final Set<String> exclusions = new HashSet<>();
        private final GeneratedClassLoadingStrategy delegate;

        void excludeClass(final Class<?> clz) {
            exclusions.add(clz.getName());
        }

        void includeClass(final Class<?> clz) {
            exclusions.remove(clz.getName());
        }

        protected ClassExcludingClassLoadingStrategy(final GeneratedClassLoadingStrategy delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
        }

        @Override
        public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
            if (exclusions.contains(fullyQualifiedName)) {
                throw new ClassNotFoundException(String.format("Class %s is not available for test reasons.",
                        fullyQualifiedName));
            }
            return delegate.loadClass(fullyQualifiedName);
        }
    }
}
