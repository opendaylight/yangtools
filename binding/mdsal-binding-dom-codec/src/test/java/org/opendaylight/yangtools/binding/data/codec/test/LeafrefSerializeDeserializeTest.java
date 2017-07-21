/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Map.Entry;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;
import org.opendaylight.yang.gen.v1.bug8449.rev180405.Cont;
import org.opendaylight.yang.gen.v1.bug8449.rev180405.Cont.Ref;
import org.opendaylight.yang.gen.v1.bug8449.rev180405.ContBuilder;
import org.opendaylight.yang.gen.v1.bug8449.rev180405.ContInt32;
import org.opendaylight.yang.gen.v1.bug8449.rev180405.ContInt32.RefUnionInt32;
import org.opendaylight.yang.gen.v1.bug8449.rev180405.ContInt32Builder;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class LeafrefSerializeDeserializeTest extends AbstractBindingRuntimeTest {

    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    @Before
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        this.registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        this.registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void listReferenceTest() {
        final YangInstanceIdentifier contYII = YangInstanceIdentifier.builder().node(Cont.QNAME).build();
        final InstanceIdentifier<?> fromYangInstanceIdentifier = this.registry.fromYangInstanceIdentifier(contYII);
        assertNotNull(fromYangInstanceIdentifier);

        final InstanceIdentifier<Cont> BA_II_CONT = InstanceIdentifier.builder(Cont.class).build();
        final Ref refVal = new Ref("myvalue");
        final Cont data = new ContBuilder().setRef(refVal).build();
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode =
                this.registry.toNormalizedNode(BA_II_CONT, data);
        assertNotNull(normalizedNode);

        final Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode =
                this.registry.fromNormalizedNode(contYII, normalizedNode.getValue());
        assertNotNull(fromNormalizedNode);
        final Cont value = (Cont) fromNormalizedNode.getValue();
        assertEquals(refVal, value.getRef());
    }

    @Test
    public void uint32LeafrefTest() {
        final YangInstanceIdentifier contYII = YangInstanceIdentifier.builder().node(ContInt32.QNAME).build();
        final InstanceIdentifier<?> fromYangInstanceIdentifier = this.registry.fromYangInstanceIdentifier(contYII);
        assertNotNull(fromYangInstanceIdentifier);

        final InstanceIdentifier<ContInt32> BA_II_CONT = InstanceIdentifier.builder(ContInt32.class).build();
        final RefUnionInt32 refVal = new RefUnionInt32(5l);
        final ContInt32 data = new ContInt32Builder().setRefUnionInt32(refVal).build();
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode =
                this.registry.toNormalizedNode(BA_II_CONT, data);
        assertNotNull(normalizedNode);

        final Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode =
                this.registry.fromNormalizedNode(contYII, normalizedNode.getValue());
        assertNotNull(fromNormalizedNode);
        final ContInt32 value = (ContInt32) fromNormalizedNode.getValue();
        assertEquals(refVal, value.getRefUnionInt32());
    }
}

