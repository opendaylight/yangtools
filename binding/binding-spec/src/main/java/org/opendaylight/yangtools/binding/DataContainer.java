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
 *       {@link InstanceNotification}</li>
 *   <li>{@code container}, {@code list} and {@code case} based on {@link DataObject}</li>
 *   <li>{@code anydata} and {@code anyxml} based on {@link OpaqueObject}
 *   <li>a {@code module}'s data schema nodes based on {@link DataRoot}
 *   <li>{@code rc:yang-data} based on {@link YangData}
 * </ul>
 */
public sealed interface DataContainer extends BindingContract<DataContainer>
    permits DataContainer.Addressable, BaseNotification, ChoiceIn, DataRoot, Grouping, OpaqueObject, YangData {
    /**
     * A {@link DataContainer} which models data which can be instantiated in a data tree. This implies
     * addressability via {@link DataObjectReference}.
     */
    sealed interface Addressable extends BindingObject, DataContainer {
        /**
         * A {@link DataContainer} which models data which can be instantiated once in a data tree. This implies
         * addressability via {@link DataObjectReference} and {@link ExactDataObjectStep}s only.
         */
        sealed interface Single extends DataContainer.Addressable permits Augmentation, DataObject {
            // Nothing else
        }

        /**
         * A {@link DataContainer} which models data which can be instantiated multiple times in a data tree. This
         * implies addressability via {@link DataObjectReference} with {@link ExactDataObjectStep} as well as
         * {@link InexactDataObjectStep}s.
         */
        sealed interface Multiple extends Addressable permits EntryObject {
            // Nothing else
        }
    }
}
