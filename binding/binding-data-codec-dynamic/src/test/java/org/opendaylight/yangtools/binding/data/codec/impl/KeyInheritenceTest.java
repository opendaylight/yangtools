/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.Def;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.DefBuilder;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp.LstBuilder;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp.LstKey;
import org.opendaylight.yang.gen.v1.mdsal442.keyuse.norev.Use;
import org.opendaylight.yang.gen.v1.mdsal442.keyuse.norev.UseBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.util.BindingMap;

class KeyInheritenceTest extends AbstractBindingCodecTest {
    private static final LstKey KEY = new LstKey("foo");
    private static final DataObjectIdentifier<Def> DEF_IID = DataObjectIdentifier.builder(Def.class).build();
    private static final DataObjectIdentifier<Use> USE_IID = DataObjectIdentifier.builder(Use.class).build();

    private static final Def DEF = new DefBuilder()
            .setLst(BindingMap.of(new LstBuilder().setFoo("foo").withKey(KEY).build()))
            .build();
    private static final Use USE = new UseBuilder()
            .setLst(BindingMap.of(new LstBuilder().setFoo("foo").withKey(KEY).build()))
            .build();

    @Test
    void testFromBinding() {
        final var domDef = codecContext.toNormalizedDataObject(DEF_IID, DEF);
        var entry = codecContext.fromNormalizedNode(domDef.path(), domDef.node());
        assertNotNull(entry);
        assertEquals(DEF_IID, entry.getKey());
        final var codecDef = assertInstanceOf(Def.class, entry.getValue());

        final var domUse = codecContext.toNormalizedDataObject(USE_IID, USE);
        entry = codecContext.fromNormalizedNode(domUse.path(), domUse.node());
        assertNotNull(entry);
        assertEquals(USE_IID, entry.getKey());
        final var codecUse = assertInstanceOf(Use.class, entry.getValue());

        var copiedUse = new UseBuilder(DEF).build();
        assertEquals(USE, copiedUse);
        assertEquals(domUse.node(), codecContext.toNormalizedDataObject(USE_IID, copiedUse).node());
        copiedUse = new UseBuilder(codecDef).build();
        assertEquals(USE, copiedUse);
        assertEquals(domUse.node(), codecContext.toNormalizedDataObject(USE_IID, copiedUse).node());
        copiedUse = new UseBuilder(codecUse).build();
        assertEquals(USE, copiedUse);
        assertEquals(domUse.node(), codecContext.toNormalizedDataObject(USE_IID, copiedUse).node());
    }
}
