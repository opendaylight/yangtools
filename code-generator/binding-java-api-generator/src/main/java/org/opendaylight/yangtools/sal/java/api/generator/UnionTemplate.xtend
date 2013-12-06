package org.opendaylight.yangtools.sal.java.api.generator

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import java.beans.ConstructorProperties

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
        «IF properties.empty && !parentProperties.empty»
            «parentConstructor»
        «ENDIF»
    '''

    private def unionConstructors() '''
        «FOR property : finalProperties SEPARATOR "\n"»
            «val isCharArray = "char[]".equals(property.returnType.name)»
            «IF isCharArray»
                /**
                 * Constructor provided only for using in JMX. Don't use it for
                 * construction new object of this union type. 
                 */
                @«ConstructorProperties.importedName»("«property.name»")
                public «type.name»(«property.returnType.importedName» «property.fieldName») {
                    «String.importedName» defVal = new «String.importedName»(«property.fieldName»);
                    «type.name» defInst = «type.name»Builder.getDefaultInstance(defVal);
                    «FOR other : finalProperties»
                        «IF other.name.equals("value")»
                            this.«other.fieldName» = «other.fieldName»;
                        «ELSE»
                            this.«other.fieldName» = defInst.«other.fieldName»;
                        «ENDIF»
                    «ENDFOR»
                }
            «ELSE»
                «val propertyAndTopParentProperties = parentProperties + #[property]»
                public «type.name»(«propertyAndTopParentProperties.asArgumentsDeclaration») {
                    super(«parentProperties.asArguments»);
                    this.«property.fieldName» = «property.fieldName»;
                    «FOR other : finalProperties»
                        «IF property != other»this.«other.fieldName» = null;«ENDIF»
                    «ENDFOR»
                }
            «ENDIF»
        «ENDFOR»
    '''

    private def unionConstructorsParentProperties() '''
        «FOR property : parentProperties SEPARATOR "\n"»
            public «type.name»(«property.returnType.importedName» «property.fieldName») {
                super(«property.fieldName»);
            }
        «ENDFOR»
    '''

    override protected copyConstructor() '''
        /**
         * Creates a copy from Source Object.
         *
         * @param source Source object
         */
        public «type.name»(«type.name» source) {
            «IF !parentProperties.empty»
                super(source);
            «ENDIF»
            «IF !properties.empty»
                «FOR p : properties»
                    this.«p.fieldName» = source.«p.fieldName»;
                «ENDFOR»
            «ENDIF»
        }
    '''

}
