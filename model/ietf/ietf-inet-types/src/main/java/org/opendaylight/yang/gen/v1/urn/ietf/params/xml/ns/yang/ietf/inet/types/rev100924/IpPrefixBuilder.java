/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 **/
public final class IpPrefixBuilder {
    private static final Pattern IPV4_PATTERN = Pattern.compile("(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])/(([0-9])|([1-2][0-9])|(3[0-2]))");
    private static final Pattern IPV6_PATTERN1 = Pattern.compile("((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(/(([0-9])|([0-9]{2})|(1[0-1][0-9])|(12[0-8])))");
    private static final Pattern IPV6_PATTERN2 = Pattern.compile("(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(/.+)");

    private IpPrefixBuilder() {

    }

    public static IpPrefix getDefaultInstance(final String defaultValue) {
        final Matcher ipv4Matcher = IPV4_PATTERN.matcher(defaultValue);

        if (ipv4Matcher.matches()) {
            if (IPV6_PATTERN1.matcher(defaultValue).matches() && IPV6_PATTERN2.matcher(defaultValue).matches()) {
                throw new IllegalArgumentException(
                        String.format("Cannot create IpPrefix from \"%s\", matches both %s and %s",
                                defaultValue, Ipv4Address.class.getSimpleName(), Ipv6Address.class.getSimpleName()));

            }
            return new IpPrefix(new Ipv4Prefix(defaultValue));
        } else if (IPV6_PATTERN1.matcher(defaultValue).matches() && IPV6_PATTERN2.matcher(defaultValue).matches()) {
            return new IpPrefix(new Ipv6Prefix(defaultValue));
        } else {
            throw new IllegalArgumentException("Cannot create IpPrefix from " + defaultValue);
        }
    }

}
