/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.binding.DataContainer;

/**
 * SPI-level contract for registry of {@link DataContainerSerializer}. The contract is kept between implementation of
 * {@link DataContainerSerializer}, Registry provides lookup for serializers to support recursive serialization of
 * nested {@link DataContainer}s.
 */
// FIXME: this interface should not be necessary
public interface DataContainerSerializerRegistry {

    DataContainerSerializer getSerializer(Class<? extends DataContainer> binding);

}
