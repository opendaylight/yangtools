/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;

/**
 * Generated Type Builder interface is helper interface for building and
 * defining the GeneratedType.
 *
 * @see GeneratedType
 */
public interface GeneratedTypeBuilder extends GeneratedTypeBuilderBase<GeneratedTypeBuilder> {
    /**
     * Returns the <code>new</code> <i>immutable</i> instance of Generated Type.
     *
     * @return the <code>new</code> <i>immutable</i> instance of Generated Type.
     */
    @NonNull GeneratedType build();
}
