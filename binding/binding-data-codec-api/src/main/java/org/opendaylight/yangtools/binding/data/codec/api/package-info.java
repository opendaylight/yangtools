/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * API elements of a codec capable translating YANG-modeled data between its
 * {@link org.opendaylight.yangtools.binding.BindingObject}s and
 * {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedData} representations. The central piece is
 * {@link BindingDataCodec}, which is a service typically provided by dependency injection.
 * {@link java.util.ServiceLoader} is provided for simple environments.
 */
package org.opendaylight.yangtools.binding.data.codec.api;