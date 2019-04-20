/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.impl;

/**
 * Package-private base class for sharing the loading capability.
 *
 * @deprecated This class is superseded by an internal implementation.
 */
@Deprecated
abstract class AbstractGenerator {
    /**
     * Ensure that the serializer class for specified class is loaded and return its name.
     *
     * @param cls Data object class
     * @return Serializer class name
     */
    protected abstract String loadSerializerFor(Class<?> cls);
}