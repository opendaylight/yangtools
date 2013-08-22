package org.opendaylight.yangtools.sal.java.api.generator

import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
/**
 * Template for generating JAVA enumeration type. 
 */
class EnumTemplate extends BaseTemplate {

    
    /**
     * Enumeration which will be transformed to JAVA source code for enumeration
     */
    val Enumeration enums
    
    /**
     * Constructs instance of this class with concrete <code>enums</code>.
     * 
     * @param enumeration which will be transformed to JAVA source code 
     */
    new(Enumeration enums) {
        super(enums as GeneratedType )
        this.enums = enums
    }
    
    
    /**
     * Generates only JAVA enumeration source code.
     * 
     * @return string with JAVA enumeration source code
     */
    def generateAsInnerClass() {
        return body
    }
    
    /**
     * Template method which generates enumeration body (declaration + enumeration items).
     * 
     * @return string with the enumeration body 
     */
    override body() '''
        public enum «enums.name» {
        «FOR v : enums.values SEPARATOR ",\n"»
            «"    "»«v.name»(«v.value»)«
        ENDFOR»;
        
            int value;
        
            private «enums.name»(int value) {
                this.value = value;
            }
        }
    '''
}