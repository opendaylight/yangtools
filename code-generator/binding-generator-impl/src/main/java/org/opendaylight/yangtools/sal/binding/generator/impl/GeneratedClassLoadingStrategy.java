package org.opendaylight.yangtools.sal.binding.generator.impl;

import org.opendaylight.yangtools.sal.binding.generator.util.ClassLoaderUtils;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

public abstract class GeneratedClassLoadingStrategy {

    private static final GeneratedClassLoadingStrategy TCCL_STRATEGY = new TCCLClassLoadingStrategy();

    public Class<?> loadClass(Type type) throws ClassNotFoundException {
        return loadClass(type.getFullyQualifiedName());
    }

    public abstract Class<?> loadClass(String fullyQualifiedName) throws ClassNotFoundException;
    
    
    public static final GeneratedClassLoadingStrategy getTCCLClassLoadingStrategy() {
        return TCCL_STRATEGY;
    }
     
    private static final class TCCLClassLoadingStrategy extends GeneratedClassLoadingStrategy {
        
        @Override
        public Class<?> loadClass(String fullyQualifiedName) throws ClassNotFoundException {
            return ClassLoaderUtils.loadClassWithTCCL(fullyQualifiedName);
        }
    }
}
