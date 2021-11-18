package onlyoffice.managers.configuration;

import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public interface ConfigurationManager extends Serializable {
    public Properties getProperties() throws IOException;
    public String getProperty(String propertyName);
    public boolean selectDemo(Boolean demo);
    public Boolean demoEnabled();
    public Boolean demoAvailable(Boolean forActivate);
    public Boolean demoActive();
    public String getDemo(String key);
    public Boolean getBooleanPluginSetting(String key, Boolean defaultValue);
    public CloseableHttpClient getHttpClient() throws Exception;
}
