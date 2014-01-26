package org.opendaylight.yangtools.sal.binding.generator.impl;

public interface SchemaLock {

    public void waitForSchema(Class<?> cls);
    
}
