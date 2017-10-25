/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.List;

/**
 * Interface describing YANG 'deviation' statement.
 *
 * <p>
 * The 'deviation' statement defines a hierarchy of a module that the device does not implement faithfully. Deviations
 * define the way a device deviate from a standard.
 */
public interface Deviation extends DocumentedNode {

    /**
     * Returns target schema path.
     *
     * @return SchemaPath that identifies the node in the schema tree where a deviation from the module occurs.
     */
    SchemaPath getTargetPath();

    /**
     * Returns deviate children.
     *
     * @return List of all deviate statements defined in this deviation.
     */
    List<DeviateDefinition> getDeviates();
}
