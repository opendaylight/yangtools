/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.tree;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Benchmark model constants.
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 */
final class BenchmarkModel {
    private static final QName TEST_QNAME = QName.create(
        "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test", "2014-03-13", "test").intern();
    static final NodeIdentifier TEST = NodeIdentifier.create(TEST_QNAME);
    static final YangInstanceIdentifier TEST_PATH = YangInstanceIdentifier.create(TEST);

    static final QName OUTER_LIST_QNAME = QName.create(TEST_QNAME, "outer-list").intern();
    static final NodeIdentifier OUTER_LIST = NodeIdentifier.create(OUTER_LIST_QNAME);
    static final YangInstanceIdentifier OUTER_LIST_PATH = YangInstanceIdentifier.create(TEST, OUTER_LIST);

    static final QName INNER_LIST_QNAME = QName.create(TEST_QNAME, "inner-list").intern();
    static final NodeIdentifier INNER_LIST = NodeIdentifier.create(INNER_LIST_QNAME);

    static final QName OUTER_CHOICE_QNAME = QName.create(TEST_QNAME, "outer-choice").intern();
    static final QName ID_QNAME = QName.create(TEST_QNAME, "id").intern();
    static final QName NAME_QNAME = QName.create(TEST_QNAME, "name").intern();
    static final QName VALUE_QNAME = QName.create(TEST_QNAME, "value").intern();

    private BenchmarkModel() {

    }

    static EffectiveModelContext createTestContext() {
        return YangParserTestUtils.parseYangResource("/odl-datastore-test.yang");
    }
}
