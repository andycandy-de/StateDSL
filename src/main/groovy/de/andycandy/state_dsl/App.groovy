package de.andycandy.state_dsl

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import groovy.transform.CompileStatic

@CompileStatic
class App {

    static void main(String[] args) {
		
		if (args.length != 1) {
			System.err.println('Usage: state [file]')
			System.exit(-1)
		}
		
		File file = new File(args[0])
		if (!file.isFile()) {
			System.err.println('File not exists!')
			System.exit(-1)
		}
		
		evaluate(file)
    }
	
	static Object evaluate(File file) {
		
		ImportCustomizer importCustomizer = new ImportCustomizer()
		importCustomizer.addStarImports('de.andycandy.state_dsl')
		importCustomizer.addImports('de.andycandy.state_dsl.StateDSL.state')
		
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration()
		compilerConfiguration.addCompilationCustomizers(importCustomizer)
		
		GroovyShell groovyShell = new GroovyShell(StateMachine.class.getClassLoader(), new Binding(), compilerConfiguration)
		
		groovyShell.evaluate(file)
	}
}