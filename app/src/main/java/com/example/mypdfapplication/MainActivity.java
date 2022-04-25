package com.example.mypdfapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

 //   private String webViewUrl = "https://africau.edu/images/default/sample.pdf";
    private String webViewUrl = "https://m.media-amazon.com/images/G/01/APS/api-reference/APS_Integration_Guide_-_Android.pdf";
    ResponseBody pdfResponseBody;
    private URL url = null;
    private String fileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            url = new URL(webViewUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        fileName = url.getPath();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    public void downloadPDF(View view) {
        httpRequest();
    }

    private void httpRequest() {
     //   progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/pdf");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(webViewUrl)
                .method("GET", null)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.e("pdf activity", "onsuccess: " + response.message());
                InputStream is = response.body().byteStream();
                pdfResponseBody = response.body();

                Log.e("response", String.valueOf(pdfResponseBody));
                if (pdfResponseBody != null) {
                    checkStoragePermission();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("pdf activity", "onFailure: " + e.getMessage());
            }
        });
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            //permission granted
            managePdfFile();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }
    }
    private void managePdfFile() {
        if (writeResponseBodyToDisk(pdfResponseBody)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(context, context.getText(R.string.pdf_downloaded),
//                            Toast.LENGTH_SHORT).show();
                    //progressBar.setVisibility(View.INVISIBLE);
                    Log.e("success","pdf_downloaded");
                    File file = new File(Environment.
                            getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                            + File.separator + "adraj_pdf.pdf");

                 //   pdfView.fromFile(file).load();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "failed_to_download_pdf",
                            Toast.LENGTH_SHORT).show();
                    Log.e("failure","failed_to_download_pdf");
                }
            });
        }
    }
    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs
          //  File futureStudioIconFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            /*java.io.File futureStudioIconFile = new java.io.File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/" + "adraj_pdf.pdf");*/
         /*   ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getCacheDir();*/
            java.io.File  futureStudioIconFile = new java.io.File(getCacheDir(), fileName);
            InputStream inputStream = null;
            OutputStream outputStream = null;
           // futureStudioIconFile.mkdirs();
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.e("pdf_download", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}