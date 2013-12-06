package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;

import java.util.ArrayList;
import java.util.List;

/**
**/
public class IpAddressBuilder {

    public static IpAddress getDefaultInstance(String defaultValue) {
        String ipv4Pattern = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(%[\\p{N}\\p{L}]+)?";
        String ipv6Pattern1 = "((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(%[\\p{N}\\p{L}]+)?";
        String ipv6Pattern2 = "(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(%.+)?";

        List<String> matchers = new ArrayList<>();
        if (defaultValue.matches(ipv4Pattern)) {
            matchers.add(Ipv4Address.class.getSimpleName());
        }
        if (defaultValue.matches(ipv6Pattern1) && defaultValue.matches(ipv6Pattern2)) {
            matchers.add(Ipv6Address.class.getSimpleName());
        }
        if (matchers.size() > 1) {
            throw new IllegalArgumentException("Cannot create IpAddress from " + defaultValue
                    + ". Value is ambigious for " + matchers);
        }

        if (defaultValue.matches(ipv4Pattern)) {
            Ipv4Address ipv4 = new Ipv4Address(defaultValue);
            return new IpAddress(ipv4);
        }
        if (defaultValue.matches(ipv6Pattern1) && defaultValue.matches(ipv6Pattern2)) {
            Ipv6Address ipv6 = new Ipv6Address(defaultValue);
            return new IpAddress(ipv6);
        }
        throw new IllegalArgumentException("Cannot create IpAddress from " + defaultValue);
    }

}
