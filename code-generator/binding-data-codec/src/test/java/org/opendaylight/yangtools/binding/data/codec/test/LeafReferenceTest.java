/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;

import static org.junit.Assert.assertEquals;

import java.util.Map.Entry;
import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.augment.rev140709.ThirdParty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.augment.rev140709.TreeComplexLeaves;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.augment.rev140709.TreeComplexLeavesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.Int32StringUnion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;


public class LeafReferenceTest extends AbstractBindingRuntimeTest {

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TreeComplexLeaves> BA_TOP_LEVEL_LIST = InstanceIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).augmentation(TreeComplexLeaves.class).toInstance();

    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    public void setup() {
        super.setup();
        JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void testCaseWithLeafReferencesType() {
        TreeComplexLeaves binding = new TreeComplexLeavesBuilder()
            .setIdentity(ThirdParty.class)
            .setIdentityRef(ThirdParty.class)
            .setSimpleType(10)
            .setSimpleTypeRef(10)
            .setSchemaUnawareUnion(new Int32StringUnion("foo"))
            .setSchemaUnawareUnionRef(new Int32StringUnion(10))
        .build();
        Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> dom = registry.toNormalizedNode(BA_TOP_LEVEL_LIST, binding);
        Entry<InstanceIdentifier<?>, DataObject> readed = registry.fromNormalizedNode(dom.getKey(),dom.getValue());
        TreeComplexLeaves readedAugment = ((TreeComplexLeaves) readed.getValue());

        assertEquals(binding,readedAugment);

    }



}
