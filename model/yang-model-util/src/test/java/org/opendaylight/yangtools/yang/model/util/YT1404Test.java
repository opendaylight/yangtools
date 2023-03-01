/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1404Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName BAZ = QName.create("foo", "baz");

    @Test
    void testDeviatedEffectiveAugmentationSchema() {
        final var module = YangParserTestUtils.parseYangResourceDirectory("/yt1404").findModule("foo").orElseThrow();
        final var augment = Iterables.getOnlyElement(module.getAugmentations());
        assertEquals(2, augment.getChildNodes().size());
        assertThat(augment.dataChildByName(BAR), instanceOf(LeafSchemaNode.class));
        assertThat(augment.dataChildByName(BAZ), instanceOf(LeafSchemaNode.class));

        final var foo = module.getDataChildByName(FOO);
        assertThat(foo, instanceOf(ContainerSchemaNode.class));
        final var fooCont = (ContainerSchemaNode) foo;
        assertEquals(1, fooCont.getChildNodes().size());
        final var fooBar = fooCont.dataChildByName(BAR);
        assertThat(fooBar, instanceOf(LeafSchemaNode.class));

        final var fooAugment = Iterables.getOnlyElement(fooCont.getAvailableAugmentations());
        assertSame(augment, fooAugment);

        final var effectiveAug = new EffectiveAugmentationSchema(augment, fooCont);
        assertEquals(1, effectiveAug.getChildNodes().size());
        assertSame(fooBar, effectiveAug.getDataChildByName(BAR));
    }
}
