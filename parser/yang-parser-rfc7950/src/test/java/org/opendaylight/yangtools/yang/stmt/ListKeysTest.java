/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class ListKeysTest {
    @Test
    public void correctListKeysTest() throws Exception {
        StmtTestUtils.parseYangSource("/list-keys-test/correct-list-keys-test.yang");
    }

    @Test
    public void incorrectListKeysTest1() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/list-keys-test/incorrect-list-keys-test.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'"));
    }

    @Test
    public void incorrectListKeysTest2() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/list-keys-test/incorrect-list-keys-test2.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Key 'test1_key1 test1_key2' misses node 'test1_key2'"));
    }

    @Test
    public void incorrectListKeysTest3() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/list-keys-test/incorrect-list-keys-test3.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Key 'grp_list' misses node 'grp_list'"));
    }

    @Test
    public void incorrectListKeysTest4()  {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/list-keys-test/incorrect-list-keys-test4.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Key 'grp_leaf' misses node 'grp_leaf'"));
    }
}
