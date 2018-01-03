/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

/**
 * Identifier namespace definitions according to section 6.2.1 in RFC7950.
 * It maintains the uniqueness of the schema path by avoiding conflicts
 * of local names of QNames.
 */
public enum IDNamespace {
    NS_DEFAULT("ns_default"),
    NS_MODULE("ns_module"),
    NS_EXTENSION("ns_extension"),
    NS_FEATURE_ID("ns_feature_id"),
    NS_IDENTITY("ns_identity"),
    NS_TYPEDEF("ns_typedef"),
    NS_GROUPING("ns_grouping"),
    NS_DATA("ns_data");

    private String value;

    IDNamespace(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
