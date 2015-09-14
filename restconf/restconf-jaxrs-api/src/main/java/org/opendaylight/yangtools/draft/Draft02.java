/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.draft;

public final class Draft02 {

    private Draft02() {
        throw new UnsupportedOperationException("Utility class should not be instantiated!");
    }

    public static final class MediaTypes {
        public static final String API = "application/yang.api";
        public static final String DATASTORE = "application/yang.datastore";
        public static final String DATA = "application/yang.data";
        public static final String OPERATION = "application/yang.operation";
        public static final String PATCH = "application/yang.patch";
        public static final String PATCH_STATUS = "application/yang.patch-status";
        public static final String STREAM = "application/yang.stream";

        private MediaTypes() {
            throw new UnsupportedOperationException("Utility class should not be instantiated!");
        }
    }

    public static class Paths {

    }
}