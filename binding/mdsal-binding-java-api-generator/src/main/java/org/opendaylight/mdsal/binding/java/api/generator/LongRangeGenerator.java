/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

final class LongRangeGenerator extends AbstractPrimitiveRangeGenerator<Long> {

    protected LongRangeGenerator() {
        super(Long.class, long.class.getName(), Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Override
    protected String format(final Long value) {
        return value.toString() + 'L';
    }

    @Override
    @Deprecated
    protected Long convert(final Number value) {
        return value.longValue();
    }
}
