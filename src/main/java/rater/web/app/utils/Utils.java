package rater.web.app.utils;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

public class Utils {

    /**
     * Redirect to a specific uri
     */
    public static String redirectTo(String url) {
        return "redirect:" + url;
    }

    /**
     * Zip directoru
     * @param dir directory to zip
     * @param destination destination of the zipped file
     * @return zipped file
     */
    public static File zipDirectory(File dir, File destination){
        for (File f: Objects.requireNonNull(dir.listFiles()))
            if (f.getName().equals(destination.getName()))
                Utils.deleteFile(f);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "zip -r " + destination.getPath() + " " + dir.getPath());
            Process process = processBuilder.start();
            int exitVal = process.waitFor();
            if (exitVal != 0)
                throw new RuntimeException("Failure zipping: " + dir.getPath());
            if (!destination.exists())
                throw new RuntimeException("Failure zipping: " + dir.getPath());

        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
            System.err.println("Failure zipping: " + dir.getPath());
        }
        return destination;
    }

    /**
     * Unzip directory
     * @param zippedFile zipped file
     * @param destination destination of the unzipped directoru
     * @return unzipped directory
     */
    public static File unzipDirectory(File zippedFile, File destination) throws IOException, InterruptedException {
        String unzippedFileName = zippedFile.getName().replace(".zip", "");
        //check before unzipping
        for (File f: Objects.requireNonNull(destination.listFiles()))
            if (f.getName().equals(unzippedFileName))
                Utils.deleteDirectory(f);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "unzip " + zippedFile.getAbsolutePath() + " -d " +
                destination.getAbsolutePath() + "/" + unzippedFileName);
        Process process = processBuilder.start();
        int exitVal = process.waitFor();
        if (exitVal != 0)
            throw new RuntimeException("Error durante la ejecucion del comando unzip: " + zippedFile.getName());

        File unzippedDirectory = new File(destination.getPath() + "/" + unzippedFileName);
        File unzippedFile = null;
        File[] files = unzippedDirectory.listFiles();
        for (File f : Objects.requireNonNull(files)) {
            unzippedFile = f;
            break;
        }
        return unzippedFile;
    }

    /**
     * Parse file into a JSONObject object
     * @param file file
     * @return JSONOBject object
     */
    public static JSONObject fileToJSONObject(File file) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try (Reader reader = new FileReader(file.getPath())) {
            jsonObject = (JSONObject) parser.parse(reader);
            System.out.println(jsonObject);
        } catch (IOException | ParseException e) {
            System.err.println(e.getMessage());
        }
        return jsonObject;
    }

    /**
     * delete directory
     * @param f directory
     */
    public static void deleteDirectory(File f){
        try {
            FileUtils.deleteDirectory(f);
        } catch (IOException e) {
            System.err.println("Problme deleting file: " + f.getPath());
        }
    }

    /**
     * Create a file
     * @param file fiale
     */
    public static void mkdir(File file){
        try {
            FileUtils.forceMkdir(file);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Delete file
     * @param file dile
     */
    public static void deleteFile(File file){
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}