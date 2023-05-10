/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    private static final String DEV_YANG = """
            module dev {
              namespace dev;
              prefix dev;
              import foo {
                prefix foo;
              }
              deviation /foo:foo/foo:baz {
                deviate not-supported;
              }
            }""";
    private static final String FOO_YANG = """
            module foo {
              namespace foo;
              prefix foo;
              container foo;
              augment /foo {
                leaf bar {
                  type string;
                }
                leaf baz {
                  type string;
                }
              }
            }""";

    @Test
    void testDeviatedEffectiveAugmentationSchema() {
        final var module = YangParserTestUtils.parseYang(DEV_YANG, FOO_YANG).findModule("foo").orElseThrow();
        final var augment = Iterables.getOnlyElement(module.getAugmentations());
        assertEquals(2, augment.getChildNodes().size());
        assertInstanceOf(LeafSchemaNode.class, augment.dataChildByName(BAR));
        assertInstanceOf(LeafSchemaNode.class, augment.dataChildByName(BAZ));

        final var foo = assertInstanceOf(ContainerSchemaNode.class, module.getDataChildByName(FOO));
        assertEquals(1, foo.getChildNodes().size());
        final var fooBar = assertInstanceOf(LeafSchemaNode.class, foo.dataChildByName(BAR));

        final var fooAugment = Iterables.getOnlyElement(foo.getAvailableAugmentations());
        assertSame(augment, fooAugment);

        final var effectiveAug = new EffectiveAugmentationSchema(augment, foo);
        assertEquals(1, effectiveAug.getChildNodes().size());
        assertSame(fooBar, effectiveAug.getDataChildByName(BAR));
    }
}
