/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.restconf.restconf.modules;

import java.util.regex.Pattern;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.restconf.restconf.modules.Module.Revision;

/**
**/
public class RevisionBuilder {

    /**
     * Defines the pattern for revisions. NOTE: This pattern will likely be
     * updated in future versions of the ietf and should be adjusted accordingly
     */
    private static final Pattern REVISION_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    public static Revision getDefaultInstance(String defaultValue) {

        if (defaultValue != null) {
            if (REVISION_PATTERN.matcher(defaultValue).matches()) {
                RevisionIdentifier id = new RevisionIdentifier(defaultValue);
                return new Revision(id);
            }
            if (defaultValue.isEmpty()) {
                return new Revision(defaultValue);
            }
        }

        throw new IllegalArgumentException("Cannot create Revision from " + defaultValue
                + ". Default value does not match pattern " + REVISION_PATTERN.pattern()
                + " or empty string.");
    }

}
