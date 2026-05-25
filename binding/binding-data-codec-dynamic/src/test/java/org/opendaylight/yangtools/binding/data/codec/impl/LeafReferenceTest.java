/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.ThirdParty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexLeaves;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexLeavesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Int32StringUnion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.DataObjectReference;

class LeafReferenceTest extends AbstractBindingCodecTest {
    @Test
    void testCaseWithLeafReferencesType() {
        final var augment = new TreeComplexLeavesBuilder()
            .setIdentity(ThirdParty.VALUE)
            .setIdentityRef(ThirdParty.VALUE)
            .setSimpleType(10)
            .setSimpleTypeRef(10)
            .setSchemaUnawareUnion(new Int32StringUnion("foo"))
            .setSchemaUnawareUnionRef(new Int32StringUnion(10))
            .build();
        final var list = new TopLevelListBuilder()
            .setName("foo")
            .addAugmentation(augment)
            .build();
        final var dom = codecContext.toNormalizedDataObject(
            DataObjectReference.builder(Top.class).child(TopLevelList.class, new TopLevelListKey("foo")).build(), list);
        final var readed = codecContext.fromNormalizedNode(dom.path(), dom.node());
        assertNotNull(readed);
        final var readAugment = assertInstanceOf(TopLevelList.class,  readed.getValue())
            .augmentation(TreeComplexLeaves.class);
        assertEquals(augment, readAugment);
    }
}
