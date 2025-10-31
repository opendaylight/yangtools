/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;

module org.opendaylight.yangtools.yang.parser.impl {
    // FIXME: do not export this package
    exports org.opendaylight.yangtools.yang.parser.impl;
    exports org.opendaylight.yangtools.yang.parser.repo;

    provides SchemaRepository with org.opendaylight.yangtools.yang.parser.repo.SharedSchemaRepository;
    provides YangLibResolver with org.opendaylight.yangtools.yang.parser.impl.DefaultYangLibResolver;
    provides YangParserFactory with org.opendaylight.yangtools.yang.parser.impl.DefaultYangParserFactory;

    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires transitive org.opendaylight.yangtools.yang.parser.rfc7950;
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
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.spi;
    requires org.slf4j;

    // Annotations
    requires static transitive javax.inject;
    requires static transitive org.eclipse.jdt.annotation;
    requires static com.github.spotbugs.annotations;
    requires static org.checkerframework.checker.qual;
    requires static org.gaul.modernizer_maven_annotations;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.annotation.bundle;
    requires static org.osgi.service.component.annotations;
}
