/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.util;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;

public interface NodeBuilder<P extends Node<?>, T extends NodeBuilder<P, T>> extends Builder<P> {

    T setQName(QName name);

    QName getQName();

    T setAttribute(QName attrName, String attrValue);

    T setAttribute(String attrName, String attrValue);

    /*
     * @deprecated use #build()
     */
    @Deprecated
    P toInstance();

}
