/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import com.fasterxml.jackson.databind.Module;
import org.opendaylight.yangtools.yang.common.jackson.YangModule;

/**
 * Datatype support for of YANG language constructs to Java.
 *
 * @provides Module for core Jackson
 */
module org.opendaylight.yangtools.yang.common.jackson {
    exports org.opendaylight.yangtools.yang.common.jackson;

    provides Module with YangModule;

    requires transitive com.fasterxml.jackson.databind;
    requires com.google.common;
    requires org.opendaylight.yangtools.yang.common;

    // Annotations
    requires static metainf.services;
}
