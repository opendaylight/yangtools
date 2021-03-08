/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.parser.rfc7950 {
    // FIXME: audit these, potentially lowering them to their sole user if reasonable
    exports org.opendaylight.yangtools.yang.parser.rfc7950.antlr;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.ir;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.namespace;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.reactor;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.repo;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

    uses org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires transitive org.opendaylight.yangtools.yang.parser.reactor;
    requires transitive org.opendaylight.yangtools.yang.parser.spi;
    requires transitive org.opendaylight.yangtools.yang.repo.spi;

    requires com.google.common;
    requires org.antlr.antlr4.runtime;
    requires org.opendaylight.yangtools.concepts;
    requires org.opendaylight.yangtools.openconfig.model.api;
    requires org.opendaylight.yangtools.yang.common;
    requires org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.yang.model.spi;
    requires org.opendaylight.yangtools.yang.model.ri;
    requires org.opendaylight.yangtools.yang.parser.antlr;
    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    // Annotations
    requires static com.github.spotbugs.annotations;
    requires static org.checkerframework.checker.qual;
    requires static org.eclipse.jdt.annotation;

    // FIXME: hide these
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;
    // FIXME: this needs to be renamed to match parser-support naming convention
    exports org.opendaylight.yangtools.yang.parser.openconfig.stmt;
}
