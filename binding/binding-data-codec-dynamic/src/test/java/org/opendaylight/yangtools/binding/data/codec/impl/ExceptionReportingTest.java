/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.LowestLevel1;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.data.codec.api.IncorrectNestingException;
import org.opendaylight.yangtools.binding.data.codec.api.MissingSchemaException;
import org.opendaylight.yangtools.binding.data.codec.api.MissingSchemaForClassException;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

class ExceptionReportingTest {
    private static final BindingNormalizedNodeSerializer CODEC_WITHOUT_TOP = codec(LowestLevel1.class);
    private static final BindingNormalizedNodeSerializer ONLY_TOP_CODEC = codec(Top.class);
    private static final BindingNormalizedNodeSerializer FULL_CODEC = codec(TreeComplexUsesAugment.class);

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final DataObjectIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = DataObjectIdentifier.builder(Top.class)
        .child(TopLevelList.class, TOP_FOO_KEY)
        .build();
    private static final DataObjectIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY =
        BA_TOP_LEVEL_LIST.toBuilder().augmentation(TreeLeafOnlyAugment.class).build();

    private static final YangInstanceIdentifier BI_TOP_PATH = YangInstanceIdentifier.of(Top.QNAME);

    @Test
    void testDOMTop() {
        assertThrows(MissingSchemaException.class,
            () -> CODEC_WITHOUT_TOP.fromYangInstanceIdentifier(BI_TOP_PATH));
    }

    @Test
    void testDOMAugment() {
        final var yiid = FULL_CODEC.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY);
        assertThrows(MissingSchemaException.class, () -> CODEC_WITHOUT_TOP.fromYangInstanceIdentifier(yiid));
    }

    @Test
    void testBindingTop() {
        assertThrows(MissingSchemaForClassException.class,
            () -> CODEC_WITHOUT_TOP.toYangInstanceIdentifier(BA_TOP_LEVEL_LIST));
    }

    @Test
    void testBindingAugment() {
        assertThrows(MissingSchemaForClassException.class,
            () -> ONLY_TOP_CODEC.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY));
    }

    @Test
    void testBindingSkippedRoot() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final var iid = DataObjectReference.builder((Class) TopLevelList.class).build();
        assertThrows(IncorrectNestingException.class, () -> FULL_CODEC.toYangInstanceIdentifier(iid));
    }

    @Test
    void testBindingIncorrectAugment() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final var iid = DataObjectIdentifier.builder(Top.class)
            .augmentation((Class) TreeComplexUsesAugment.class)
            .build();
        assertThrows(IncorrectNestingException.class, () -> FULL_CODEC.toYangInstanceIdentifier(iid));
    }

    private static BindingNormalizedNodeSerializer codec(final Class<?>... classes) {
        return new BindingCodecContext(BindingRuntimeHelpers.createRuntimeContext(classes));
    }
}
