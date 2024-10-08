/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import javax.xml.transform.dom.DOMSource;

/**
 * An AnyxmlNode with data in {@link DOMSource} format.
 */
public interface DOMSourceAnyxmlNode extends AnyxmlNode<DOMSource> {
    @Override
    default Class<DOMSource> bodyObjectModel() {
        return DOMSource.class;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returned value is a DOMSource representation. Returned source contains top level element that duplicates the
     * anyxml node.
     */
    @Override
    DOMSource body();
}
