/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import org.junit.Test;

/**
 * Unit tests for BlockingThreadPoolExecutor.
 *
 * @author Thomas Pantelis
 */
public class BlockingThreadPoolExecutorTest {

    @Test
    public void testExecution() throws Exception {

        CommonTestUtils.testThreadPoolExecution(
                new BlockingThreadPoolExecutor( 50, 5000, "TestPool" ), 100000, "TestPool" );
    }

    @Test
    public void testExecutionWithSmallQueueLimit() throws Exception {

        CommonTestUtils.testThreadPoolExecution(
                new BlockingThreadPoolExecutor( 20, 1, "TestPool" ), 10000, "TestPool" );
    }
}
