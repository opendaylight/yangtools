/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

/**
 * Contains the method for getting detail data about unsigned integer.
 *
 * <p>
 * Note this is an intermediate interface, concretized by sub-interfaces.
 *
 * @param <N> native representation type
 * @param <T> concrete type definition
 */
public interface UnsignedIntegerTypeDefinition<N extends Number & Comparable<N>,
        T extends UnsignedIntegerTypeDefinition<N, T>> extends RangeRestrictedTypeDefinition<T, N> {

}
