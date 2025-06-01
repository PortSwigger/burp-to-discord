import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.StatusCodeClass
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.scanner.audit.AuditIssueHandler
import burp.api.montoya.scanner.audit.issues.AuditIssue
import org.json.JSONArray
import org.json.JSONObject

class BurpToDiscord : BurpExtension {
    companion object {
        var unloaded = false
        val hookUrl = "https://discord.com/api/webhooks/1376245237290369034/iVTGvQTNdgqxGOt_e6jvwocI7YpyUWaEyKEpt5lnt6BWr_KxYseWRb86izczmObbqU1_"
        lateinit var montoyaApi: MontoyaApi
    }

    override fun initialize(api: MontoyaApi?) {
        if (api == null) {
            return
        }
        montoyaApi = api

        val name = "burp-to-discord"
        api.extension().setName(name)
        api.logging().logToOutput("Loaded $name")

        api.scanner().registerAuditIssueHandler(MyAuditIssueHandler())



        //Register unloading handler
        api.extension().registerUnloadingHandler {
            unloaded = true
            api.logging().logToOutput("Unloading Extension...")
        }
    }

    private class MyAuditIssueHandler : AuditIssueHandler {
        override fun handleNewAuditIssue(issue: AuditIssue?) {
            montoyaApi.logging().logToOutput("New Issue Reported!!!")

            val issueName = issue?.name()
            val url = issue?.baseUrl().toString().replace("/", "\\/")
            val details = issue?.detail().toString().replace("/", "\\/")
            val requestResponses = issue?.requestResponses()
            var attachmentsList = JSONArray()

            if (requestResponses != null) {
                for (requestResponse in requestResponses) {
                    var req = requestResponse.request().toString()

                    var resp = ""

                    if (requestResponse.hasResponse()) {
                        resp = requestResponse.response().toString()
                    }

                    if (req.length > 2000) {
                        req = req.substring(0, 2000)
                    }
                    if (resp.length > 2000) {
                        resp = resp.substring(0, 2000)
                    }

                    var attachment = JSONObject()
                    attachment.put("description",
                        """Request #${requestResponses.indexOf(requestResponse)+1}:
                        ```
$req
                        ```
                        Response #${requestResponses.indexOf(requestResponse)+1}:
                        ```
$resp
                        ```""".trimIndent())
                    attachmentsList.put(attachment)
                }
            }

            val content = """
                > <@177495962825129984>, new research issue!
                > **Title**: $issueName
                > **URL**: $url
                > **Details**: $details
            """.trimIndent()

            var jsonContent = JSONObject()
            jsonContent.put("content", content)

            val dicordRequest = HttpRequest.httpRequestFromUrl(hookUrl).
                withMethod("POST").
                withHeader("Content-Type", "application/json").
                withBody(jsonContent.toString())

            val requestResponse = montoyaApi.http().sendRequest(dicordRequest)

            if (!requestResponse.response().isStatusCodeClass(StatusCodeClass.CLASS_2XX_SUCCESS)) {
                montoyaApi.logging().logToError("Error when sending issue to discord: ${requestResponse.response().statusCode()} | ${requestResponse.response().bodyToString()}")
            }

            for (attachment in attachmentsList) {
                val attachmentJsonContent = JSONObject()
                val smallArray = JSONArray()
                smallArray.put(attachment)

                attachmentJsonContent.put("embeds", smallArray)

                val discordAttachmentRequest = HttpRequest.httpRequestFromUrl(hookUrl).
                        withMethod("POST").
                        withHeader("Content-Type","application/json").
                        withBody(attachmentJsonContent.toString())
                val requestResponseAttachments = montoyaApi.http().sendRequest(discordAttachmentRequest)

                if (!requestResponseAttachments.response().isStatusCodeClass(StatusCodeClass.CLASS_2XX_SUCCESS)) {
                    montoyaApi.logging().logToError("Error when sending attachments to discord: ${requestResponseAttachments.response().statusCode()} | ${requestResponseAttachments.response().bodyToString()}")
                }
            }

        }
    }
}