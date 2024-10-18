/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Utilities for constructing and working with
 * {@link org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext}.
 */
module org.opendaylight.yangtools.binding.runtime.spi {
    exports org.opendaylight.yangtools.binding.runtime.spi;

    requires transitive org.opendaylight.yangtools.binding.runtime.api;
    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires org.opendaylight.yangtools.binding.model;
    requires org.opendaylight.yangtools.binding.reflect;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.ir;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.spi;
    requires org.opendaylight.yangtools.yang.parser.impl;
    requires org.slf4j;

    uses org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
    uses org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeGenerator;
    uses org.opendaylight.yangtools.yang.parser.api.YangParserFactory;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static org.checkerframework.checker.qual;
    requires static org.osgi.annotation.bundle;
}
