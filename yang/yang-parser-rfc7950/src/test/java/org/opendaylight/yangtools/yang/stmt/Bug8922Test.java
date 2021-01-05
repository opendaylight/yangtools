/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public class Bug8922Test {
    private static final String NS = "foo";

    @Test
    public void testAllFeaturesSupported() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSource("/bugs/bug8922/foo.yang");
        assertNotNull(context);
        final SchemaNode findNode = findNode(context, qN("target"), qN("my-con"));
        assertTrue(findNode instanceof ContainerSchemaNode);
        assertEquals(Optional.of("New description"), findNode.getDescription());
    }

    @Test
    public void testNoFeatureSupported() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSource("/bugs/bug8922/foo.yang",
                ImmutableSet.of());
        assertNotNull(context);
        try {
            final SchemaNode findNode = findNode(context, qN("target"), qN("my-con"));
            assertNull(findNode);
        } catch (final IllegalArgumentException e) {
            assertEquals(String.format("Schema tree child %s not present", qN("my-con")), e.getMessage());
        }
        assertTrue(context.getAvailableAugmentations().isEmpty());
    }

    private static SchemaNode findNode(final EffectiveModelContext context, final QName... qnames) {
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        for (final QName qname : qnames) {
            stack.enterSchemaTree(qname);
        }
        return SchemaContextUtil.findDataSchemaNode(context, stack);
    }

    private static QName qN(final String localName) {
        return QName.create(NS, localName);
    }
}
