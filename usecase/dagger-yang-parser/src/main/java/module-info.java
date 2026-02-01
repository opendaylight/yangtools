/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Default YANG Parser Dagger components.
 */
module org.opendaylight.yangtools.dagger.yang.parser {
    exports org.opendaylight.yangtools.dagger.yang.parser;

    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires org.opendaylight.yangtools.odlext.parser.support;
    requires org.opendaylight.yangtools.openconfig.parser.support;
    requires org.opendaylight.yangtools.rfc6241.parser.support;
    requires org.opendaylight.yangtools.rfc6536.parser.support;
    requires org.opendaylight.yangtools.rfc6643.parser.support;
    requires org.opendaylight.yangtools.rfc7952.parser.support;
    requires org.opendaylight.yangtools.rfc8040.parser.support;
    requires org.opendaylight.yangtools.rfc8528.parser.support;
    requires org.opendaylight.yangtools.rfc8639.parser.support;
    requires org.opendaylight.yangtools.rfc8819.parser.support;
    requires org.opendaylight.yangtools.yang.parser.rfc7950;
    requires org.opendaylight.yangtools.yang.source.ir;
    requires org.opendaylight.yangtools.yang.xpath.impl;
    requires org.opendaylight.yangtools.yin.source.dom;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static transitive dagger;
    requires static transitive jakarta.inject;
    requires static transitive java.compiler;
    requires static transitive javax.inject;
}
