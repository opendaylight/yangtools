/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.common;

import java.text.SimpleDateFormat;

public class SimpleDateFormatUtil {

    private static final ThreadLocal<SimpleDateFormat> REVISION_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        };

        public void set(SimpleDateFormat value) {
            throw new UnsupportedOperationException();
        };

    };

    public static SimpleDateFormat getRevisionFormat() {
        return REVISION_FORMAT.get();
    }
}
