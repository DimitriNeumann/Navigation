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
    public static void startDownload(Context c, String... url)
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

        DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(pDialog);
        downloadFileFromURL.execute(url);



    }
    private static class DownloadFileFromURL extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;

       public DownloadFileFromURL(ProgressDialog pDialog)
       {
           this.pDialog = pDialog;
       }
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                for (String s : f_url) {
                    URL url = new URL(s);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    int lenghtOfFile = conection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);
                    OutputStream output = new FileOutputStream(Environment
                            .getExternalStorageDirectory().toString()
                            +"/hs_owl_navigation/"+ s.substring(s.lastIndexOf("//")));
                    publishProgress(new String[]{"-1", s.substring(s.lastIndexOf("//"))});
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
                }catch(Exception e) {
                Log.e("Error: ", e.getMessage());
            }

                return null;


        }
        protected void onProgressUpdate(String... progress) {
            if(progress[0].equals("-1"))
                pDialog.setMessage("Datei: " + progress[1]);
            else
                pDialog.setProgress(Integer.parseInt(progress[0]));
        }
        @Override
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            isLoading = false;
        }

    }
}
