/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;

import java.util.ArrayList;
import java.util.List;

/**
**/
public class IpPrefixBuilder {

    public static IpPrefix getDefaultInstance(String defaultValue) {
        String ipv4Pattern = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])/(([0-9])|([1-2][0-9])|(3[0-2]))";
        String ipv6Pattern1 = "((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(/(([0-9])|([0-9]{2})|(1[0-1][0-9])|(12[0-8])))";
        String ipv6Pattern2 = "(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(/.+)";

        List<String> matchers = new ArrayList<>();
        if (defaultValue.matches(ipv4Pattern)) {
            matchers.add(Ipv4Address.class.getSimpleName());
        }
        if (defaultValue.matches(ipv6Pattern1) && defaultValue.matches(ipv6Pattern2)) {
            matchers.add(Ipv6Address.class.getSimpleName());
        }
        if (matchers.size() > 1) {
            throw new IllegalArgumentException("Cannot create IpPrefix from " + defaultValue
                    + ". Value is ambigious for " + matchers);
        }

        if (defaultValue.matches(ipv4Pattern)) {
            Ipv4Prefix ipv4 = new Ipv4Prefix(defaultValue);
            return new IpPrefix(ipv4);
        }
        if (defaultValue.matches(ipv6Pattern1) && defaultValue.matches(ipv6Pattern2)) {
            Ipv6Prefix ipv6 = new Ipv6Prefix(defaultValue);
            return new IpPrefix(ipv6);
        }
        throw new IllegalArgumentException("Cannot create IpPrefix from " + defaultValue);
    }

}
