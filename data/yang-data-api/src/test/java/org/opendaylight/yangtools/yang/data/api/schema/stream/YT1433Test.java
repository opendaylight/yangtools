/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1433Test {
    private static final String BAR_YANG = """
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
        }""";
    private static final String FOO_YANG = """
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
        }""";
    private static EffectiveModelContext CONTEXT;

    @BeforeAll
    public static void beforeAll() {
        CONTEXT = YangParserTestUtils.parseYang(BAR_YANG, FOO_YANG);
    }

    @Test
    public void testContainerAugmentContainer() throws IOException {
        final FormattingNormalizedNodeStreamWriter streamWriter = new FormattingNormalizedNodeStreamWriter();

        final QName bar = QName.create("foo", "bar");
        final QName baz = QName.create("foo", "baz");

        final YangInstanceIdentifier path = YangInstanceIdentifier.builder()
            .node(QName.create("foo", "foo"))
            .node(AugmentationIdentifier.create(Set.of(bar)))
            .node(bar)
            .node(AugmentationIdentifier.create(Set.of(baz)))
            .node(baz)
            .build();

        try (var iidWriter = YangInstanceIdentifierWriter.open(streamWriter, CONTEXT, path)) {
            try (var nnWriter = new NormalizedNodeWriter(streamWriter)) {
                // No-op
            }
        }

        assertEquals("""
            (foo)foo(container)
              AugmentationIdentifier{childNames=[(foo)bar]}(augmentation)
                (foo)bar(container)
                  AugmentationIdentifier{childNames=[(foo)baz]}(augmentation)
                    (foo)baz(container)
                    (end)
                  (end)
                (end)
              (end)
            (end)
            """, streamWriter.result());
    }

    @Test
    public void testChoiceAugmentCointainer() throws IOException {
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
