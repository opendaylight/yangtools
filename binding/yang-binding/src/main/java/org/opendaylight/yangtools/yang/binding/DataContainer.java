/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Data Container - object contains structured data. Marker interface which must be implemented by all interfaces
 * generated for YANG:
 * <ul>
 * <li>Rpc Input
 * <li>Output
 * <li>Notification
 * <li>Container
 * <li>List
 * <li>Case
 * </ul>
 */
public interface DataContainer {
    /**
     * Return the interface implemented by this object. This method differs from {@link Object#getClass()} in that it
     * returns the interface contract, not a concrete implementation class.
     *
     * @return Implemented contract
     * @deprecated Use {@link #implementedInterface()} instead.
     */
    // FIXME: 4.0.0: MDSAL-395: remove this method
    @Deprecated
    Class<? extends DataContainer> getImplementedInterface();

    @NonNull Class<? extends DataContainer> implementedInterface();
}
