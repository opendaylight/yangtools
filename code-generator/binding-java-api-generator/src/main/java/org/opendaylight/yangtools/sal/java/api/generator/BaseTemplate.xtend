package org.opendaylight.yangtools.sal.java.api.generator

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import java.util.Map
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.binding.generator.util.Types
import com.google.common.base.Splitter

abstract class BaseTemplate {
    
    
    protected val GeneratedType type;
    protected val Map<String,String> importMap;
    static val paragraphSplitter = Splitter.on("\n\n").omitEmptyStrings();
    new(GeneratedType _type) {
         if (_type== null) {
            throw new IllegalArgumentException("Generated type reference cannot be NULL!")
        }
        this.type = _type;
        this.importMap = GeneratorUtil.createImports(type)
    }
    
    def packageDefinition () '''package «type.packageName»;'''


    protected def getFullyQualifiedName() {
        return type.fullyQualifiedName
    }
    
    final public def generate() {
    val _body = body()
    '''
    «packageDefinition»
    «imports»
    
    «_body»
    '''.toString
    }
    protected def imports()  ''' 
        «IF !importMap.empty»
            «FOR entry : importMap.entrySet»
                «IF entry.value != fullyQualifiedName»
                    import «entry.value».«entry.key»;
                «ENDIF»
            «ENDFOR»
        «ENDIF»
        
    '''
    
    protected abstract def CharSequence body();

    // Helper patterns
    
    final protected def fieldName(GeneratedProperty property) '''_«property.name»'''
    
    /**
     * Template method which generates the getter method for <code>field</code>
     * 
     * @param field 
     * generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format 
     */
    final protected def getterMethod(GeneratedProperty field) {
    '''
        public «field.returnType.importedName» «field.getterMethodName»() {
            return «field.fieldName»;
        }
    '''
    }
    
    final protected def getterMethodName(GeneratedProperty field) {
        val prefix = if(field.returnType.equals(Types.BOOLEAN)) "is" else "get"
        return '''«prefix»«field.name.toFirstUpper»'''
    }
    
    /**
     * Template method which generates the setter method for <code>field</code>
     * 
     * @param field 
     * generated property with data about field which is generated as the setter method
     * @return string with the setter method source code in JAVA format 
     */
    final protected def setterMethod(GeneratedProperty field) '''
        «val returnType = field.returnType.importedName»
        public «type.name» set«field.name.toFirstUpper»(«returnType» value) {
            this.«field.fieldName» = value;
            return this;
        }
    '''
    
    final protected def importedName(Type intype) {
        GeneratorUtil.putTypeIntoImports(type, intype, importMap);
        GeneratorUtil.getExplicitType(type, intype, importMap)
    }
    
    final protected def importedName(Class cls) {
        importedName(Types.typeForClass(cls))
    }
    
    /**
     * Template method which generates method parameters with their types from <code>parameters</code>.
     * 
     * @param parameters
     * group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    def final protected asArgumentsDeclaration(Iterable<GeneratedProperty> parameters) 
    '''«IF !parameters.empty»«FOR parameter : parameters SEPARATOR ", "»«parameter.returnType.importedName» «parameter.fieldName»«ENDFOR»«ENDIF»'''
    
    /**
     * Template method which generates sequence of the names of the class attributes from <code>parameters</code>.
     * 
     * @param parameters 
     * group of generated property instances which are transformed to the sequence of parameter names
     * @return string with the list of the parameter names of the <code>parameters</code> 
     */
    def final protected asArguments(Iterable<GeneratedProperty> parameters) 
    '''«IF !parameters.empty»«FOR parameter : parameters SEPARATOR ", "»«parameter.fieldName»«ENDFOR»«ENDIF»'''
    
    
        /**
     * Template method which generates JAVA comments.
     * 
     * @param comment string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    def protected CharSequence asJavadoc(String comment) {
        if (comment==null) return '';
        val paragraphs = paragraphSplitter.split(comment)
        
        return '''
            /**
              «FOR p : paragraphs SEPARATOR "<p>"»
              «p»
              «ENDFOR»
            **/
            '''
    }
}