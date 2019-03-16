/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.xpath;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * The notion of a W3C documented bound to a specific object model. This binding is expressed in terms of the document
 * exposing a root node from the object model.
 *
 * <p>
 * This interface is not meant to be used directly. Refer to its specializations like {@link NormalizedNodeDocument}
 * and {@link DataTreeCandidateDocument}.
 *
 * @param <T> Object model node type
 * @author Robert Varga
 */
@Beta
public interface XPathDocument<T> {
    /**
     * Return the root node of this document.
     *
     * @return This document's root node.
     */
    @NonNull T getRootNode();
}
