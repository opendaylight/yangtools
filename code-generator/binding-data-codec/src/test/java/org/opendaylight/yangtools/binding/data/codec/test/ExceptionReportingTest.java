/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;

import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.binding.data.codec.impl.IncorrectNestingException;
import org.opendaylight.yangtools.binding.data.codec.impl.MissingSchemaException;
import org.opendaylight.yangtools.binding.data.codec.impl.MissingSchemaForClassException;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;


public class ExceptionReportingTest {


    private static final BindingNormalizedNodeCodecRegistry EMPTY_SCHEMA_CODEC = codec();
    private static final BindingNormalizedNodeCodecRegistry ONLY_TOP_CODEC = codec(Top.class);
    private static final BindingNormalizedNodeCodecRegistry FULL_CODEC = codec(TreeComplexUsesAugment.class);

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier
            .builder(Top.class).child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY =
            BA_TOP_LEVEL_LIST.augmentation(TreeLeafOnlyAugment.class);

    private static final QName TOP_QNAME =
            QName.create("urn:opendaylight:params:xml:ns:yang:yangtools:test:binding", "2014-07-01", "top");
    private static final YangInstanceIdentifier BI_TOP_PATH = YangInstanceIdentifier.builder().node(TOP_QNAME).build();
    private static final YangInstanceIdentifier BI_TREE_LEAF_ONLY = FULL_CODEC.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY);


    @Test(expected=MissingSchemaException.class)
    public void testDOMTop() {
        EMPTY_SCHEMA_CODEC.fromYangInstanceIdentifier(BI_TOP_PATH);
    }

    @Test(expected=MissingSchemaException.class)
    public void testDOMAugment() {
        EMPTY_SCHEMA_CODEC.fromYangInstanceIdentifier(BI_TREE_LEAF_ONLY);
    }

    @Test(expected=MissingSchemaForClassException.class)
    public void testBindingTop() {
        EMPTY_SCHEMA_CODEC.toYangInstanceIdentifier(BA_TOP_LEVEL_LIST);
    }

    @Test(expected=MissingSchemaForClassException.class)
    public void testBindingAugment() {
        ONLY_TOP_CODEC.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY);
    }

    @Test(expected=IncorrectNestingException.class)
    public void testBindingSkippedRoot() {
        FULL_CODEC.toYangInstanceIdentifier(InstanceIdentifier.create(TopLevelList.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test(expected=IncorrectNestingException.class)
    public void testBindingIncorrectAugment() {
        FULL_CODEC.toYangInstanceIdentifier(InstanceIdentifier.create(Top.class).augmentation((Class) TreeComplexUsesAugment.class));
    }


    private static BindingNormalizedNodeCodecRegistry codec(final Class<?>... classes ) {
        final ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        for(final Class<?> clazz : classes) {
            YangModuleInfo modInfo;
            try {
                modInfo = BindingReflections.getModuleInfo(clazz);
                ctx.registerModuleInfo(modInfo);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
        final SchemaContext schema = ctx.tryToCreateSchemaContext().get();
        final BindingRuntimeContext runtimeCtx = BindingRuntimeContext.create(ctx, schema);
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        final BindingNormalizedNodeCodecRegistry registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(runtimeCtx);
        return registry;
    }

}
