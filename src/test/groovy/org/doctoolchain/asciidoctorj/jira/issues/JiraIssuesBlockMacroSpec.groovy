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
import spock.lang.TempDir

import static com.github.tomakehurst.wiremock.client.WireMock.*

class JiraIssuesBlockMacroSpec extends Specification {

    @Shared
    WireMockServer wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().stubRequestLoggingDisabled(false).notifier(new ConsoleNotifier(true)).withRootDirectory("src/test/resources"))

    @Shared
    Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    @TempDir
    File tempDir

    def "jql is not set, standard jql should used and not empty result is returned"() {
        wireMock.start()
        Options options = OptionsBuilder
                .options()
                .backend("html5")
                .safe(SafeMode.UNSAFE)
                .inPlace(true)
                .destinationDir(tempDir)
                .attributes([
                        "jira-host"    : wireMock.baseUrl(),
                        "jira-username": "username",
                        "jira-apitoken": "apitoken"
                ])
                .get()

        String responseBody = wireMock.getOptions().filesRoot().child("__files").getTextFileNamed("jiraIssuesBlockMacroProcessorSpec/issuesFoundForJQL.json").readContentsAsString()
        responseBody = responseBody.replaceAll("https://uniqueck\\.atlassian\\.net", wireMock.baseUrl())

        wireMock.stubFor(
                get(
                        urlPathEqualTo("/rest/api/2/search")
                )
                        .withQueryParam("jql", equalTo("project='DOC' AND resolution='Unresolved' ORDER BY priority DESC, duedate ASC"))
                        .withQueryParam("fields", equalTo("created,resolutiondate,priority,summary,timeoriginalestimate,assignee,issuetype"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(responseBody)
                        )
        )

        wireMock.stubFor(
                get(
                        urlPathEqualTo("/secure/viewavatar")
                )
                        .withQueryParams([
                            size: equalTo("medium"),
                            avatarId: equalTo("10303"),
                            avatarType: equalTo("issuetype")
                        ])
                        .willReturn(aResponse().withBodyFile("jiraIssuesBlockMacroProcessorSpec/Bug.svg"))
        )

        wireMock.stubFor(
                get(
                        urlPathEqualTo("/secure/viewavatar")
                )
                        .withQueryParams([
                                size: equalTo("medium"),
                                avatarId: equalTo("10318"),
                                avatarType: equalTo("issuetype")
                        ])
                        .willReturn(aResponse().withBodyFile("jiraIssuesBlockMacroProcessorSpec/Task.svg"))
        )

        wireMock.stubFor(
                get(
                        urlPathEqualTo("/secure/viewavatar")
                )
                        .withQueryParams([
                                size: equalTo("medium"),
                                avatarId: equalTo("10315"),
                                avatarType: equalTo("issuetype")
                        ])
                        .willReturn(aResponse().withBodyFile("jiraIssuesBlockMacroProcessorSpec/Story.svg"))
        )

        when:
        def convertedContent = asciidoctor.convert("jiraIssues::DOC[]", options)

        then:
        Assertions.assertThat(convertedContent).isEqualToIgnoringWhitespace("""<table class="tableblock frame-all grid-all stretch">
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
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><div class="content"><div class="paragraph">
<p><span class="image"><img src="jira_assets/Bug.svg" alt="Bug"></span> <a href="${wireMock.baseUrl()}/browse/DOC-4" id="DOC-4">DOC-4</a></p>
</div></div></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Medium</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">2021-10-09T10:02:04.629+0200</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">not assigned</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="${wireMock.baseUrl()}/browse/DOC-4">AnotherBug</a></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><div class="content"><div class="paragraph">
<p><span class="image"><img src="jira_assets/Task.svg" alt="Task"></span> <a href="${wireMock.baseUrl()}/browse/DOC-3" id="DOC-3">DOC-3</a></p>
</div></div></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Medium</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">2021-10-09T10:01:35.706+0200</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">not assigned</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="${wireMock.baseUrl()}/browse/DOC-3">Task123</a></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><div class="content"><div class="paragraph">
<p><span class="image"><img src="jira_assets/Bug.svg" alt="Bug"></span> <a href="${wireMock.baseUrl()}/browse/DOC-2" id="DOC-2">DOC-2</a></p>
</div></div></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Medium</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">2021-10-09T09:59:31.902+0200</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">not assigned</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="${wireMock.baseUrl()}/browse/DOC-2">Bug123</a></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><div class="content"><div class="paragraph">
<p><span class="image"><img src="jira_assets/Story.svg" alt="Story"></span> <a href="${wireMock.baseUrl()}/browse/DOC-1" id="DOC-1">DOC-1</a></p>
</div></div></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Medium</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">2021-08-08T12:13:19.203+0200</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Constantin Krueger</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="${wireMock.baseUrl()}/browse/DOC-1">Test123</a></p></td>
</tr>
</tbody>
</table>""")

        cleanup:
        wireMock.stop()

    }

    def "jql is not set, standard jql should used and empty result is returned"() {
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
                        .withQueryParam("fields", equalTo("created,resolutiondate,priority,summary,timeoriginalestimate,assignee,issuetype"))
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
