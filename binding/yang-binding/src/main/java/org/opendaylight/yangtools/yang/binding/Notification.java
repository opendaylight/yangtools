/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Marker interface for YANG-defined global notifications. This interface should never be implemented directly. A
 * concrete Notification and its implementations may choose to also extend/implement the {@link EventInstantAware}
 * interface. In case they do, {@link EventInstantAware#eventInstant()} returns the time when this notification was
 * generated.
 */
// FIXME: 6.0.0: narrow implementedInterface()
public interface Notification extends BaseNotification {

}
