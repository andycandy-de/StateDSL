package de.andycandy.state_dsl

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import groovy.transform.CompileStatic

@CompileStatic
class App {

    static void main(String[] args) {
		
		App app = new App()
		
		println app.start(args)
    }
	
	Object start(String[] args) {
		
		if (args.length != 1) {
			printErr('Usage: StateDSL [file]')
			systemExit()
			return
		}
		
		File file = new File(args[0])
		if (!file.isFile()) {
			printErr("File ${file.absolutePath} not exists!")
			systemExit()
			return
		}
		
		return evaluate(file)
	}
	
	Object evaluate(File file) {
		
		ImportCustomizer importCustomizer = new ImportCustomizer()
		importCustomizer.addStarImports('de.andycandy.state_dsl')
		importCustomizer.addImports('de.andycandy.state_dsl.StateDSL.state')
		
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration()
		compilerConfiguration.addCompilationCustomizers(importCustomizer)
		
		GroovyShell groovyShell = new GroovyShell(StateMachine.class.getClassLoader(), new Binding(), compilerConfiguration)
		
		groovyShell.evaluate(file)
	}
	
	void printErr(String text) {
		System.err.println(text)
	}
	
	void systemExit() {
		System.exit(-1)
	}
}