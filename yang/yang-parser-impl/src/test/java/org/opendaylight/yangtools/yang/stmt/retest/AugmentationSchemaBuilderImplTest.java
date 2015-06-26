/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.impl.AugmentationSchemaBuilderImpl;

public class AugmentationSchemaBuilderImplTest {

    private AugmentationSchemaBuilderImpl augmentSchemaBuilderImpl;
    private AugmentationSchemaBuilderImpl augmentSchemaBuilderImpl2;
    private AugmentationSchemaBuilderImpl augmentSchemaBuilderImpl3;
    private AugmentationSchemaBuilderImpl augmentSchemaBuilderImpl4;
    private AugmentationSchema augmentSchema;

    @Before
    public void init() {
        augmentSchemaBuilderImpl = new AugmentationSchemaBuilderImpl("test-module", 10, "augment-test/rpc", SchemaPath.ROOT, 1);
        augmentSchemaBuilderImpl2 = new AugmentationSchemaBuilderImpl("test-module", 10, "augment-test/rpc2", SchemaPath.ROOT, 1);
        augmentSchemaBuilderImpl3 = augmentSchemaBuilderImpl;
        augmentSchemaBuilderImpl4 = new AugmentationSchemaBuilderImpl("test-module", 10, null, SchemaPath.ROOT, 1);
        augmentSchema = augmentSchemaBuilderImpl.build();
    }

    @Test
    public void testgetPath() {
        assertTrue(Iterables.isEmpty(augmentSchemaBuilderImpl.getPath().getPathFromRoot()));
    }

    @Test
    public void testEquals() {
        assertFalse(augmentSchemaBuilderImpl.equals("test"));
        assertFalse(augmentSchemaBuilderImpl.equals(null));
        assertTrue(augmentSchemaBuilderImpl.equals(augmentSchemaBuilderImpl3));
        assertFalse(augmentSchemaBuilderImpl4.equals(augmentSchemaBuilderImpl));
        assertFalse(augmentSchemaBuilderImpl.equals(augmentSchemaBuilderImpl2));
    }

    @Test
    public void testGetOriginalDefinition() {
        augmentSchema = augmentSchemaBuilderImpl.build();
        Optional<AugmentationSchema> origDefinition = augmentSchema.getOriginalDefinition();
        assertFalse(origDefinition.isPresent());
    }

    @Test
    public void testGetUnknownSchemaNodes() {
        assertTrue(Iterables.isEmpty(augmentSchema.getUnknownSchemaNodes()));
    }
}
