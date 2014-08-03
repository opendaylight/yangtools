/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;


/**
 * @author michal.rehak
 * @param <T> node value type
 *
 * @deprecated Use {@link NormalizedNode} instead.
 */
@Deprecated
public interface MutableSimpleNode<T> extends MutableNode<T>, SimpleNode<T> {

    /**
     * @return original node, if available
     */
    SimpleNode<T> getOriginal();

}
