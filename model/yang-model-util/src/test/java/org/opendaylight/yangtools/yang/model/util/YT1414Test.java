/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaTreeInference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1414Test {
    private static EffectiveModelContext context;

    private static final QName FOO1 = QName.create("uri:my-module", "2014-10-07", "my-container");

    @BeforeClass
    public static void beforeClass() {
        context = YangParserTestUtils.parseYangResourceDirectory("/schema-context-util");
    }

    @Test
    public void basicTest() {
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context);
        stack.enterDataTree(FOO1);
        final SchemaTreeInference tree = stack.toSchemaTreeInference();
        final SchemaInferenceStack stackFromTree = SchemaInferenceStack.ofInference(tree);
        assertTrue(tree.toSchemaNodeIdentifier().getNodeIdentifiers().contains(FOO1));
        assertTrue(stackFromTree.toSchemaNodeIdentifier().getNodeIdentifiers().contains(FOO1));
    }
}
