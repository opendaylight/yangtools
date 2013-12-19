package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
**/
public class HostBuilder {
    private static final Pattern ipv4Pattern = Pattern.compile("(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(%[\\p{N}\\p{L}]+)?");
    private static final Pattern  ipv6Pattern1 = Pattern.compile("((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(%[\\p{N}\\p{L}]+)?");
    private static final Pattern ipv6Pattern2 = Pattern.compile("(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(%.+)?");
    private static final Pattern domainPattern = Pattern.compile("((([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.)*([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.?)|\\.");

    public static Host getDefaultInstance(String defaultValue) {

        List<String> matchers = new ArrayList<>();
        if (ipv4Pattern.matcher(defaultValue).matches()) {
            matchers.add(Ipv4Address.class.getSimpleName());
        }
        if (ipv6Pattern1.matcher(defaultValue).matches() && ipv6Pattern2.matcher(defaultValue).matches()) {
            matchers.add(Ipv6Address.class.getSimpleName());
        }
        if (domainPattern.matcher(defaultValue).matches()) {
            matchers.add(DomainName.class.getSimpleName());
        }
        if (matchers.size() > 1) {
            throw new IllegalArgumentException("Cannot create Host from " + defaultValue + ". Value is ambigious for "
                    + matchers);
        }

        if (ipv4Pattern.matcher(defaultValue).matches()) {
            Ipv4Address ipv4 = new Ipv4Address(defaultValue);
            IpAddress ipAddress = new IpAddress(ipv4);
            return new Host(ipAddress);
        }
        if (ipv6Pattern1.matcher(defaultValue).matches() && ipv6Pattern2.matcher(defaultValue).matches()) {
            Ipv6Address ipv6 = new Ipv6Address(defaultValue);
            IpAddress ipAddress = new IpAddress(ipv6);
            return new Host(ipAddress);
        }
        if (domainPattern.matcher(defaultValue).matches()) {
            DomainName domainName = new DomainName(defaultValue);
            return new Host(domainName);
        }
        throw new IllegalArgumentException("Cannot create Host from " + defaultValue);
    }

}
