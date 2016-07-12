/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class TestModel {

    public static final QName TEST_QNAME = QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test", "2014-03-13", "test");
    public static final QName OUTER_LIST_QNAME = QName.create(TEST_QNAME, "outer-list");
    public static final QName INNER_LIST_QNAME = QName.create(TEST_QNAME, "inner-list");
    public static final QName OUTER_CHOICE_QNAME = QName.create(TEST_QNAME, "outer-choice");
    public static final QName INNER_CONTAINER_QNAME = QName.create(TEST_QNAME, "inner-container");
    public static final QName ID_QNAME = QName.create(TEST_QNAME, "id");
    public static final QName NAME_QNAME = QName.create(TEST_QNAME, "name");
    public static final QName VALUE_QNAME = QName.create(TEST_QNAME, "value");
    private static final String DATASTORE_TEST_YANG = "/odl-datastore-test.yang";

    public static final QName NON_PRESENCE_QNAME = QName.create(TEST_QNAME, "non-presence");
    public static final QName DEEP_CHOICE_QNAME = QName.create(TEST_QNAME, "deep-choice");
    public static final QName A_LIST_QNAME = QName.create(TEST_QNAME, "a-list");
    public static final QName A_NAME_QNAME = QName.create(TEST_QNAME, "a-name");

    public static final YangInstanceIdentifier TEST_PATH = YangInstanceIdentifier.of(TEST_QNAME);
    public static final YangInstanceIdentifier OUTER_LIST_PATH = YangInstanceIdentifier.builder(TEST_PATH)
            .node(OUTER_LIST_QNAME).build();
    public static final YangInstanceIdentifier INNER_CONTAINER_PATH = YangInstanceIdentifier.builder(TEST_PATH).node(INNER_CONTAINER_QNAME).build();
    public static final YangInstanceIdentifier VALUE_PATH = YangInstanceIdentifier.of(VALUE_QNAME);
    public static final YangInstanceIdentifier INNER_VALUE_PATH = YangInstanceIdentifier.builder(INNER_CONTAINER_PATH).node(VALUE_QNAME).build();
    public static final QName TWO_QNAME = QName.create(TEST_QNAME, "two");
    public static final QName THREE_QNAME = QName.create(TEST_QNAME, "three");

    public static InputStream getDatastoreTestInputStream() {
        return TestModel.class.getResourceAsStream(DATASTORE_TEST_YANG);
    }

    public static SchemaContext createTestContext() throws ReactorException {
        return YangParserTestUtils.parseYangStreams(Arrays.asList(getDatastoreTestInputStream()));
    }

    public static SchemaContext createTestContext(final String resourcePath) throws ReactorException {
        final InputStream yangStream = TestModel.class.getResourceAsStream(resourcePath);
        return YangParserTestUtils.parseYangStreams(Collections.singletonList(yangStream));
    }
}
