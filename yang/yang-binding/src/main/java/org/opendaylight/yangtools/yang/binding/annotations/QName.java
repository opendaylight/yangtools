package org.opendaylight.yangtools.yang.binding.annotations;

public @interface QName {

    String namespace();
    String revision();
    String name();
    
}
