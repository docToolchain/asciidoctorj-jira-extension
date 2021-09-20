package org.doctoolchain.asciidoctorj.jira

import org.asciidoctor.Asciidoctor
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry

class JiraExtensionRegistry implements ExtensionRegistry {

    @Override
    void register(Asciidoctor asciidoctor) {
        asciidoctor.javaExtensionRegistry().blockMacro(JiraIssuesBlockMacro.class)
        asciidoctor.javaExtensionRegistry().inlineMacro(JiraIssueInlineMacro.class)
    }
}
