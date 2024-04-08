/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1433Test {
    private static EffectiveModelContext CONTEXT;

    @BeforeAll
    static void beforeAll() {
        CONTEXT = YangParserTestUtils.parseYang("""
            module foo {
              namespace foo;
              prefix foo;

              container foo;

              augment /foo {
                container bar;
              }

              augment /foo/bar {
                container baz;
              }
            }""", """
            module bar {
              namespace bar;
              prefix bar;

              choice foo;

              augment /foo {
                container bar;
              }

              augment /foo/bar/bar {
                container baz;
              }
            }""");
    }

    @Test
    void testContainerAugmentContainer() throws Exception {
        final FormattingNormalizedNodeStreamWriter streamWriter = new FormattingNormalizedNodeStreamWriter();

        final QName bar = QName.create("foo", "bar");
        final QName baz = QName.create("foo", "baz");

        final YangInstanceIdentifier path = YangInstanceIdentifier.builder()
            .node(QName.create("foo", "foo"))
            .node(bar)
            .node(baz)
            .build();

        try (var iidWriter = YangInstanceIdentifierWriter.open(streamWriter, CONTEXT, path)) {
            try (var nnWriter = new NormalizedNodeWriter(streamWriter)) {
                // No-op
            }
        }

        assertEquals("""
            (foo)foo(container)
              (foo)bar(container)
                (foo)baz(container)
                (end)
              (end)
            (end)
            """, streamWriter.result());
    }

    @Test
    void testChoiceAugmentCointainer() throws Exception {
        final FormattingNormalizedNodeStreamWriter streamWriter = new FormattingNormalizedNodeStreamWriter();

        final QName bar = QName.create("bar", "bar");
        final QName baz = QName.create("bar", "baz");

        final YangInstanceIdentifier path = YangInstanceIdentifier.builder()
            .node(QName.create("bar", "foo"))
            .node(bar)
            .node(baz)
            .build();

        try (var iidWriter = YangInstanceIdentifierWriter.open(streamWriter, CONTEXT, path)) {
            try (var nnWriter = new NormalizedNodeWriter(streamWriter)) {
                // No-op
            }
        }

        assertEquals("""
            (bar)foo(choice)
              (bar)bar(container)
                (bar)baz(container)
                (end)
              (end)
            (end)
            """, streamWriter.result());
    }
}
