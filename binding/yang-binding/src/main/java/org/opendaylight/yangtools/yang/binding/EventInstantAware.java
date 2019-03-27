/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import java.time.Instant;
import org.eclipse.jdt.annotation.NonNull;

/**
 * This interface is mixed in into implementations of other constructs, such as {@link Notification} to add the time
 * when the event occurred.
 */
@Beta
public interface EventInstantAware {
    /**
     * Get the time of the event occurrence.
     *
     * @return the event time
     */
    @NonNull Instant eventInstant();
}
