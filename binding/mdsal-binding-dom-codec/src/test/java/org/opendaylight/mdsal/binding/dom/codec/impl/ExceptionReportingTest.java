/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.junit.Test;
import org.opendaylight.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.dom.codec.api.MissingSchemaException;
import org.opendaylight.mdsal.binding.dom.codec.api.MissingSchemaForClassException;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.LowestLevel1;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class ExceptionReportingTest {
    private static final BindingNormalizedNodeSerializer CODEC_WITHOUT_TOP = codec(LowestLevel1.class);
    private static final BindingNormalizedNodeSerializer ONLY_TOP_CODEC = codec(Top.class);
    private static final BindingNormalizedNodeSerializer FULL_CODEC = codec(TreeComplexUsesAugment.class);

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier
            .builder(Top.class).child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY =
            BA_TOP_LEVEL_LIST.augmentation(TreeLeafOnlyAugment.class);

    private static final QName TOP_QNAME = Top.QNAME;
    private static final YangInstanceIdentifier BI_TOP_PATH = YangInstanceIdentifier.builder().node(TOP_QNAME).build();
    private static final YangInstanceIdentifier BI_TREE_LEAF_ONLY = FULL_CODEC.toYangInstanceIdentifier(
        BA_TREE_LEAF_ONLY);

    @Test(expected = MissingSchemaException.class)
    public void testDOMTop() {
        CODEC_WITHOUT_TOP.fromYangInstanceIdentifier(BI_TOP_PATH);
    }

    @Test(expected = MissingSchemaException.class)
    public void testDOMAugment() {
        CODEC_WITHOUT_TOP.fromYangInstanceIdentifier(BI_TREE_LEAF_ONLY);
    }

    @Test(expected = MissingSchemaForClassException.class)
    public void testBindingTop() {
        CODEC_WITHOUT_TOP.toYangInstanceIdentifier(BA_TOP_LEVEL_LIST);
    }

    @Test(expected = MissingSchemaForClassException.class)
    public void testBindingAugment() {
        ONLY_TOP_CODEC.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY);
    }

    @Test(expected = IncorrectNestingException.class)
    public void testBindingSkippedRoot() {
        FULL_CODEC.toYangInstanceIdentifier(InstanceIdentifier.create(TopLevelList.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test(expected = IncorrectNestingException.class)
    public void testBindingIncorrectAugment() {
        FULL_CODEC.toYangInstanceIdentifier(InstanceIdentifier.create(Top.class).augmentation(
            (Class) TreeComplexUsesAugment.class));
    }

    private static BindingNormalizedNodeSerializer codec(final Class<?>... classes) {
        return new BindingCodecContext(BindingRuntimeHelpers.createRuntimeContext(
            new DefaultBindingRuntimeGenerator(), classes));
    }
}
