package org.opendaylight.yangtools.sal.java.api.generator

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject


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
    «unionConstructorsParentProperties»
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

     def unionConstructorsParentProperties() '''
        «FOR property : parentProperties SEPARATOR "\n"»
            public «type.name»(«property.returnType.importedName» «property.fieldName») {
                super(«property.fieldName»);
            }
        «ENDFOR»
     ''' 
}
