/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Common YANG concepts and constants expressed in terms of Java. This notably includes the concepts of
 * <ul>
 *   <li>a {@link YangVersion}</li>
 *   <li>a {@link Revision}</li>
 *   <li>a {@link QNameModule}</li>
 *   <li>a {@link QName}</li>
 *   <li>a Java-native representation of a YANG string, {@link DerivedString}</li>
 *   <li>a Java-native representations of numeric YANG types, like {@link Uint64}, {@link Decimal64} and
 *       {@link Empty}</li>
 *   <li>markers for a Java-native representations of {@code anydata} and {@anyxml} values, {@link OpaqueValue}</li>
 * </ul>
 */
@Export
package org.opendaylight.yangtools.yang.common;

import org.osgi.annotation.bundle.Export;
