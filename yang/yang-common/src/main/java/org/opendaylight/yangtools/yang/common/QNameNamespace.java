/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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
public enum QNameNamespace {
    NS_DEFAULT,
    NS_MODULE,
    NS_EXTENSION,
    NS_FEATURE_ID,
    NS_IDENTITY,
    NS_TYPEDEF,
    NS_GROUPING,
    NS_DATA,
}
