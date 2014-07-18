/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.RandomPrefix;

/**
 * @author tkubas
 */
public class RandomPrefixTest {
    private RandomPrefix randomPrefix;

    /**
     * setup {@link #randomPrefix} instance
     */
    @Before
    public void setUp() {
        randomPrefix = new RandomPrefix();
    }
    /**
     * Test method for {@link org.opendaylight.yangtools.yang.data.impl.codec.xml.RandomPrefix#encodeQName(QName)}.
     */
    @Test
    public void testEncodeQName() {
        QName node = QName.create("","2013-06-07","node");
        String encodedQName = randomPrefix.encodeQName(node);
        Assert.assertNotNull(encodedQName);
        Assert.assertTrue("prefix is expected to contain 4 small letters as prefix but result is: "+encodedQName,
                encodedQName.matches("[a-z]{4}:node"));
    }

}
