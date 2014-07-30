package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

class StaticConstantDefinition {

    final String name;
    final Class<?> type;
    final Object value;

    public StaticConstantDefinition(final String name, final Class<?> type, final Object value) {
        super();
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StaticConstantDefinition other = (StaticConstantDefinition) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
