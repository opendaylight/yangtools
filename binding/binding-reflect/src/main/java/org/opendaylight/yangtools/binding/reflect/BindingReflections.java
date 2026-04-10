/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.reflect;

import com.google.common.annotations.VisibleForTesting;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.contract.Naming;

@Deprecated(since = "14.0.16", forRemoval = true)
public final class BindingReflections {
    private BindingReflections() {
        // Hidden on purpose
    }

    /**
     * Check if supplied class is derived from YANG model.
     *
     * @param cls
     *            Class to check
     * @return true if class is derived from YANG model.
     */
    public static boolean isBindingClass(final Class<?> cls) {
        if (DataContainer.class.isAssignableFrom(cls) || Augmentation.class.isAssignableFrom(cls)) {
            return true;
        }
        return cls.getName().startsWith(Naming.PACKAGE_PREFIX);
    }

    /**
     * Checks if supplied class represents RPC Input / RPC Output.
     *
     * @param targetType
     *            Class to be checked
     * @return true if class represents RPC Input or RPC Output class.
     */
    @VisibleForTesting
    static boolean isRpcType(final Class<? extends DataObject> targetType) {
        return DataContainer.class.isAssignableFrom(targetType)
                && !ChildOf.class.isAssignableFrom(targetType)
                && !Notification.class.isAssignableFrom(targetType)
                && (targetType.getName().endsWith("Input") || targetType.getName().endsWith("Output"));
    }
}
