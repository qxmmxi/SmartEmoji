package utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * created by shonary on 18/10/22
 * email： xiaonaxi.mail@gmail.com
 */
public class FileUtil {

    private static String SDCardRoot;
    private static File updateFile;

    static {
        SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    public static String getSDCardPath() {
        return SDCardRoot;
    }

    public static String getDataFilePath(Context context) {
        if (context != null) {
            return context.getFilesDir().getAbsolutePath() + File.separator;
        }
        return "";
    }

    public static File createFileInSDCard(String fileName, String dir) {
        File file = new File(SDCardRoot + dir + File.separator + fileName);
        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateFile = file;
        return file;
    }

    public static File creatSDDir(String dir) {
        File dirFile = new File(SDCardRoot + dir + File.separator);
        dirFile.mkdirs();
        return dirFile;
    }

    public static boolean isFileExist(String fileName, String path) {
        File file = new File(SDCardRoot + path + File.separator + fileName);
        return file.exists();
    }

    public static File writeToSDFromInput(String path, String fileName, InputStream input) {

        File file = null;
        OutputStream output = null;
        try {
            file = createFileInSDCard(fileName, path);
            output = new FileOutputStream(file, false);
            byte buffer[] = new byte[4 * 1024];
            int temp;
            while ((temp = input.read(buffer)) != -1) {
                output.write(buffer, 0, temp);
            }
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null){
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static File writeToSDFromInput(String path, String fileName, String data) {

        File file = null;
        OutputStreamWriter outputWriter = null;
        OutputStream outputStream = null;
        try {
            creatSDDir(path);
            file = createFileInSDCard(fileName, path);
            outputStream = new FileOutputStream(file, false);
            outputWriter = new OutputStreamWriter(outputStream);
            outputWriter.write(data);
            outputWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputWriter != null) {
                    outputWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static String getFromCipherConnection(String actionUrl, String content, String path) {
        try {
            File[] files = new File[1];
            files[0] = new File(path);
            String BOUNDARY = java.util.UUID.randomUUID().toString();
            String PREFIX = "--", LINEND = "\r\n";
            String MULTIPART_FROM_DATA = "multipart/form-data";
            String CHARSET = "UTF-8";
            URL uri = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(5 * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
            StringBuilder sb = new StringBuilder();
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\"userName\"" + LINEND);// \"userName\"
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(content);
            sb.append(LINEND);

            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(sb.toString().getBytes());
            if (files != null) {
                for (File file : files) {
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(PREFIX);
                    sb1.append(BOUNDARY);
                    sb1.append(LINEND);
                    sb1.append("Content-Disposition: form-data; name=\"" + file.getName()
                            + "\"; filename=\"" + file.getName() + "\"" + LINEND);
                    sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET
                            + LINEND);
                    sb1.append(LINEND);
                    outStream.write(sb1.toString().getBytes());
                    try {
                        InputStream is = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while ((len = is.read(buffer)) != -1) {

                            outStream.write(buffer, 0, len);
                        }
                        is.close();
                        outStream.write(LINEND.getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            outStream.close();
            int res = conn.getResponseCode();
            if (res == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String line = null;
                StringBuilder result = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                in.close();
                conn.disconnect();
                return "true";
            } else {
                return "";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static byte[] getFileContent(String fileName) {
        if (fileName == null || TextUtils.isEmpty(fileName)) {
            return null;
        }

        File file = new File(fileName);
        if(file != null && !file.exists()){
            return null;
        }

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(fileName);
            int length = fin.available();
            byte[] bytes = new byte[length];
            fin.read(bytes);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void delete(String path){
        File file = new File(path);
        if(file != null && file.exists()){
            delete(file);
        }
    }

    public static void delete(File file) {
        try {
            if (file == null || !file.exists()) {
                return;
            }

            if (file.isFile()) {
                file.delete();
                return;
            }

            if (file.isDirectory()) {
                File[] childFiles = file.listFiles();
                if (childFiles == null || childFiles.length == 0) {
                    file.delete();
                    return;
                }

                for (int i = 0; i < childFiles.length; i++) {
                    delete(childFiles[i]);
                }
                file.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteHistoryFiles(File dirFile, int timeLine) {
        if (!dirFile.exists() || dirFile.isFile()) {
            return;
        }

        try {
            if (dirFile.isDirectory()) {
                File[] childFiles = dirFile.listFiles();
                if (childFiles == null || childFiles.length == 0) {
                    return;
                }
                Long timeLimit = Long.valueOf(timeLine) * 1000;
                Long fileTime;
                for (int i = 0; i < childFiles.length; i++) {
                    fileTime = childFiles[i].lastModified();
                    if (fileTime < timeLimit) {
                        delete(childFiles[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File save2File(String savePath, String saveName,
                                 String crashReport) {
        try {
            File dir = new File(savePath);
            if (!dir.exists())
                dir.mkdir();
            File file = new File(dir, saveName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(crashReport.getBytes());
            fos.close();
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getFileLen(File file) {
        long total = 0;
        try {
            InputStream is = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                total += len;
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }
}
