/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DataTreeConfigurationTest {

    @Test
    public void testDataTreeConfiguration() {
        DataTreeConfiguration.Builder builder = new DataTreeConfiguration.Builder(TreeType.CONFIGURATION);
        builder.setUniqueIndexes(true);
        builder.setMandatoryNodesValidation(true);

        DataTreeConfiguration dataTreeConfiguration = builder.build();
        assertEquals(TreeType.CONFIGURATION, dataTreeConfiguration.getTreeType());
        assertTrue(dataTreeConfiguration.isUniqueIndexEnabled());
        assertTrue(dataTreeConfiguration.isMandatoryNodesValidationEnabled());

        builder = new DataTreeConfiguration.Builder(TreeType.OPERATIONAL);
        builder.setUniqueIndexes(false);
        builder.setMandatoryNodesValidation(false);

        dataTreeConfiguration = builder.build();
        assertEquals(TreeType.OPERATIONAL, dataTreeConfiguration.getTreeType());
        assertFalse(dataTreeConfiguration.isUniqueIndexEnabled());
        assertFalse(dataTreeConfiguration.isMandatoryNodesValidationEnabled());

        dataTreeConfiguration = DataTreeConfiguration.getDefault(TreeType.CONFIGURATION);
        assertEquals(TreeType.CONFIGURATION, dataTreeConfiguration.getTreeType());
        assertFalse(dataTreeConfiguration.isUniqueIndexEnabled());
        assertTrue(dataTreeConfiguration.isMandatoryNodesValidationEnabled());

        dataTreeConfiguration = DataTreeConfiguration.getDefault(TreeType.OPERATIONAL);
        assertEquals(TreeType.OPERATIONAL, dataTreeConfiguration.getTreeType());
        assertFalse(dataTreeConfiguration.isUniqueIndexEnabled());
        assertFalse(dataTreeConfiguration.isMandatoryNodesValidationEnabled());
    }
}
