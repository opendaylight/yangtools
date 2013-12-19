package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.regex.Pattern;

import com.google.common.net.InetAddresses;


/**
**/
public final class IpPrefixBuilder {
    private static final Pattern PATTERN = Pattern.compile("/");

    private IpPrefixBuilder() {
    }

    public static IpPrefix getDefaultInstance(final String defaultValue) {
        final String[] parts = PATTERN.split(defaultValue);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected string in format ADDRESS/LENGTH");
        }

        final InetAddress a = InetAddresses.forString(parts[0]);
        if (a instanceof Inet4Address) {
            return new IpPrefix(new Ipv4Prefix(defaultValue));
        } else if (a instanceof Inet6Address) {
            return new IpPrefix(new Ipv6Prefix(defaultValue));
        } else {
            throw new IllegalArgumentException("Unhandled address type " + a.getClass());
        }
    }
}
