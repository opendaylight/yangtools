/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

module org.opendaylight.yangtools.yang.parser.rfc7950 {
    exports org.opendaylight.yangtools.yang.parser.dagger;

    // FIXME: audit these, potentially lowering them to their sole user if reasonable
    exports org.opendaylight.yangtools.yang.parser.rfc7950.reactor;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.repo;

    uses ParserExtension;
    uses YangXPathParserFactory;
    provides YangLibResolver with org.opendaylight.yangtools.yang.parser.ri.DefaultYangLibResolver;
    provides YangParserFactory with org.opendaylight.yangtools.yang.parser.ri.DefaultYangParserFactory;

    requires transitive java.xml;
    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.model.spi;
    requires transitive org.opendaylight.yangtools.yang.ir;
    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires transitive org.opendaylight.yangtools.yang.parser.reactor;
    requires transitive org.opendaylight.yangtools.yang.parser.spi;
    requires transitive org.opendaylight.yangtools.yang.repo.api;
    requires transitive org.opendaylight.yangtools.yang.repo.spi;
    requires transitive org.opendaylight.yangtools.yang.xpath.api;

    requires org.antlr.antlr4.runtime;
    requires org.opendaylight.yangtools.yang.model.ri;
    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static dagger;
    requires static java.compiler;
    requires static javax.inject;
    requires static jakarta.inject;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.annotation.bundle;
    requires static org.osgi.service.component.annotations;
}
