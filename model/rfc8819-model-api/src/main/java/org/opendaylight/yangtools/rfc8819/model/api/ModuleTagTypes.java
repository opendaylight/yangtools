package org.opendaylight.yangtools.rfc8819.model.api;

public enum ModuleTagTypes {
    IETF("ietf:"),
    VENDOR("vendor:"),
    USER("user"),
    RESERVED(),
    INVALID();

    private final String prefix;

    ModuleTagTypes(String prefix) {
        this.prefix = prefix;
    }
    ModuleTagTypes() {
        this.prefix = null;
    }

    public String getPrefix() {
        return prefix;
    }
}
