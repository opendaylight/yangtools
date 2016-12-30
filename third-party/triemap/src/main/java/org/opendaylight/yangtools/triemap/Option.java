package org.opendaylight.yangtools.triemap;

/**
 * Mimic Option in Scala
 *
 * @author Roman Levenstein <romixlev@gmail.com>
 *
 * @param <V>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class Option<V> {
    static None none = new None();
    public static <V> Option<V> makeOption(final V o){
        if(o!=null) {
            return new Some<>(o);
        } else {
            return none;
        }
    }

    public static <V> Option<V> makeOption(){
        return none;
    }
    public boolean nonEmpty () {
        return false;
    }
}
