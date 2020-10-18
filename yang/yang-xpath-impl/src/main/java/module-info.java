/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.opendaylight.yangtools.yang.xpath.impl.AntlrXPathParserFactory;

module org.opendaylight.yangtools.yang.xpath.impl {
    provides YangXPathParserFactory with AntlrXPathParserFactory;

    requires org.opendaylight.yangtools.yang.xpath.antlr;
    requires org.opendaylight.yangtools.yang.xpath.api;
    requires org.slf4j;

    // Annotations
    requires static javax.inject;
    requires static org.osgi.service.component.annotations;
}
