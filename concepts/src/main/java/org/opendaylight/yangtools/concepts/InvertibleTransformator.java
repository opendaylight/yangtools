package org.opendaylight.yangtools.concepts;

public interface InvertibleTransformator<P, I> extends Transformator<P, I>{

    I fromProduct(P product);

}
