/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;

class DataTreeConfigurationTest {
    @Test
    void testDataTreeConfiguration() {
        var dataTreeConfiguration = DataTreeConfiguration.builder(TreeType.CONFIGURATION)
            .setUniqueIndexes(true)
            .setMandatoryNodesValidation(true)
            .build();
        assertEquals(TreeType.CONFIGURATION, dataTreeConfiguration.getTreeType());
        assertTrue(dataTreeConfiguration.isUniqueIndexEnabled());
        assertTrue(dataTreeConfiguration.isMandatoryNodesValidationEnabled());

        dataTreeConfiguration = DataTreeConfiguration.builder(TreeType.OPERATIONAL)
            .setUniqueIndexes(false)
            .setMandatoryNodesValidation(false).build();
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
        assertTrue(dataTreeConfiguration.isMandatoryNodesValidationEnabled());
    }
}
