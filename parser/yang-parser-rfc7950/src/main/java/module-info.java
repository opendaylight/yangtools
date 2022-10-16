/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

module org.opendaylight.yangtools.yang.parser.rfc7950 {
    // FIXME: audit these, potentially lowering them to their sole user if reasonable
    exports org.opendaylight.yangtools.yang.parser.rfc7950.reactor;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.repo;

    uses YangXPathParserFactory;

    requires transitive java.xml;
    requires transitive com.google.common;
    requires transitive org.opendaylight.yangtools.concepts;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.yang.model.spi;
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
    requires static com.github.spotbugs.annotations;
    requires static org.checkerframework.checker.qual;
}
