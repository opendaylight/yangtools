/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Date;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

public class RandomPrefixTest {
    static final int MAX_COUNTER = 4000;

    @Test
    public void testEncodeDecode() throws Exception {

        final List<String> allGenerated = Lists.newArrayList();
        for (int i = 0; i < MAX_COUNTER; i++) {
            final String encoded = RandomPrefix.encode(i);
            assertEquals(RandomPrefix.decode(encoded), i);
            allGenerated.add(encoded);
        }

        assertEquals(allGenerated.size(), MAX_COUNTER);
        assertEquals("dPT", allGenerated.get(MAX_COUNTER - 1));
        assertEquals("a", allGenerated.get(0));
        assertEquals(allGenerated.size(), Sets.newHashSet(allGenerated).size());
    }

    @Test
    public void testQNameWithPrefix() throws Exception {
        final RandomPrefix a = new RandomPrefix();

        final List<String> allGenerated = Lists.newArrayList();
        for (int i = 0; i < MAX_COUNTER; i++) {
            final String prefix = RandomPrefix.encode(i);
            final URI uri = new URI("localhost:" + prefix);
            final QName qName = QName.create(QNameModule.create(uri, new Date()), "local-name");
            allGenerated.add(a.encodePrefix(qName.getNamespace()));
        }

        assertEquals(MAX_COUNTER, allGenerated.size());
        // We are generating MAX_COUNTER_VALUE + 27 prefixes total, so we should encounter a reset in prefix a start
        // from 0 at some point
        // At the end, there should be only 27 values in RandomPrefix cache
        assertEquals(MAX_COUNTER, Iterables.size(a.getPrefixes()));
        assertThat(allGenerated, CoreMatchers.not(CoreMatchers.hasItem("xml")));
        assertThat(allGenerated, CoreMatchers.not(CoreMatchers.hasItem("xmla")));
        assertThat(allGenerated, CoreMatchers.not(CoreMatchers.hasItem("xmlz")));

        assertEquals(1, Iterables.frequency(allGenerated, "a"));
    }

    @Test
    public void test2QNames1Namespace() throws Exception {
        final RandomPrefix a = new RandomPrefix();

        final URI uri = URI.create("localhost");
        final QName qName = QName.create(QNameModule.create(uri, new Date()), "local-name");
        final QName qName2 = QName.create(QNameModule.create(uri, new Date()), "local-name");

        assertEquals(a.encodePrefix(qName.getNamespace()), a.encodePrefix(qName2.getNamespace()));
    }

    @Test
    public void testQNameNoPrefix() throws Exception {
        final RandomPrefix a = new RandomPrefix();

        final URI uri = URI.create("localhost");
        QName qName = QName.create(uri, new Date(), "local-name");
        assertEquals("a", a.encodePrefix(qName.getNamespace()));
        qName = QName.create(QNameModule.create(uri, new Date()), "local-name");
        assertEquals("a", a.encodePrefix(qName.getNamespace()));
        qName = QName.create(QNameModule.create(URI.create("second"), new Date()), "local-name");
        assertEquals("b", a.encodePrefix(qName.getNamespace()));

    }
}
