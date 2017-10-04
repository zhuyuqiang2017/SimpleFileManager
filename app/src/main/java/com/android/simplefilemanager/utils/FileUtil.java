package com.android.simplefilemanager.utils;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;

import com.android.simplefilemanager.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Administrator on 2017/10/1 0001.
 */

public class FileUtil {

    public static File copyFile(File src, File path) throws Exception {

        try {

            if (src.isDirectory()) {
                LogUtil.e("copyFile : src.isDirectory()");
                if (src.getPath().equals(path.getPath())) throw new Exception();

                File directory = createDirectory(path, src.getName());

                for (File file : src.listFiles()) copyFile(file, directory);

                return directory;
            }
            else {

                File file = new File(path, src.getName());

                FileChannel channel = new FileInputStream(src).getChannel();

                channel.transferTo(0, channel.size(), new FileOutputStream(file).getChannel());

                return file;
            }
        }
        catch (Exception e) {

            throw new Exception(String.format("Error copying %s", src.getName()));
        }
    }

    public static File createDirectory(File path, String name) throws Exception {
        path.setWritable(true);
        File directory = new File(path, name);
        directory.setReadable(true);
        directory.setWritable(true);
        if(!directory.exists()){
            if (directory.mkdir())
                return directory;
        }else{
            throw new Exception(String.format("%s already exists", name));
        }
        throw new Exception(String.format("Error creating %s", name));
    }

    public static File createFile(File path, String name) throws Exception {
        path.setWritable(true);
        File file = new File(path, name);
        file.setReadable(true);
        file.setWritable(true);
        if(!file.exists()){
            if (file.createNewFile())
                return file;
        }else{
            throw new Exception(String.format("%s already exists", name));
        }
        throw new Exception(String.format("Error creating %s", name));
    }

    public static File deleteFile(File file) throws Exception {

        if (file.isDirectory()) {

            for (File child : file.listFiles()) {

                deleteFile(child);
            }
        }

        if (file.delete()) return file;

        throw new Exception(String.format("Error deleting %s", file.getName()));
    }

    public static File renameFile(File file, String name) throws Exception {

        String extension = getExtension(file.getName());

        if (!extension.isEmpty()) name += "." + extension;

        File newFile = new File(file.getParent(), name);

        if (file.renameTo(newFile)) return newFile;

        throw new Exception(String.format("Error renaming %s", file.getName()));
    }

    public static File unzip(File zip) throws Exception {

        File directory = createDirectory(zip.getParentFile(), removeExtension(zip.getName()));

        FileInputStream fileInputStream = new FileInputStream(zip);

        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        try (ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream)) {

            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {

                byte[] buffer = new byte[1024];

                File file = new File(directory, zipEntry.getName());

                if (zipEntry.isDirectory()) {

                    if (!file.mkdirs()) throw new Exception("Error uncompressing");
                }
                else {

                    int count;

                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

                        while ((count = zipInputStream.read(buffer)) != -1) {

                            fileOutputStream.write(buffer, 0, count);
                        }
                    }
                }
            }
        }

        return directory;
    }

    public static File getInternalStorage() {

        return Environment.getExternalStorageDirectory();
    }

    public static File getExternalStorage() {

        String path = System.getenv("SECONDARY_STORAGE");

        return path != null ? new File(path) : null;
    }

    public static File getPublicDirectory(String type) {

        //returns the path to the public directory of the given type

        return Environment.getExternalStoragePublicDirectory(type);
    }

    public static String getAlbum(File file) {

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(file.getPath());

            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        }
        catch (Exception e) {

            return null;
        }
    }

    public static String getArtist(File file) {

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(file.getPath());

            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        }
        catch (Exception e) {

            return null;
        }
    }

    public static String getDuration(File file) {

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(file.getPath());

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long milliseconds = Long.parseLong(duration);

            long s = milliseconds / 1000 % 60;

            long m = milliseconds / 1000 / 60 % 60;

            long h = milliseconds / 1000 / 60 / 60 % 24;

            if (h == 0) return String.format(Locale.getDefault(), "%02d:%02d", m, s);

            return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
        }
        catch (Exception e) {

            return null;
        }
    }

    public static String getLastModified(File file) {

        //returns the last modified date of the given file as a formatted string

        return DateFormat.format("dd MMM yyy", new Date(file.lastModified())).toString();
    }

    public static String getMimeType(File file) {

        //returns the mime type for the given file or null iff there is none

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(file.getName()));
    }

    public static String getName(File file) {

        switch (FileType.getFileType(file)) {

            case DIRECTORY:
                return file.getName();

            case MISC_FILE:
                return file.getName();

            default:
                return removeExtension(file.getName());
        }
    }

    public static String getPath(File file) {

        //returns the path of the given file or null if the file is null

        return file != null ? file.getPath() : null;
    }

    public static String getSize(Context context, File file) {

        if (file.isDirectory()) {

            File[] children = getChildren(file);

            if (children == null) return null;

            return String.format("%s items", children.length);
        }
        else {

            return Formatter.formatShortFileSize(context, file.length());
        }
    }

    public static String getStorageUsage(Context context) {

        File internal = getInternalStorage();

        File external = getExternalStorage();

        long f = internal.getFreeSpace();

        long t = internal.getTotalSpace();

        if (external != null) {

            f += external.getFreeSpace();

            t += external.getTotalSpace();
        }

        String use = Formatter.formatShortFileSize(context, t - f);
        String unused = Formatter.formatFileSize(context,f);
        String tot = Formatter.formatShortFileSize(context, t);
        StringBuffer sb = new StringBuffer();
        sb.append(context.getString(R.string.be_used)).append(use).append("\n")
                .append(context.getString(R.string.unused)).append(unused).append("\n")
                .append(context.getString(R.string.total_storage)).append(tot);
        return sb.toString();
    }

    public static String getTitle(File file) {

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(file.getPath());

            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        }
        catch (Exception e) {

            return null;
        }
    }

    private static String getExtension(String filename) {

        //returns the file extension or an empty string iff there is no extension

        return filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1) : "";
    }

    public static String removeExtension(String filename) {

        int index = filename.lastIndexOf(".");

        return index != -1 ? filename.substring(0, index) : filename;
    }

    public static int compareDate(File file1, File file2) {

        long lastModified1 = file1.lastModified();

        long lastModified2 = file2.lastModified();

        return Long.compare(lastModified2, lastModified1);
    }

    public static int compareName(File file1, File file2) {

        String name1 = file1.getName();

        String name2 = file2.getName();

        return name1.compareToIgnoreCase(name2);
    }

    public static int compareSize(File file1, File file2) {

        long length1 = file1.length();

        long length2 = file2.length();

        return Long.compare(length2, length1);
    }

    public static int getColorResource(File file) {

        switch (FileType.getFileType(file)) {

            case DIRECTORY:
                return R.color.directory;

            case MISC_FILE:
                return R.color.misc_file;

            case AUDIO:
                return R.color.audio;

            case IMAGE:
                return R.color.image;

            case VIDEO:
                return R.color.video;

            case DOC:
                return R.color.doc;

            case PPT:
                return R.color.ppt;

            case XLS:
                return R.color.xls;

            case PDF:
                return R.color.pdf;

            case TXT:
                return R.color.txt;

            case ZIP:
                return R.color.zip;

            default:
                return 0;
        }
    }

    //----------------------------------------------------------------------------------------------

    public static int getImageResource(File file) {

        switch (FileType.getFileType(file)) {

            case DIRECTORY:
                return R.drawable.ic_directory;

            case MISC_FILE:
                return R.drawable.ic_misc_file;

            case AUDIO:
                return R.drawable.ic_audio;

            case IMAGE:
                return R.drawable.ic_image;

            case VIDEO:
                return R.drawable.ic_video;

            case DOC:
                return R.drawable.ic_doc;

            case PPT:
                return R.drawable.ic_ppt;

            case XLS:
                return R.drawable.ic_xls;

            case PDF:
                return R.drawable.ic_pdf;

            case TXT:
                return R.drawable.ic_txt;

            case ZIP:
                return R.drawable.ic_zip;

            default:
                return 0;
        }
    }

    public static boolean isStorage(File dir) {

        return dir == null || dir.equals(getInternalStorage()) || dir.equals(getExternalStorage());
    }

    //----------------------------------------------------------------------------------------------

    public static File[] getChildren(File directory) {

        if (!directory.canRead()) return null;

        return directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.exists() && !pathname.isHidden();
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    public static ArrayList<File> getAudioLibrary(Context context) {

        ArrayList<File> list = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String data[] = new String[]{MediaStore.Audio.Media.DATA};

        String selection = MediaStore.Audio.Media.IS_MUSIC;

        Cursor cursor = new CursorLoader(context, uri, data, selection, null, null).loadInBackground();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));

                if (file.exists()) list.add(file);
            }

            cursor.close();
        }

        return list;
    }

    //----------------------------------------------------------------------------------------------

    public static ArrayList<File> getImageLibrary(Context context) {

        ArrayList<File> list = new ArrayList<>();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String data[] = new String[]{MediaStore.Images.Media.DATA};

        Cursor cursor = new CursorLoader(context, uri, data, null, null, null).loadInBackground();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));

                if (file.exists()) list.add(file);
            }

            cursor.close();
        }

        return list;
    }

    public static ArrayList<File> getVideoLibrary(Context context) {

        ArrayList<File> list = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String data[] = new String[]{MediaStore.Video.Media.DATA};

        Cursor cursor = new CursorLoader(context, uri, data, null, null, null).loadInBackground();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));

                if (file.exists()) list.add(file);
            }

            cursor.close();
        }

        return list;
    }

    public static ArrayList<File> searchFilesName(Context context, String name) {

        ArrayList<File> list = new ArrayList<>();

        Uri uri = MediaStore.Files.getContentUri("external");

        String data[] = new String[]{MediaStore.Files.FileColumns.DATA};

        Cursor cursor = new CursorLoader(context, uri, data, null, null, null).loadInBackground();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));
                if ((file.exists() || file.isDirectory()) && file.getName().contains(name)) list.add(file);
            }
            cursor.close();
        }
        return list;
    }

    public enum FileType {

        DIRECTORY, MISC_FILE, AUDIO, IMAGE, VIDEO, DOC, PPT, XLS, PDF, TXT, ZIP;

        public static FileType getFileType(File file) {

            if (file.isDirectory())
                return FileType.DIRECTORY;

            String mime = FileUtil.getMimeType(file);

            if (mime == null)
                return FileType.MISC_FILE;

            if (mime.startsWith("audio"))
                return FileType.AUDIO;

            if (mime.startsWith("image"))
                return FileType.IMAGE;

            if (mime.startsWith("video"))
                return FileType.VIDEO;

            if (mime.startsWith("application/ogg"))
                return FileType.AUDIO;

            if (mime.startsWith("application/msword"))
                return FileType.DOC;

            if (mime.startsWith("application/vnd.ms-word"))
                return FileType.DOC;

            if (mime.startsWith("application/vnd.ms-powerpoint"))
                return FileType.PPT;

            if (mime.startsWith("application/vnd.ms-excel"))
                return FileType.XLS;

            if (mime.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml"))
                return FileType.DOC;

            if (mime.startsWith("application/vnd.openxmlformats-officedocument.presentationml"))
                return FileType.PPT;

            if (mime.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml"))
                return FileType.XLS;

            if (mime.startsWith("application/pdf"))
                return FileType.PDF;

            if (mime.startsWith("text"))
                return FileType.TXT;

            if (mime.startsWith("application/zip"))
                return FileType.ZIP;

            return FileType.MISC_FILE;
        }
    }

    public static String getMIMEType(File file) {
        String type="*/*";
        String fName = file.getName();
        LogUtil.e(fName);
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0){
            return type;
        }
        String end=fName.substring(dotIndex,fName.length()).toLowerCase();
        if(end=="")return type;
        for(int i=0;i<MIME_MapTable.length;i++){
            if(end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }
    private static final String[][] MIME_MapTable = {
            { ".3gp", "video/3gpp" },
            { ".apk", "application/vnd.android.package-archive" },
            { ".asf", "video/x-ms-asf" },
            { ".avi", "video/x-msvideo" },
            { ".bin", "application/octet-stream" },
            { ".bmp", "image/bmp" },
            { ".c", "text/plain" },
            { ".class", "application/octet-stream" },
            { ".conf", "text/plain" },
            { ".cpp", "text/plain" },
            { ".doc", "application/msword" },
            { ".docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
            { ".xls", "application/vnd.ms-excel" },
            { ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
            { ".exe", "application/octet-stream" },
            { ".gif", "image/gif" },
            { ".gtar", "application/x-gtar" },
            { ".gz", "application/x-gzip" },
            { ".h", "text/plain" },
            { ".htm", "text/html" },
            { ".html", "text/html" },
            { ".jar", "application/java-archive" },
            { ".java", "text/plain" },
            { ".jpeg", "image/jpeg" },
            { ".jpg", "image/jpeg" },
            { ".js", "application/x-javascript" },
            { ".log", "text/plain" },
            { ".m3u", "audio/x-mpegurl" },
            { ".m4a", "audio/mp4a-latm" },
            { ".m4b", "audio/mp4a-latm" },
            { ".m4p", "audio/mp4a-latm" },
            { ".m4u", "video/vnd.mpegurl" },
            { ".m4v", "video/x-m4v" },
            { ".mov", "video/quicktime" },
            { ".mp2", "audio/x-mpeg" },
            { ".mp3", "audio/x-mpeg" },
            { ".mp4", "video/mp4" },
            { ".mpc", "application/vnd.mpohun.certificate" },
            { ".mpe", "video/mpeg" },
            { ".mpeg", "video/mpeg" },
            { ".mpg", "video/mpeg" },
            { ".mpg4", "video/mp4" },
            { ".mpga", "audio/mpeg" },
            { ".msg", "application/vnd.ms-outlook" },
            { ".ogg", "audio/ogg" },
            { ".pdf", "application/pdf" },
            { ".png", "image/png" },
            { ".pps", "application/vnd.ms-powerpoint" },
            { ".ppt", "application/vnd.ms-powerpoint" },
            { ".pptx",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" },
            { ".prop", "text/plain" }, { ".rc", "text/plain" },
            { ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" },
            { ".sh", "text/plain" }, { ".tar", "application/x-tar" },
            { ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
            { ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
            { ".wmv", "audio/x-ms-wmv" },
            { ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
            { ".z", "application/x-compress" },
            { ".zip", "application/x-zip-compressed" }, { "", "*/*" } };
}
