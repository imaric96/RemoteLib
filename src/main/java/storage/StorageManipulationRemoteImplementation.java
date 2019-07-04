package storage;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import file.FileBasicOperation;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Implementation of {@link StorageManipulation}. This implementation use dropbox as storage.
 */
public class StorageManipulationRemoteImplementation implements StorageManipulation {

    String path;
    Properties properties;

    public StorageManipulationRemoteImplementation(String path){
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
    /**
     * Change storage location.
     *
     * @param oldPath old storage path
     * @param newPath new storage path
     */
    public void moveStorage(String oldPath, String newPath) {
        //add implementation
    }


}
