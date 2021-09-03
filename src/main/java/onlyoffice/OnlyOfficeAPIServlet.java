/**
 *
 * (c) Copyright Ascensio System SIA 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package onlyoffice;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import onlyoffice.managers.configuration.ConfigurationManager;
import onlyoffice.managers.document.DocumentManager;
import onlyoffice.managers.jwt.JwtManager;
import onlyoffice.managers.url.UrlManager;
import onlyoffice.utils.attachment.AttachmentUtil;
import onlyoffice.utils.parsing.ParsingUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OnlyOfficeAPIServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LogManager.getLogger("onlyoffice.OnlyOfficeAPIServlet");

    private final JwtManager jwtManager;
    private final DocumentManager documentManager;

    private final AttachmentUtil attachmentUtil;
    private final ParsingUtil parsingUtil;
    private final UrlManager urlManager;
    private final ConfigurationManager configurationManager;

    @Inject
    public OnlyOfficeAPIServlet(JwtManager jwtManager, DocumentManager documentManager,
                                AttachmentUtil attachmentUtil, ParsingUtil parsingUtil, UrlManager urlManager,
                                ConfigurationManager configurationManager) {
        this.jwtManager = jwtManager;
        this.documentManager = documentManager;
        this.attachmentUtil = attachmentUtil;
        this.parsingUtil = parsingUtil;
        this.urlManager = urlManager;
        this.configurationManager = configurationManager;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type");
        if (type != null) {
            switch (type.toLowerCase())
            {
                case "create":
                    create(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
    }

    private void create (HttpServletRequest request, HttpServletResponse response) throws IOException {
        ConfluenceUser user = AuthenticatedUserThreadLocal.get();

        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        InputStream requestStream = request.getInputStream();
        String body = parsingUtil.getBody(requestStream);
        HttpURLConnection connection = null;

        try {
            JSONObject bodyJson = new JSONObject(body);
            String downloadUrl = bodyJson.getString("url");
            String title = bodyJson.getString("title");
            String ext = bodyJson.getString("ext");
            String pageIdString = bodyJson.getString("pageId");

            if (downloadUrl.isEmpty() || title.isEmpty() || ext.isEmpty() || pageIdString.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Long pageId = Long.parseLong(pageIdString);

            if (!attachmentUtil.checkAccessCreate(user, pageId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();

            Integer timeout = Integer.parseInt(configurationManager.getProperty("timeout")) * 1000;
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            int size = connection.getContentLength();
            InputStream stream = connection.getInputStream();

            String fileName = documentManager.getCorrectName(title, ext, pageId);
            String mimeType = documentManager.getMimeType(fileName);

            attachmentUtil.createNewAttachment(fileName, mimeType, stream, size, pageId, user);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }  finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
