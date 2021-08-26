package org.doctoolchain.asciidoctorj.jira

import org.asciidoctor.ast.ContentNode
import org.asciidoctor.extension.InlineMacroProcessor
import org.asciidoctor.extension.Name

@Name("jira")
class JIRALinkMacro extends InlineMacroProcessor {

    @Override
    Object process(ContentNode parent, String target, Map<String, Object> attributes) {

        def jiraHost = attributes.get("jira-host", parent.getDocument().getAttribute("jira-host"))

        String href = new StringBuilder()
                .append("https://)")
                .append(jiraHost)
                .append("/browse/")
                .append(target)

        Map<String, Object> options = [
                type: ":link",
                target: href,
                id: target
        ]
        return createPhraseNode(parent, "anchor", target, options)
    }
}
