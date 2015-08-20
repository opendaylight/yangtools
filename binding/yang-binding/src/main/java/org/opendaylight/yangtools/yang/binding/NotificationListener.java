/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.EventListener;

/**
 * Marker interface for generated notification listener interfaces. This interface
 * exists solely as support for generated code. Users should never implement this
 * interface directly, but rather implement one of the sub-interfaces generated
 * from a YANG model.
 *
 * <p>
 * The subclasses of this interface have callbacks for events, which are derived
 * from {@link Notification} class in form void
 * on{NotificationType}(NotificationType notification).
 *
 * <p>
 * E.g. if we have notification SessionUp the callback will have signature:
 * <code>void  onSessionUp(SessionUp notification)</code>
 */
public interface NotificationListener extends EventListener {

}
