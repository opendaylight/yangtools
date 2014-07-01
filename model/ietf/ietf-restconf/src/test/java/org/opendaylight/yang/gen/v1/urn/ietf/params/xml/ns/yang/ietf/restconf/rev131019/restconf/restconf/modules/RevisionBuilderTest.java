/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.restconf.restconf.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.restconf.restconf.modules.Module.Revision;

public class RevisionBuilderTest {
    @Test
    public void testEmptyString() {
        Revision revision = RevisionBuilder.getDefaultInstance("");
        validate(revision, "", null);
    }

    @Test
    public void testValidDataString() {
        String dateString = "2014-04-23";
        Revision revision = RevisionBuilder.getDefaultInstance(dateString);
        validate(revision, null, new RevisionIdentifier(dateString));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullString() {
        RevisionBuilder.getDefaultInstance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadFormatString() {
        RevisionBuilder.getDefaultInstance("badFormat");
    }

    private void validate(final Revision revisionUnderTest, final String expectedRevisionString,
            final RevisionIdentifier expectedRevisionIdentifier) {
        assertNotNull(revisionUnderTest);
        assertEquals(expectedRevisionString, revisionUnderTest.getString());
        assertEquals(expectedRevisionIdentifier, revisionUnderTest.getRevisionIdentifier());
    }
}