package org.doctoolchain.asciidoctorj.jira.issues


import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.assertj.core.api.Assertions
import spock.lang.Shared
import spock.lang.Specification

class JiraIssueInlineMacroSpec extends Specification {

    @Shared
    Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def "jira:DOC-1234[] is rendered as link to jira issue DOC-1234"() {
        Options options = OptionsBuilder
                .options()
                .backend("html5")
                .safe(SafeMode.UNSAFE)
                .inPlace(true)
                .attributes([
                        "jira-host"    : "http://jira-host",
                ])
                .get()

        when:
        def convertedContent = asciidoctor.convert("jira:DOC-1234[]", options)

        then:
        Assertions.assertThat(convertedContent).isEqualToIgnoringWhitespace('''
        <div class="paragraph">
            <p><a href="http://jira-host/browse/DOC-1234" id="DOC-1234">DOC-1234</a></p>
        </div>''')

    }

}
