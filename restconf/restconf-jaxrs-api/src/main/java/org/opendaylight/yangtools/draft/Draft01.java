/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.draft;

public class Draft01 {

    private Draft01() {
        throw new UnsupportedOperationException("Utility class should not be instantiated!");
    }

    public static class MediaTypes {
        public static final java.lang.String API = "application/vnd.yang.api";
        public static final java.lang.String DATASTORE = "application/vnd.yang.datastore";
        public static final java.lang.String DATA = "application/vnd.yang.data";
        public static final java.lang.String EVENT = "application/vnd.yang.event";
        public static final java.lang.String OPERATION = "application/vnd.yang.operation";
        public static final java.lang.String PATCH = "application/vnd.yang.patch";

        private MediaTypes() {
            throw new UnsupportedOperationException("Utility class should not be instantiated!");
        }
    }
}