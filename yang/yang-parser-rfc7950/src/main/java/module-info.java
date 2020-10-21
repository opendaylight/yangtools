/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.yangtools.yang.parser.rfc7950 {
    // FIXME: audit these
    exports org.opendaylight.yangtools.yang.parser.rfc7950.antlr;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.ir;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.namespace;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.reactor;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.repo;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires org.opendaylight.yangtools.openconfig.model.api;
    requires org.opendaylight.yangtools.yang.parser.antlr;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.opendaylight.yangtools.util;

    uses org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

    // FIXME: this is not a module yet
    requires transitive org.opendaylight.yangtools.yang.parser.api;
    requires org.opendaylight.yangtools.yang.parser.spi;
    requires org.opendaylight.yangtools.yang.parser.reactor;

    // FIXME: hide these
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.action;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anydata;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.argument;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.base;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.belongs_to;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.case_;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.config;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.contact;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.default_;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.description;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviate;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviation;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_app_tag;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_message;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.feature;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.fraction_digits;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.include;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.length;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.mandatory;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.max_elements;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.min_elements;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.modifier;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.must;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.namespace;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ordered_by;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.organization;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.position;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.prefix;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.presence;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.range;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.reference;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.require_instance;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision_date;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.rpc;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.status;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.typedef;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.unique;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.units;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.value;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.when;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yang_version;
    exports org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yin_element;
    // FIXME: this needs to be renamed to match parser-support naming convention
    exports org.opendaylight.yangtools.yang.parser.openconfig.stmt;
}
