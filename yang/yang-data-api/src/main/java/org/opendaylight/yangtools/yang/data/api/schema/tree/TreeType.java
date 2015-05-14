/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

/**
 * Data Tree type. Data Tree behaviour depends on data tree type.
 *
 *
 */
public enum TreeType {

    /**
     * Configuration data tree.
     *
     * Configuration data tree may contains only data tree nodes,
     * which are modeled in YANG as data nodes with <code>config true</code>
     * statement (which is default behaviour in YANG 1.0).
     *
     *
     */
    CONFIGURATION,

    /**
     *
     * Operational data tree type.
     *
     * Operational data tree may contains any YANG-modeled data tree nodes.
     *
     */
    OPERATIONAL
}
