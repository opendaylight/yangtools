package org.opendaylight.yangtools.sal.binding.generator.impl;

public class NameTypePattern {
    final String name;
    final String type;

    public NameTypePattern(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }
}