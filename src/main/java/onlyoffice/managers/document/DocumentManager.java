package onlyoffice.managers.document;

import com.atlassian.confluence.user.ConfluenceUser;
import onlyoffice.model.DocumentType;
import onlyoffice.model.Type;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface DocumentManager extends Serializable {
    public long getMaxFileSize();
    public String getKeyOfFile(Long attachmentId, boolean embedded);
    public String createHash(String str);
    public String readHash(String base64);
    public String getCorrectName(String fileName, String fileExt, Long pageID);
    public Long createDemo(String fileName, String fileExt, Long pageID, ConfluenceUser user) throws IOException;
    public DocumentType getDocType(Long attachmentId);
    public DocumentType getDocType(String ext);
    public Type getEditorType (String userAgent);
    public String getMimeType(String name);
    public boolean isEditable(String fileExtension);
    public boolean isFillForm(String fileExtension);
    public boolean isViewable(String fileExtension);
    public List<String> getInsertImageTypes();
    public List<String> getCompareFileTypes();
    public List<String> getMailMergeTypes();
}
