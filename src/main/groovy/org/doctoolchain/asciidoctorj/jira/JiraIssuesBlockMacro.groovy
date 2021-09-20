package org.doctoolchain.asciidoctorj.jira


import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name
import org.asciidoctor.log.LogRecord
import org.asciidoctor.log.Severity
import wslite.http.auth.HTTPBasicAuthorization
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.Response

@Name("jiraIssues")
class JiraIssuesBlockMacro extends BlockMacroProcessor {

    static final String ATTR_JQL = "jql"
    static final String DEFAULT_JQL = "resolution='Unresolved' ORDER BY priority DESC, duedate ASC"
    static final String ATTR_JIRA_HOST = "jira-host"
    static final String ATTR_JIRA_USERNAME = "jira-username"
    static final String ATTR_JIRA_APITOKEN = "jira-apitoken"

    @Override
    Object process(StructuralNode parent, String target, Map<String, Object> attributes) {
        String projectKey = target
        String jql = attributes.getOrDefault(ATTR_JQL, DEFAULT_JQL)
        String jiraHost = parent.getAttribute(ATTR_JIRA_HOST)
        if (! jiraHost) {
            log(new LogRecord(Severity.ERROR, "Attribute :jira-host: is not defined"))
            return
        }
        String jiraUsername = parent.getAttribute(ATTR_JIRA_USERNAME)
        String jiraApiToken = parent.getAttribute(ATTR_JIRA_APITOKEN)

        List<String> content = []
        content << "[options=\"header\",cols=\"2,1,1,2,6\"]"
        content << "|===="
        content << "|ID | Priority | Created | Assignee | Summary"
        def issues = searchIssues(jiraHost, jiraUsername, jiraApiToken, projectKey, jql)
        issues.each { issue ->
            content << "| <<${issue.key}>> ".toString()
            content << "|${issue.fields.priority.name}".toString()
            content << "|${issue.fields.created}".toString()
            content << "|${issue.fields.assignee? issue.fields.assignee.displayname : 'not assigned'}".toString()
            content << "| ${jiraHost}/browse/${issue.key}[${issue.fields.summary}]".toString()
        }
        content << "|===="


        parseContent(parent, content)

        return null
    }


    protected def searchIssues(String jiraHost, String username, String apiToken, String projectKey, String jql) {
        RESTClient restClient = createRestClient(jiraHost)
        restClient.authorization = new HTTPBasicAuthorization(username, apiToken)
        Response response = restClient.get(
                path: "/search",
                accept: ContentType.JSON,
                query: [
                    jql : "project='${projectKey}' AND ${jql}",
                    fields: 'created,resolutiondate,priority,summary,timeoriginalestimate,assignee'
                ])
        if (response.statusCode == 200) {
            def json = response.json
            return json.issues
        }
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    protected RESTClient createRestClient(String jiraHost) {
        return new RESTClient("${jiraHost}/rest/api/2")
    }

}
