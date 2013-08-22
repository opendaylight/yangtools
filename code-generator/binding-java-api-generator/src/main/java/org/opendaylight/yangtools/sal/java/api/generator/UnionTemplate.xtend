package org.opendaylight.yangtools.sal.java.api.generator

import java.util.List
import java.util.Map
import org.opendaylight.yangtools.binding.generator.util.TypeConstants
import org.opendaylight.yangtools.sal.binding.model.api.Constant
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.binding.generator.util.Types
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType


/**
 * Template for generating JAVA class. 
 */
class UnionTemplate extends ClassTemplate {


    
    /**
     * Creates instance of this class with concrete <code>genType</code>.
     * 
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        super(genType)
    }
    

    
    
    override constructors() '''
    «unionConstructors»
    «IF !allProperties.empty»
    «copyConstructor»
    «ENDIF»
    «IF properties.empty && !parentProperties.empty »
        «parentConstructor»
    «ENDIF»
    '''
    

     def unionConstructors() '''
        «FOR property : finalProperties SEPARATOR "\n"»
                «val propertyAndTopParentProperties = parentProperties + #[property]»
                public «type.name»(«propertyAndTopParentProperties.asArgumentsDeclaration») {
                    super(«parentProperties.asArguments»);
                    this.«property.fieldName» = «property.fieldName»;
                    «FOR other : finalProperties»
                    «IF property != other»this.«other.fieldName» = null;«ENDIF»
                    «ENDFOR»
                }
        «ENDFOR»
     ''' 
}
