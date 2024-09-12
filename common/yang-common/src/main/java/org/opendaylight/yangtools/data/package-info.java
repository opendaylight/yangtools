/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * This package defines the object model covering mapping of YANG data tree into Java world. It is currently limited to
 * modeling values held by {@code leaf} and {@code leaf-list} statements using the {@link Value} contract.
 */
@org.osgi.annotation.bundle.Export
package org.opendaylight.yangtools.data;