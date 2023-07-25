/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.anyXmlBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.anydataBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.choiceBuilder;

import javax.xml.transform.dom.DOMSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1104Test {
    private static final QName MODULE = QName.create("yt1104", "yt1104");
    private static final NodeIdentifier FOO = new NodeIdentifier(QName.create(MODULE, "foo"));
    private static final NodeIdentifier BAR = new NodeIdentifier(QName.create(MODULE, "bar"));
    private static final NodeIdentifier BAZ = new NodeIdentifier(QName.create(MODULE, "baz"));

    private static EffectiveModelContext SCHEMA_CONTEXT;

    private DataTree dataTree;

    @BeforeAll
    static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module yt1104 {
              yang-version 1.1;
              namespace yt1104;
              prefix yt1104;

              choice foo {
                anydata bar;
                anyxml baz;
              }
            }""");
    }

    @AfterAll
    static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @BeforeEach
    void init() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
    }

    @Test
    void testAnydata() throws DataValidationFailedException {
        writeChoice(anydataBuilder(String.class).withNodeIdentifier(BAR).withValue("anydata").build());
    }

    @Test
    void testAnyxml() throws DataValidationFailedException {
        writeChoice(anyXmlBuilder().withNodeIdentifier(BAZ).withValue(new DOMSource()).build());
    }

    private void writeChoice(final DataContainerChild child) throws DataValidationFailedException {
        final var mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.of(FOO), choiceBuilder().withNodeIdentifier(FOO).withChild(child).build());
        mod.ready();
        dataTree.validate(mod);
        dataTree.commit(dataTree.prepare(mod));
    }
}
