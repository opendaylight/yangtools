/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Simple interface for reuse with BindingObjects which expose a single value in some representation. This interface
 * is not meant to be directly implemented.
 *
 * @param <T> value type
 */
@Beta
public interface ValueAware<T> {
    /**
     * Return the value associated with this object.
     *
     * @return This object's value.
     */
    @NonNull T getValue();
}
