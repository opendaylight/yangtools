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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 **/
public final class HostBuilder {
    private static final Pattern IPV4_PATTERN = Pattern.compile("(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(%[\\p{N}\\p{L}]+)?");
    private static final Pattern IPV6_PATTERN1 = Pattern.compile("((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(%[\\p{N}\\p{L}]+)?");
    private static final Pattern IPV6_PATTERN2 = Pattern.compile("(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(%.+)?");
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("((([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.)*([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.?)|\\.");

    private HostBuilder() {

    }

    public static Host getDefaultInstance(final String defaultValue) {
        final Matcher ipv4Matcher = IPV4_PATTERN.matcher(defaultValue);
        final Matcher ipv6Matcher1 = IPV6_PATTERN1.matcher(defaultValue);
        final Matcher ipv6Matcher2 = IPV6_PATTERN2.matcher(defaultValue);
        final Matcher domainMatcher = DOMAIN_PATTERN.matcher(defaultValue);

        List<String> matchers = new ArrayList<>(3);
        if (ipv6Matcher1.matches() || ipv6Matcher2.matches()) {
            matchers.add(Ipv6Address.class.getSimpleName());
        }

        // Ipv4 and Domain Name patterns are not exclusive
        // Address 127.0.0.1 matches both patterns
        // This way Ipv4 address is preferred to domain name
        if (ipv4Matcher.matches()) {
            matchers.add(Ipv4Address.class.getSimpleName());
        } else if (domainMatcher.matches()) {
            matchers.add(DomainName.class.getSimpleName());
        }

        if (matchers.size() > 1) {
            throw new IllegalArgumentException("Cannot create Host from " + defaultValue + ". Value is ambigious for "
                    + matchers);
        }

        if (ipv4Matcher.matches()) {
            Ipv4Address ipv4 = new Ipv4Address(defaultValue);
            IpAddress ipAddress = new IpAddress(ipv4);
            return new Host(ipAddress);
        }
        if (ipv6Matcher1.matches() || ipv6Matcher2.matches()) {
            Ipv6Address ipv6 = new Ipv6Address(defaultValue);
            IpAddress ipAddress = new IpAddress(ipv6);
            return new Host(ipAddress);
        }
        if (domainMatcher.matches()) {
            DomainName domainName = new DomainName(defaultValue);
            return new Host(domainName);
        }
        throw new IllegalArgumentException("Cannot create Host from " + defaultValue);
    }

}
