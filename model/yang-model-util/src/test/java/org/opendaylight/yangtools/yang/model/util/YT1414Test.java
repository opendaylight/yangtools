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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1414Test {
    private static final QName MY_CONTAINER = QName.create("uri:my-module", "2014-10-07", "my-container");
    private static final QName MY_LIST = QName.create(MY_CONTAINER, "my-list");
    private static final Absolute MY_LIST_ID = Absolute.of(MY_CONTAINER, MY_LIST);

    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create(FOO, "bar");
    private static final Absolute BAR_FOO_ID = Absolute.of(BAR, FOO);

    @Test
    public void testToFromSchemaTreeInference() {
        final var stack = SchemaInferenceStack.of(
            YangParserTestUtils.parseYangResourceDirectory("/schema-context-util"));
        stack.enterSchemaTree(MY_LIST_ID);
        final var inference = stack.toSchemaTreeInference();
        assertThat(inference, instanceOf(DefaultSchemaTreeInference.class));
        assertEquals(MY_LIST_ID, inference.toSchemaNodeIdentifier());
        assertEquals(MY_LIST_ID, stack.toSchemaNodeIdentifier());
        assertEquals(MY_LIST_ID, SchemaInferenceStack.ofInference(inference).toSchemaNodeIdentifier());
    }

    @Test
    public void testOfUntrustedSchemaTreeInference() {
        final var context = YangParserTestUtils.parseYangResource("/yt1414.yang");
        final var foo = context.findSchemaTreeNode(Absolute.of(FOO)).orElseThrow();
        final var bar = context.findSchemaTreeNode(Absolute.of(BAR)).orElseThrow();
        final var barFoo = context.findSchemaTreeNode(BAR_FOO_ID).orElseThrow();

        // Let's check that correct thing works out
        final var correct = DefaultSchemaTreeInference.of(context, BAR_FOO_ID);
        assertEquals(List.of(bar, barFoo), correct.statementPath());
        assertEquals(correct.statementPath(),
            SchemaInferenceStack.ofUntrusted(correct).toSchemaTreeInference().statementPath());

        // Now let's try some abuse: we use 'foo' instead of 'barFoo', created unsafely ...
        final var incorrect = DefaultSchemaTreeInference.unsafeOf(context, ImmutableList.of(bar, foo));
        // ... the default non-verify method is happy to oblige ...
        assertEquals(incorrect.statementPath(),
            SchemaInferenceStack.ofInference(incorrect).toSchemaTreeInference().statementPath());
        // ... but ofUntrusted() will reject it
        assertEquals("Provided " + incorrect + " is not consistent with resolved path " + correct,
            assertThrows(IllegalArgumentException.class, () -> SchemaInferenceStack.ofUntrusted(incorrect))
                .getMessage());
    }
}
