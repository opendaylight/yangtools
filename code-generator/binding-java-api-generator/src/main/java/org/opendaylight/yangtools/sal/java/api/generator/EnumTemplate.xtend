package org.opendaylight.yangtools.sal.java.api.generator

import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
/**
 * Template for generating JAVA enumeration type. 
 */
class EnumTemplate {
    
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
        this.enums = enums
    }
    
    /**
     * Generates JAVA source code for the enumeration with the package name.
     * 
     * @return JAVA source code for enumeration and for the package name 
     */
    def String generate() {
        val body = generateBody
        val pkg = generatePkg
        return pkg.toString + body.toString
    }
    
    /**
     * Generates only JAVA enumeration source code.
     * 
     * @return string with JAVA enumeration source code
     */
    def generateAsInnerClass() {
        return generateBody
    }
    
    /**
     * Template method which generates enumeration body (declaration + enumeration items).
     * 
     * @return string with the enumeration body 
     */
    def private generateBody() '''
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
    
    /**
     * Template method which generates the package name line.
     * 
     * @return string with the package name line   
     */
    def private generatePkg() '''
        package «enums.packageName»;
        
        
    '''
    
}