/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

/**
 * API-private constants. We do not leak these objects, but allow access to them via interface-static methods.
 *
 * @author Robert Varga
 */
final class Constants {
    static final YangLocationPath ROOT_LOCATION = new YangLocationPathBuilder().isAbsolute(true).build();
    static final YangLocationPath SAME_LOCATION = new YangLocationPathBuilder().isAbsolute(false).build();

    private Constants() {

    }
}
