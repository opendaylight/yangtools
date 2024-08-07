/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * Data Container - object contains structured data. Marker interface which must be implemented by all interfaces
 * generated for YANG:
 * <ul>
 *   <li>{@code grouping} based on {@link Grouping}</li>
 *   <li>{@code input} based on {@link RpcInput}</li>
 *   <li>{@code output} based on {@link RpcOutput}</li>
 *   <li>{@code notification} based on {@link BaseNotification}, either {@link Notification} or
 *       {@link InstanceNotification}, potentially with a shared {@link NotificationBody} definition</li>
 *   <li>{@code container}, {@code list} and {@code case} based on {@link DataObject}</li>
 *   <li>a {@code module}'s data schema nodes based on {@link DataRoot}
 *   <li>{@code rc:yang-data} based on {@link YangData}
 * </ul>
 */
public sealed interface DataContainer extends BindingContract<DataContainer>
    permits BaseNotification, ChoiceIn, DataObject, DataRoot, Grouping, YangData {
    // Nothing else
}
