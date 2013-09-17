package org.opendaylight.yangtools.yang.binding.annotations;

import java.lang.annotation.Inherited;

import org.opendaylight.yangtools.yang.binding.BaseIdentity;

@Inherited
public @interface RoutingContext {

    Class<? extends BaseIdentity> value();
}
