package org.opendaylight.yangtools.sal.java.api.generator;

import org.opendaylight.yangtools.sal.binding.model.api.CodeGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

public abstract class AbstractCodeGenerator implements CodeGenerator {
	
	@Override
    public boolean isAcceptable(Type type) {
    	return (type instanceof GeneratedType  && !(type instanceof GeneratedTransferObject));
    }

	
}
