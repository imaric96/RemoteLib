package file;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of {@link FileBasicOperation} interface. This implementation use dropbox as storage.
 */
public class FileBasicOperationRemoteImplementation implements FileBasicOperation {

    String storagePath;
    String blacklist[];
    Properties properties;
    DbxClientV2 client;

    public FileBasicOperationRemoteImplementation(String path){
        this.storagePath = path;
        this.properties = new Properties();
        try (InputStream in = new FileInputStream(this.storagePath)) {
            this.properties.load(in);
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            client = new DbxClientV2(config, properties.getProperty("token"));
            this.blacklist = properties.getProperty("blacklist").trim().split(";");
            createStorage();
        }catch (Exception e){
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    public void createFolder(String Name, String destinationPath) {
        try{
            if(destinationPath == null || destinationPath.isEmpty() || destinationPath.trim().equals(""))
                destinationPath = "/" + properties.getProperty("storagePath") + "/" + Name;
            else
                destinationPath = "/" + properties.getProperty("storagePath") + "/" + destinationPath + "/" + Name;
            client.files().createFolderV2(destinationPath);
        }catch (DbxException e){
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    public void createFile(String Name, String destinationPath, boolean metaData) {
        try{
            String ext = ".txt";
            File tempFile = File.createTempFile("RAF", ext);
            File fileWithNewName = new File(tempFile.getParent(), Name+=ext);
            tempFile.renameTo(fileWithNewName);
            uploadFile(fileWithNewName, destinationPath, metaData);

        }catch (Exception e){
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * Upload file to dropbox.
     *
     * @param file file which we want to upload
     * @param destinationPath path of the file on the storage
     */
    public void uploadFile(File file, String destinationPath, boolean metaData) {
        String pathTemp = destinationPath;
        createStorage();
        int brojac = 0;
        InputStream in = null;
        try{
            ArrayList<String> nameAndExtension = separateNameExtenstion(file);

            if(isBlacklisted(nameAndExtension.get(1)) == true){
                throw new Exception("Unallowed extension!");
            }else{
                in = new  FileInputStream(file);

                if(destinationPath == null || destinationPath.isEmpty() || destinationPath.trim().equals(""))
                    destinationPath = "/" + properties.getProperty("storagePath");
                else
                    destinationPath = "/" + properties.getProperty("storagePath") + "/" + destinationPath;

                while(true){
                    if(brojac == 0)
                        pathTemp = destinationPath + "/" + nameAndExtension.get(0) + "." + nameAndExtension.get(1);

                    else
                        pathTemp = destinationPath + "/" + nameAndExtension.get(0) + "_" + brojac + "." + nameAndExtension.get(1);

                    if(isExist(pathTemp)){
                        brojac++;
                        pathTemp = destinationPath;
                    }else{
                        FileMetadata metadata = client.files().uploadBuilder(pathTemp).uploadAndFinish(in);
                        if(metaData == true) {
                            if(brojac == 0)
                                pathTemp = destinationPath + "/" + nameAndExtension.get(0) + ".json";

                            else
                                pathTemp = destinationPath + "/" + nameAndExtension.get(0) + "_" + brojac + ".json";
                            in = new FileInputStream(createMedaData(file, pathTemp));
                            metadata = client.files().uploadBuilder(pathTemp).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);
                        }
                        break;
                    }
                }
            }
        }catch (Exception e){
            System.err.println("Greska: " + e.getMessage());
        }
    }

    public void uploadFile(String filePath, String destinationPath, boolean metaData) {
        File file = new File(filePath);
        uploadFile(file, destinationPath, metaData);
    }

    public void uploadFiles(ArrayList<File> files, String destinationPath, boolean metaData) {
        try {
            for(File file : files)
                uploadFile(file, destinationPath, metaData);
        }catch (Exception e){
            System.out.println("Greska: " + e.getMessage());
        }
    }

    public void uploadFolder(File file, String s, boolean b) {

    }

    public void uploadFolder(String s, String s1, boolean b) {

    }

    public void downlaodFile(String s, String s1) {

    }

    @Override
    public void downlaodFiles(ArrayList<String> arrayList, String s) {

    }

    @Override
    public void downlaodFiles(String s, String s1) {

    }

    @Override
    public ArrayList<String> getFilesByPath(String path){

        if(path == null || path.isEmpty() || path.trim() == "" || path.trim() == "root")
            path = "/" + properties.getProperty("storagePath");
        else
            path = "/" + properties.getProperty("storagePath") + "/" + path;

        ArrayList<String> list = new ArrayList<String>();

        try {
            ListFolderResult result = client.files().listFolder(path);
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                   // File tempFile = new File(metadata.getPathDisplay());
                    list.add(metadata.getPathDisplay());
                }
                if (!result.getHasMore()) {
                    return list;
                }
            }
        } catch (Exception e) {
            return list;
        }
    }

    @Override
    public ArrayList<String> getFileNamesByPath(String s) {
        return null;
    }

    @Override
    public ArrayList<String> getFilesByName(String s) {
        return null;
    }

    public ArrayList<String> getFoldersByPath(String path) {
        if(path == null || path.isEmpty() || path.trim() == "" || path.trim() == "root")
            path = "/" + properties.getProperty("storagePath");
        else
            path = "/" + properties.getProperty("storagePath") + "/" + path;

        ArrayList<String> names = new ArrayList<>();
        ListFolderResult result = null;
        try {
            result = client.files().listFolder(path);
        } catch (DbxException e) {
            e.printStackTrace();
        }
        List<Metadata> list = null;
        for (Metadata metadata: result.getEntries())
            names.add(metadata.getName());
        return names;
    }


    public ArrayList<String> getBlacklistedExtensions() {
        ArrayList<String> extensions = new ArrayList<String>();
        for (int i=0; i < blacklist.length; i++){
            extensions.add(blacklist[i]);
        }
        return extensions;
    }

    public boolean isExist(String filePath) {
        boolean exist = false;
        try {
            client.files().getMetadata(filePath);
            exist = true;
        }catch (GetMetadataErrorException e){
            exist = false;
        }catch (DbxException e){
            exist = false;
        }
        return exist;
    }

    public void deleteFile(String destinationPath) {
        try {
            if(destinationPath == null || destinationPath.isEmpty() || destinationPath.trim().equals(""))
                destinationPath = "/" + properties.getProperty("storagePath");
            else
                destinationPath = "/" + properties.getProperty("storagePath") + "/" + destinationPath;
            client.files().deleteV2(destinationPath);
        }catch (DbxException e){
            e.printStackTrace();
        }
    }


    public void deleteFiles(ArrayList<String> files) {

        try {
            for(String file : files)
                deleteFile(file);
        }catch (Exception e){
            System.out.println("Greska: " + e.getMessage());
        }
    }

    @Override
    public void deleteFiles(String s) {

    }

    private void createStorage(){
        try {
            client.files().getMetadata("/" + properties.getProperty("storagePath"));
        }catch (GetMetadataErrorException e){
            if (e.errorValue.isPath())
            {
                LookupError le = e.errorValue.getPathValue();
                if (le.isNotFound())
                {
                    System.out.println("STORAGE doesn't exist on Dropbox.");
                    try
                    {
                        client.files().createFolderV2("/" + properties.getProperty("storagePath"));
                        System.out.println("STORAGE CREATED.");
                    }
                    catch (CreateFolderErrorException e1)
                    {
                        e1.printStackTrace();
                    }
                    catch (DbxException e1)
                    {
                        e1.printStackTrace();
                    }
                }
                else{
                    System.out.println("STORAGE exist on Dropbox.");
                }
            }
        } catch (DbxException e) {
            System.out.println("Greska: " + e.getClass().getName() + e.getMessage());
        }
    }
    private File createMedaData(File file, String filePathDestination){
        File tempFile = null;
        try {
            tempFile = File.createTempFile("RAF", ".json");
            ArrayList<String> nameAndExtension = separateNameExtenstion(file);
            double kilobytes = (file.length() / 1024);
            JSONObject obj = new JSONObject();
            obj.put("fileName", nameAndExtension.get(0));
            obj.put("fileSize", kilobytes + " KB");
            obj.put("fileLocation", filePathDestination);
            obj.put("fileType", nameAndExtension.get(1));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String objFormated = gson.toJson(obj);
            tempFile = File.createTempFile("RAF", ".json");
            FileWriter fw = new FileWriter(tempFile);
            fw.write(objFormated);
            fw.flush();
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
          return tempFile;
    }
    private ArrayList<String> separateNameExtenstion(File file){

        ArrayList<String> lista = new ArrayList<>();
        try {
            String[] fileNameSplits = file.getName().split("\\.");
            String ekstenzija = fileNameSplits[fileNameSplits.length - 1];
            String nazivFajla = "";
            for (int i = 0; i < fileNameSplits.length - 1; i++) {
                if (i > 0)
                    nazivFajla += "." + fileNameSplits[i];
                else
                    nazivFajla += fileNameSplits[i];
                lista.add(nazivFajla);
                lista.add(ekstenzija);
                return lista;
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return lista;
    }
    private boolean isBlacklisted(String extension){

        boolean blacklisted = false;

        for (int i=0; i < blacklist.length; i++){
            if(extension.equals(blacklist[i])) {
                blacklisted = true;
                break;
            }
            else
                blacklisted = false;
        }
        return blacklisted;
    }
}
