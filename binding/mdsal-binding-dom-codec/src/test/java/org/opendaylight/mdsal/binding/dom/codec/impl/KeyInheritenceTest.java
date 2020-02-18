/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Map.Entry;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.Def;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.DefBuilder;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp.LstBuilder;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp.LstKey;
import org.opendaylight.yang.gen.v1.mdsal442.keyuse.norev.Use;
import org.opendaylight.yang.gen.v1.mdsal442.keyuse.norev.UseBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class KeyInheritenceTest extends AbstractBindingCodecTest {
    private static final LstKey KEY = new LstKey("foo");
    private static final InstanceIdentifier<Def> DEF_IID = InstanceIdentifier.create(Def.class);
    private static final InstanceIdentifier<Use> USE_IID = InstanceIdentifier.create(Use.class);

    private static final Def DEF = new DefBuilder()
            .setLst(ImmutableMap.of(KEY, new LstBuilder().setFoo("foo").withKey(KEY).build()))
            .build();
    private static final Use USE = new UseBuilder()
            .setLst(ImmutableMap.of(KEY, new LstBuilder().setFoo("foo").withKey(KEY).build()))
            .build();

    @Test
    public void testFromBinding() {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> domDef = registry.toNormalizedNode(DEF_IID, DEF);
        Entry<InstanceIdentifier<?>, DataObject> entry = registry.fromNormalizedNode(domDef.getKey(),
            domDef.getValue());
        assertEquals(DEF_IID, entry.getKey());
        final Def codecDef = (Def) entry.getValue();

        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> domUse = registry.toNormalizedNode(USE_IID, USE);
        entry = registry.fromNormalizedNode(domUse.getKey(), domUse.getValue());
        assertEquals(USE_IID, entry.getKey());
        final Use codecUse = (Use) entry.getValue();

        Use copiedUse = new UseBuilder(DEF).build();
        assertEquals(USE, copiedUse);
        assertEquals(domUse.getValue(), registry.toNormalizedNode(USE_IID, copiedUse).getValue());
        copiedUse = new UseBuilder(codecDef).build();
        assertEquals(USE, copiedUse);
        assertEquals(domUse.getValue(), registry.toNormalizedNode(USE_IID, copiedUse).getValue());
        copiedUse = new UseBuilder(codecUse).build();
        assertEquals(USE, copiedUse);
        assertEquals(domUse.getValue(), registry.toNormalizedNode(USE_IID, copiedUse).getValue());
    }
}
