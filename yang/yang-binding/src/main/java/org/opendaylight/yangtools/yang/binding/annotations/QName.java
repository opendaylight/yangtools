package org.opendaylight.yangtools.yang.binding.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface QName {

    String namespace();
    String revision();
    String name();

}
