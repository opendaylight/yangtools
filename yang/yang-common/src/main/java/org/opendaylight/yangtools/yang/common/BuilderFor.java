/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

/**
 * Builder for Type T
 *
 * Marker interface marking a Builder as building a Type T
 *
 * Any Builder generated from yang for a type must implement this interface.
 *
 * @param <T> Type built by builder
 */
public interface BuilderFor<T> {
    T build();
}
