/*
 * Copyright (c) 2021 Vratko Polak and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.testing.HashedTestingElement;
import org.opendaylight.yangtools.yang.data.api.testing.ReversedListTester;
import org.opendaylight.yangtools.yang.data.api.testing.SemirandomListTester;
import org.opendaylight.yangtools.yang.data.api.testing.SequentialListTester;
import org.opendaylight.yangtools.yang.data.api.testing.StringedTestingElement;

public class SimpleIdListTest {

    @Test
    public void testSemirandom1() {
        SimpleIdList<String, HashedTestingElement> list = new SimpleIdList();
        SemirandomListTester.testScale1(list);
    }

    @Test
    public void testSequential1() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        SequentialListTester.testScale1(list);
    }

    @Test
    public void testReversed1() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        ReversedListTester.testScale1(list);
    }

    @Test
    public void testSemirandom10() {
        SimpleIdList<String, HashedTestingElement> list = new SimpleIdList();
        SemirandomListTester.testScale10(list);
    }

    @Test
    public void testSequential10() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        SequentialListTester.testScale10(list);
    }

    @Test
    public void testReversed10() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        ReversedListTester.testScale10(list);
    }

    @Test
    public void testSemirandom100() {
        SimpleIdList<String, HashedTestingElement> list = new SimpleIdList();
        SemirandomListTester.testScale100(list);
    }

    @Test
    public void testSequential100() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        SequentialListTester.testScale100(list);
    }

    @Test
    public void testReversed100() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        ReversedListTester.testScale100(list);
    }

    @Test
    public void testSemirandom1000() {
        SimpleIdList<String, HashedTestingElement> list = new SimpleIdList();
        SemirandomListTester.testScale1000(list);
    }

    @Test
    public void testSequential1000() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        SequentialListTester.testScale1000(list);
    }

    @Test
    public void testReversed1000() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        ReversedListTester.testScale1000(list);
    }

    @Test
    public void testSemirandom100000() {
        SimpleIdList<String, HashedTestingElement> list = new SimpleIdList();
        SemirandomListTester.testScale100000(list);
    }

    @Test
    public void testSequential100000() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        SequentialListTester.testScale100000(list);
    }

    @Test
    public void testReversed100000() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        ReversedListTester.testScale100000(list);
    }

    @Test
    public void testSemirandom10000() {
        SimpleIdList<String, HashedTestingElement> list = new SimpleIdList();
        SemirandomListTester.testScale10000(list);
    }

    @Test
    public void testSequential10000() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        SequentialListTester.testScale10000(list);
    }

    @Test
    public void testReversed10000() {
        SimpleIdList<String, StringedTestingElement> list = new SimpleIdList();
        ReversedListTester.testScale10000(list);
    }

}
