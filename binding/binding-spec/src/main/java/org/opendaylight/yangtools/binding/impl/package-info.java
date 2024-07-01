/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Internal implementation details. This package has a number of functions:
 * <ul>
 *   <li>the class loader part of {@link org.opendaylight.yangtools.binding.contract.ContractTrust}, being inaccessible
 *       to outside code unless a JVM override is applied</li>
 *   <li>use with {@link org.opendaylight.yangtools.yang.binding.InstanceIdentifier}, so the dance we are doing with
 *       class hierarchy is not (entirely) visible</li>
 * </ul>
 */
package org.opendaylight.yangtools.binding.impl;