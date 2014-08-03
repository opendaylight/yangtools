/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import java.util.List;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;


/**
 * @author michal.rehak
 *
 * @deprecated Deprecated in favor of {@link NormalizedNodeContainer} classes.
 */
@Deprecated
public interface MutableCompositeNode extends MutableNode<List<Node<?>>>, CompositeNode {

    /**
     * update internal map
     */
    @Deprecated
    void init();

    /**
     * @return original node, if available
     */
    CompositeNode getOriginal();
}
