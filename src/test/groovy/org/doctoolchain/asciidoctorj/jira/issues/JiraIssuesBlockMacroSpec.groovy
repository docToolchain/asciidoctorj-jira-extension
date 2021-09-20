package org.doctoolchain.asciidoctorj.jira.issues

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.assertj.core.api.Assertions
import spock.lang.Shared
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

class JiraIssuesBlockMacroSpec extends Specification {

    @Shared
    WireMockServer wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().stubRequestLoggingDisabled(false).notifier(new ConsoleNotifier(true)).withRootDirectory("src/test/resources"))

    @Shared
    Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def "jql is not set, standard jql should used"() {
        wireMock.start()
        Options options = OptionsBuilder
                .options()
                .backend("html5")
                .safe(SafeMode.UNSAFE)
                .inPlace(true)
                .attributes([
                        "jira-host"    : wireMock.baseUrl(),
                        "jira-username": "username",
                        "jira-apitoken": "apitoken"
                ])
                .get()

        wireMock.stubFor(
            get(
                urlPathEqualTo("/rest/api/2/search")
            )
            .withQueryParam("jql", equalTo("project='DOC' AND resolution='Unresolved' ORDER BY priority DESC, duedate ASC"))
            .withQueryParam("fields", equalTo("created,resolutiondate,priority,summary,timeoriginalestimate,assignee"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("jiraIssuesBlockMacroProcessorSpec/noIssuesFoundForJQL.json")
            )
        )

        when:
        def convertedContent = asciidoctor.convert("jiraIssues::DOC[]", options)

        then:
        Assertions.assertThat(convertedContent).isEqualToIgnoringWhitespace('''<table class="tableblock frame-all grid-all stretch">
        <colgroup>
        <col style="width: 16.6666%;">
        <col style="width: 8.3333%;">
        <col style="width: 8.3333%;">
        <col style="width: 16.6666%;">
        <col style="width: 50.0002%;">
        </colgroup>
        <thead>
        <tr>
        <th class="tableblock halign-left valign-top">ID</th>
        <th class="tableblock halign-left valign-top">Priority</th>
        <th class="tableblock halign-left valign-top">Created</th>
        <th class="tableblock halign-left valign-top">Assignee</th>
        <th class="tableblock halign-left valign-top">Summary</th>
        </tr>
        </thead>
        </table>''')

        cleanup:
        wireMock.stop()
    }

}
