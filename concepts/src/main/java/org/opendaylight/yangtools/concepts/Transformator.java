package org.opendaylight.yangtools.concepts;

public interface Transformator<P,I> {

    P transform(I input);
}
