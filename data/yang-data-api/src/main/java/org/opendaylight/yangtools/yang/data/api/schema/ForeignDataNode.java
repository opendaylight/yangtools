/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import java.util.Map.Entry;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A NormalizedNode holding a value in some foreign object model. The object model is identified by a single class,
 * which must be the superclass of (or interface implemented by) all objects used to anchor that object model into
 * NormalizedNode model.
 *
 * <p>
 * This interface should not be implemented directly, but rather further specialized, like {@link AnyxmlNode}.
 *
 * @param <V> Value type, uniquely identifying the object model used for values
 */
@Beta
public sealed interface ForeignDataNode<V> extends DataContainerChild permits AnydataNode, AnyxmlNode {
    /**
     * {@inheritDoc}
     *
     * <p>
     * The body follows the object model exposed through {@link #bodyObjectModel()}
     */
    @Override
    V body();

    @Override
    Entry<@NonNull NodeIdentifier, ? extends @NonNull ForeignDataBody<V>> toEntry();

    /**
     * Return the object model class, which identifies it. For example {@link DOMSourceAnyxmlNode} uses
     * {@link DOMSource} as its value object model.
     *
     * @return Object model class
     */
    @NonNull Class<V> bodyObjectModel();
}
