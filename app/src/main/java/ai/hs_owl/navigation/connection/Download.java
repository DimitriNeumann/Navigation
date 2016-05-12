package ai.hs_owl.navigation.connection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by mberg on 11.05.2016.
 */
public class Download {
    static boolean isLoading = false;
    public static void startDownload(Context c,  Synchronize.DownloadHandler callback,String... url)
    {
        if(isLoading)
            return;
        else
            isLoading = true;
        ProgressDialog pDialog = new ProgressDialog(c);
        pDialog.setTitle("Lade Datei(en)...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(0);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
        pDialog.show();
        DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(pDialog, callback);
        downloadFileFromURL.execute(url);



    }
    private static class DownloadFileFromURL extends AsyncTask<String, String, Boolean> {
        ProgressDialog pDialog;
        Synchronize.DownloadHandler callback;

       public DownloadFileFromURL(ProgressDialog pDialog, Synchronize.DownloadHandler callback)
       {
           this.pDialog = pDialog;
           this.callback = callback;
       }
        @Override
        protected Boolean doInBackground(String... f_url) {
            int count;
            try {
                for (String s : f_url) {
                    URL url = new URL(s);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    int lenghtOfFile = conection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);
                    OutputStream output = new FileOutputStream(Synchronize.rootpath+ s.substring(s.lastIndexOf("/")));
                    publishProgress(new String[]{"-1", s.substring(s.lastIndexOf("/"))});
                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                return true;
                }catch(Exception e) {
                Log.e("Error: ", e.getMessage());
            }

                return false;


        }
        protected void onProgressUpdate(String... progress) {
            if(progress[0].equals("-1"))
                pDialog.setMessage("Datei: " + progress[1]);
            else
                pDialog.setProgress(Integer.parseInt(progress[0]));
        }
        @Override
        protected void onPostExecute(Boolean success) {
            pDialog.dismiss();
            isLoading = false;
            if(success)
                callback.data_received();
        }

    }
}
