/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.anyXmlBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.anydataBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.choiceBuilder;

import javax.xml.transform.dom.DOMSource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public class YT1104Test {
    private static final QName MODULE = QName.create("yt1104", "yt1104");
    private static final NodeIdentifier FOO = new NodeIdentifier(QName.create(MODULE, "foo"));
    private static final NodeIdentifier BAR = new NodeIdentifier(QName.create(MODULE, "bar"));
    private static final NodeIdentifier BAZ = new NodeIdentifier(QName.create(MODULE, "baz"));

    private static EffectiveModelContext SCHEMA_CONTEXT;

    private DataTree dataTree;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext("/yt1104.yang");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @Before
    public void init() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
    }

    @Test
    public void testAnydata() throws DataValidationFailedException {
        writeChoice(anydataBuilder(String.class).withNodeIdentifier(BAR).withValue("anydata").build());
    }

    @Test
    public void testAnyxml() throws DataValidationFailedException {
        writeChoice(anyXmlBuilder().withNodeIdentifier(BAZ).withValue(new DOMSource()).build());
    }

    private void writeChoice(final DataContainerChild child) throws DataValidationFailedException {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.create(FOO), choiceBuilder().withNodeIdentifier(FOO).withChild(child).build());
        mod.ready();
        dataTree.validate(mod);
        dataTree.commit(dataTree.prepare(mod));
    }
}
