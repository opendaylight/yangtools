/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Utility interfaces for dealing with YANG/YIN files.
 */
module org.opendaylight.yangtools.yang.file.api {
    exports org.opendaylight.yangtools.yang.file.api;

    requires org.opendaylight.yangtools.yang.common;
    requires org.slf4j;

    // Annotations
    requires static org.eclipse.jdt.annotation;
}
