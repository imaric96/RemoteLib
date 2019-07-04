package storage;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Implementation of {@link StorageDefinition}. This implementation use dropbox as storage.
 */
public class StorageDefinitionRemoteImplementation implements StorageDefinition {

    String path;
    Properties properties;
    StorageDefinitionRemoteImplementation definition;

    public StorageDefinitionRemoteImplementation(String path){
        this.path = path;
        this.properties = new Properties();
        try{
            InputStream input = new FileInputStream(this.path);
            this.properties.load(input);
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            DbxClientV2 client = new DbxClientV2(config, properties.getProperty("token"));
        }catch (Exception e){
            System.out.println("Greska: " + e.getMessage());
        }
    }

    @Override
    public void createStorage(String s, String s1) {

    }

    /**
     * Create storage on given path(on remote location).
     *
     * @param path path of the storage
     */
    public void deleteStorage(String path) {
        //add implementation
    }
}
