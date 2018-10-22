/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class YT911Test {
    private static final QName FOO = QName.create("foo", "2018-10-22", "foo");
    private static final QName BAR = QName.create(FOO, "bar");

    @Test
    public void testAugmentationConfig() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSource("/bugs/YT911/foo.yang");
        final DataSchemaNode foo = context.findDataChildByName(FOO).get();
        assertFalse(foo.isConfiguration());
        assertTrue(foo instanceof ContainerSchemaNode);

        // Instantiated node
        final DataSchemaNode bar = ((ContainerSchemaNode) foo).findDataTreeChild(BAR).get();
        assertFalse(bar.isConfiguration());
        assertTrue(foo instanceof ContainerSchemaNode);

        // Original augmentation node
        final AugmentationSchemaNode aug = ((ContainerSchemaNode) foo).getAvailableAugmentations().iterator().next();
        final DataSchemaNode augBar = aug.findDataTreeChild(BAR).get();
        assertTrue(augBar.isConfiguration());
        assertTrue(foo instanceof ContainerSchemaNode);
    }
}
