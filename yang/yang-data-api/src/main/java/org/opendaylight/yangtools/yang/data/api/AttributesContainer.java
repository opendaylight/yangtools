/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api;

import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;

/**
 *
 * Container of attributes, which may be attached to nodes.
 *
 */
public interface AttributesContainer {

    /**
     * Returns immutable map of QName and value of the attribute.
     *
     * @return immutable map of attribute names and values.
     */
    Map<QName, String> getAttributes();

    /**
     * Returns attribute value by supplied QName
     *
     *
     * @param name Attribute name
     * @return Value of attribute if present, null otherwise.
     */
    Object getAttributeValue(QName name);

}
