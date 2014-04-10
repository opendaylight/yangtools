package org.opendaylight.yangtools.yang.data.api;

public interface PrefixToNSMapper {
    String getNamespace(String prefix);
}
