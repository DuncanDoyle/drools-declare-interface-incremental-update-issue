package org.jboss.ddoyle.drools;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class IncrementalUpdateTest {

	@Test
	public void test() {
		KieServices kieServices = KieServices.Factory.get();
		
		// Create the first KieModule.
		KieFileSystem firstKfs = kieServices.newKieFileSystem();
		firstKfs.write("src/main/resources/rules.drl", getFirstRule());
		ReleaseId firstReleaseId = kieServices.newReleaseId("org.jboss.ddoyle", "rules-test", "1.0.0");
		firstKfs.generateAndWritePomXML(firstReleaseId);
		KieBuilder firstKieBuilder = kieServices.newKieBuilder(firstKfs);
		// kieModule is automatically deployed to KieRepository if successfully built.
		firstKieBuilder.buildAll();
		if (firstKieBuilder.getResults().hasMessages(Level.ERROR)) {
			throw new RuntimeException("Build Errors:\n" + firstKieBuilder.getResults().toString());
		}

		// Create the second KieModule.
		KieFileSystem secondKfs = kieServices.newKieFileSystem();
		secondKfs.write("src/main/resources/rules.drl", getSecondRule());
		ReleaseId secondReleaseId = kieServices.newReleaseId("org.jboss.ddoyle", "rules-test", "2.0.0");
		secondKfs.generateAndWritePomXML(secondReleaseId);
		KieBuilder secondKieBuilder = kieServices.newKieBuilder(secondKfs);

		// kieModule is automatically deployed to KieRepository if successfully built.
		secondKieBuilder.buildAll();
		if (secondKieBuilder.getResults().hasMessages(Level.ERROR)) {
			throw new RuntimeException("Build Errors:\n" + secondKieBuilder.getResults().toString());
		}

		// Create the KieContainer with the firstReleaseId and create a new session. 
		KieContainer kieContainer = kieServices.newKieContainer(firstReleaseId);
		KieSession kieSession = kieContainer.newKieSession();
		try {
			// Update the container and session to the second version.
			kieContainer.updateToVersion(secondReleaseId);
		} finally {
			kieSession.dispose();
		}
	}
	
	public String getFirstRule() {
		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder.append("package org.jboss.ddoyle.drools.rules;\n\n");
		ruleBuilder.append("import org.jboss.ddoyle.drools.model.Event;\n\n");
		ruleBuilder.append("rule \"test-rule\"\n");
		ruleBuilder.append("when\n");
		ruleBuilder.append("Event()\n");
		ruleBuilder.append("then\n");
		ruleBuilder.append("System.out.println(\"First rule fired!\");\n");
		ruleBuilder.append("end");
		return ruleBuilder.toString();
	}

	public String getSecondRule() {

		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder.append("package org.jboss.ddoyle.drools.rules;\n\n");
		ruleBuilder.append("import org.jboss.ddoyle.drools.model.Event;\n\n");
		// Declare our interface as a PropertyReactive event.
		ruleBuilder.append("declare Event\n");
		ruleBuilder.append("@role( event )\n");
		ruleBuilder.append("@propertyReactive\n");
		ruleBuilder.append("end\n\n");
		ruleBuilder.append("rule \"test-rule\"\n");
		ruleBuilder.append("when\n");
		ruleBuilder.append("Event()\n");
		ruleBuilder.append("then\n");
		ruleBuilder.append("System.out.println(\"Second rule fired!\");\n");
		ruleBuilder.append("end");
		return ruleBuilder.toString();

	}

}
