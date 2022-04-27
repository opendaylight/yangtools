/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.opendaylight.yangtools.yang.xpath.impl.AntlrXPathParserFactory;

/**
 * Reference implementation of YANG XPath parser.
 */
module org.opendaylight.yangtools.yang.xpath.impl {
    exports org.opendaylight.yangtools.yang.xpath.impl.di;

    provides YangXPathParserFactory with AntlrXPathParserFactory;

    requires transitive org.opendaylight.yangtools.yang.xpath.api;
    requires java.xml;
    requires org.antlr.antlr4.runtime;
    requires org.opendaylight.yangtools.yang.common;
    requires org.slf4j;

    // Annotations
    requires static transitive javax.inject;
    requires static com.github.spotbugs.annotations;
    requires static org.eclipse.jdt.annotation;
    requires static org.kohsuke.metainf_services;
    requires static org.osgi.service.component.annotations;
}
