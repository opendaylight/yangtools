/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

/**
 * Class that calculates and tracks time duration statistics.
 *
 * @author Thomas Pantelis
 * @author Robert Varga
 *
 * @deprecated Use {@link DurationStatisticsTracker} instead.
 */
@Deprecated
public class DurationStatsTracker extends ConcurrentDurationStatisticsTracker {
    /**
     * @deprecated Use {@link DurationStatisticsTracker#createConcurrent() instead.
     */
    @Deprecated
    public DurationStatsTracker() {

    }

    // Remove once the no-argument constructor is removed
    DurationStatsTracker(final Void dummy) {

    }
}
