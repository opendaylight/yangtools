package org.opendaylight.yangtools.sal.java.api.generator

import java.util.List
import java.util.Map
import org.opendaylight.yangtools.binding.generator.util.TypeConstants
import org.opendaylight.yangtools.sal.binding.model.api.Constant
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature
import org.opendaylight.yangtools.sal.binding.model.api.Type
import java.util.LinkedHashMap
/**
 * Template for generating JAVA interfaces. 
 */
class InterfaceTemplate {
    
    /**
     * Generated type which is transformed to interface JAVA file.
     */
    val GeneratedType genType
    
    /**
     * Map of imports for this <code>genTO</code>.
     */
    val Map<String, String> imports
    
    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    val List<Constant> consts
    
    /**
     * List of method signatures which are generated as method declarations.
     */
    val List<MethodSignature> methods
    
    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    val List<Enumeration> enums
    
    /**
     * List of generated types which are enclosed inside <code>genType</code>
     */
    val List<GeneratedType> enclosedGeneratedTypes
    
    /**
     * Creates the instance of this class which is used for generating the interface file source 
     * code from <code>genType</code>.
     * 
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    new(GeneratedType genType) {
        if (genType == null) {
            throw new IllegalArgumentException("Generated type reference cannot be NULL!")
        }
        
        this.genType = genType
        imports = GeneratorUtil.createImports(genType)
        consts = genType.constantDefinitions
        methods = genType.methodDefinitions
        enums = genType.enumerations
        enclosedGeneratedTypes = genType.enclosedTypes
    }
    
    /**
     * Generates the source code for interface with package name, imports and interface body.
     * 
     * @return string with the code for the interface file in JAVA format
     */
    def String generate() {
        val body = generateBody
        val pkgAndImports = generatePkgAndImports
        return pkgAndImports.toString + body.toString
    }
    
    /**
     * Template method which generate the whole body of the interface.
     * 
     * @return string with code for interface body in JAVA format
     */
    def private generateBody() '''
        «genType.comment.generateComment»
        «generateIfcDeclaration» {
        
            «generateInnerClasses»
        
            «generateEnums»
        
            «generateConstants»
        
            «generateMethods»
        
        }
        
    '''
    
    /**
     * Template method which generates JAVA comment.
     * 
     * @param comment 
     * string with the comment for whole JAVA interface
     * @return string with comment in JAVA format
     */
    def private generateComment(String comment) '''
        «IF comment != null && !comment.empty»
            /*
            «comment»
            */
        «ENDIF»
    '''
    
    /**
     * Template method which generates the interface name declaration.
     * 
     * @return string with the code for the interface declaration in JAVA format
     */
    def private generateIfcDeclaration() '''
        public interface «genType.name»«
        IF (!genType.implements.empty)»«
            " extends "»«
            FOR type : genType.implements SEPARATOR ", "»«
                type.resolveName»«
            ENDFOR»«
        ENDIF
    »'''
    
    /**
     * Template method which generates inner classes inside this interface.
     * 
     * @return string with the source code for inner classes in JAVA format
     */
    def private generateInnerClasses() '''
        «IF !enclosedGeneratedTypes.empty»
            «FOR innerClass : enclosedGeneratedTypes SEPARATOR "\n"»
                «IF (innerClass instanceof GeneratedTransferObject)»
                    «val classTemplate = new ClassTemplate(innerClass as GeneratedTransferObject)»
                    «classTemplate.generateAsInnerClass»
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates JAVA enum type.
     * 
     * @return string with inner enum source code in JAVA format
     */    
    def private generateEnums() '''
        «IF !enums.empty»
            «FOR e : enums SEPARATOR "\n"»
                «val enumTemplate = new EnumTemplate(e)»
                «enumTemplate.generateAsInnerClass»
            «ENDFOR»
        «ENDIF»
    '''
    
    /**
     * Template method wich generates JAVA constants.
     * 
     * @return string with constants in JAVA format 
     */    
    def private generateConstants() '''
        «IF !consts.empty»
            «FOR c : consts»
                «IF c.name != TypeConstants.PATTERN_CONSTANT_NAME»
                    public static final «c.type.resolveName» «c.name» = «c.value»;
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates the declaration of the methods.
     * 
     * @return string with the declaration of methods source code in JAVA format 
     */    
    def private generateMethods() '''
        «IF !methods.empty»
            «FOR m : methods SEPARATOR "\n"»
                «m.comment.generateComment»
                «m.returnType.resolveName» «m.name»(«m.parameters.generateParameters»);
            «ENDFOR»
        «ENDIF»
    '''
    
    /**
     * Template method which generates method parameters with their types from <code>parameters</code>.
     * 
     * @param parameters
     * list of parameter instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    def private generateParameters(List<MethodSignature.Parameter> parameters) '''«
        IF !parameters.empty»«
            FOR parameter : parameters SEPARATOR ", "»«
                parameter.type.resolveName» «parameter.name»«
            ENDFOR»«
        ENDIF
    »'''
    

    /**
     * Template method which generates the map of all the required imports for and imports 
     * from extended type (and recursivelly so on).
     * 
     * @return map which maps type name to package name   
     */
    def private Map<String, String> resolveImports() {
        val innerTypeImports = GeneratorUtil.createChildImports(genType)
        val Map<String, String> resolvedImports = new LinkedHashMap
        for (Map.Entry<String, String> entry : imports.entrySet() + innerTypeImports.entrySet) {
            val typeName = entry.getKey();
            val packageName = entry.getValue();
            if (packageName != genType.packageName && packageName != genType.fullyQualifiedName) {
                resolvedImports.put(typeName, packageName);
            }
        }
        return resolvedImports
    }
    
    /**
     * Template method which generate package name line and import lines.
     * 
     * @result string with package and import lines in JAVA format
     */    
    def private generatePkgAndImports() '''
        package «genType.packageName»;
        
        
        «IF !imports.empty»
            «FOR entry : resolveImports.entrySet»
                import «entry.value».«entry.key»;
            «ENDFOR»
        «ENDIF»
        
    '''    
    
    /**
     * Adds package to imports if it is necessary and returns necessary type name (with or without package name)
     *  
     * @param type JAVA <code>Type</code> 
     * @return string with the type name (with or without package name)
     */      
    def private resolveName(Type type) {
        GeneratorUtil.putTypeIntoImports(genType, type, imports);
        GeneratorUtil.getExplicitType(genType, type, imports)
    }
    
}   