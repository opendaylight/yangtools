/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.impl;

import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;

/**
 * Public interface exposed from generator implementation.
 *
 * @deprecated This interface is superseded by an internal implementation.
 */
@Deprecated
public interface DataObjectSerializerGenerator {
    /**
     * Get a serializer for a particular type.
     *
     * @param type Type class
     * @return Serializer instance.
     */
    DataObjectSerializerImplementation getSerializer(Class<?> type);

    /**
     * Notify the generator that the runtime context has been updated.
     * @param runtime New runtime context
     */
    void onBindingRuntimeContextUpdated(BindingRuntimeContext runtime);
}
