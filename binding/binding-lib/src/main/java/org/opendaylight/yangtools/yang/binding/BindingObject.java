/*
 * Copyright (c) 2019 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Base interface for all interfaces and classes which can be encountered in the context of a particular data exchange.
 * These map to YANG data definition constructs:
 * <ul>
 *   <li>{@code identity} as referenced via a {@code type identityref} statement, represented by
 *       {@link BaseIdentity}</li>
 *   <li>{@code anydata} and {@code anyxml}, represented by {@link OpaqueObject}</li>
 *   <li>{@code typedef} and {@code type}, represented by {@link TypeObject}</li>
 *   <li>{@code md:annotation}, represented by {@link Annotation}</li>
 *   <li>{@code rc:yang-data}, represented by {@link YangData}</li>
 *   <li>all others, represented by {@link DataObject} and its further specializations</li>
 * </ul>
 *
 * @author Jie Han
 */
public sealed interface BindingObject permits Annotation, BaseIdentity, DataObject, OpaqueObject, TypeObject, YangData {
}
