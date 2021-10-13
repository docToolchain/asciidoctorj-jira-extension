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
        String jiraHost = parent.getAttribute(ATTR_JIRA_HOST, "", true)
        if (!jiraHost) {
            log(new LogRecord(Severity.ERROR, "Attribute :jira-host: is not defined"))
            return
        }
        String jiraUsername = parent.getAttribute(ATTR_JIRA_USERNAME, "", true)
        String jiraApiToken = parent.getAttribute(ATTR_JIRA_APITOKEN, "", true)

        def content = []

        content << "[options=\"header\",cols=\"2,1,1,2,6\"]"
        content << "|===="
        content << "|ID | Priority | Created | Assignee | Summary"
        def issues = searchIssues(jiraHost, jiraUsername, jiraApiToken, projectKey, jql)
        issues.each { issue ->
            String issueTypeName = issue.fields.issuetype.name
            downloadFile(issue.fields.issuetype.iconUrl as String, "${issueTypeName}.svg".toString(), getJiraAssetsDirectory(parent))

            content << "a| image:jira_assets/${issueTypeName}.svg[] jira:${issue.key}[] ".toString()
            content << "|${issue.fields.priority.name}".toString()
            content << "|${issue.fields.created}".toString()
            content << "|${issue.fields.assignee ? issue.fields.assignee.displayName : 'not assigned'}".toString()
            content << "| ${jiraHost}/browse/${issue.key}[${issue.fields.summary}]".toString()
        }
        content << "|===="

        parseContent(parent, content)

        return null
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    protected void downloadFile(String url, String fileName, File imageDirectory) {
        File targetFile = new File(imageDirectory, fileName)
        if (! targetFile.exists()) {
            new URL(url).openConnection().with { conn ->
                targetFile.withOutputStream { out ->
                    conn.inputStream.with { inp ->
                        out << inp
                        inp.close()
                    }
                }
            }
        }

    }


    protected def searchIssues(String jiraBaseUrl, String username, String apiToken, String projectKey, String jql) {
        RESTClient restClient = createRestClient(jiraBaseUrl)
        restClient.defaultCharset = "UTF-8"
        restClient.authorization = new HTTPBasicAuthorization(username, apiToken)
        Response response = restClient.get(
                path: "/search",
                accept: ContentType.JSON,
                query: [
                        jql   : "project='${projectKey}' AND ${jql}",
                        fields: 'created,resolutiondate,priority,summary,timeoriginalestimate,assignee,issuetype'
                ])
        if (response.statusCode == 200) {
            def json = response.json
            return json.issues as List
        }
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    protected RESTClient createRestClient(String jiraBaseUrl) {
        def restClient = new RESTClient("${jiraBaseUrl}/rest/api/2")
        return  restClient
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    protected File getBuildDir(final StructuralNode structuralNode) {
        if (structuralNode == null) {
            return null;
        }
        final Map<Object, Object> globalOptions = structuralNode.getDocument().getOptions()

        String toDir = (String) globalOptions.get("to_dir")
        String destDir = (String) globalOptions.get("destination_dir")
        String buildDir = toDir ?: destDir ?: "."
        return new File(buildDir)
    }

    private File getJiraAssetsDirectory(StructuralNode structuralNode) {
        final String imagesDirName = structuralNode.getAttribute("imagesdir", "images", true)
        File jira_assets_dir
        if (new File(imagesDirName).isAbsolute()) {
            jira_assets_dir = new File(imagesDirName, "jira_assets")
            jira_assets_dir.mkdirs()
        } else {
            final File buildDir = getBuildDir(structuralNode);
            jira_assets_dir = new File(new File(buildDir, imagesDirName), "jira_assets")
            jira_assets_dir.mkdirs()
        }
        return jira_assets_dir
    }

}
