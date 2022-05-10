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
    private static EffectiveModelContext CONTEXT;

    @BeforeAll
    public static void beforeAll() {
        CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/YT1433");
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

        assertEquals("(foo)foo(container)\n"
            + "  AugmentationIdentifier{childNames=[(foo)bar]}(augmentation)\n"
            + "    (foo)bar(container)\n"
            + "      AugmentationIdentifier{childNames=[(foo)baz]}(augmentation)\n"
            + "        (foo)baz(container)\n"
            + "        (end)\n"
            + "      (end)\n"
            + "    (end)\n"
            + "  (end)\n"
            + "(end)\n", streamWriter.result());
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

        assertEquals("(bar)foo(choice)\n"
            + "  (bar)bar(container)\n"
            + "    (bar)baz(container)\n"
            + "    (end)\n"
            + "  (end)\n"
            + "(end)\n", streamWriter.result());
    }
}
