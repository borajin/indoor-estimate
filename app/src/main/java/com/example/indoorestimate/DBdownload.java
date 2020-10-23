package com.example.indoorestimate;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DBdownload extends AsyncTask<String, String, String> {

    private ProgressDialog mDlg;
    private Context context;

    private static String DB_PATH = "/data/data/com.example.indoorestimate/databases";
    private static String DB_NAME = "/test.db";


    public DBdownload(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        mDlg = new ProgressDialog(context);
        mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDlg.setMessage("테스트_장전역 DB 다운로드...");
        mDlg.show();

        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        int count = 0;

        try {
            Thread.sleep(100);

            URL url = new URL(params[0]);
            URLConnection connection = url.openConnection();

            InputStream is = null;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            byte[] buf = new byte[1024];

            //상위 폴더 없으면 만들기
            File dir = new File(DB_PATH);
            if(!dir.exists()) dir.mkdirs();

            //db 파일 생성
            File target = new File(dir + DB_NAME);
            target.createNewFile();

            //db 다운로드
            is = connection.getInputStream();
            fos = new FileOutputStream(target);
            bos = new BufferedOutputStream(fos);

            long total = 0;
            int lenghtOfFile = connection.getContentLength();

            while ((count = is.read(buf)) > 0) {
                total += count;
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                bos.write(buf, 0, count);
                System.out.println("다운중");
            }

            bos.close();
            fos.close();
            is.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //리턴값은 다음에 수행되는 onProgressUpdate의 파라미터가 됨
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        if (progress[0].equals("progress")) {
            mDlg.setProgress(Integer.parseInt(progress[1]));
            mDlg.setMessage(progress[2]);
        } else if (progress[0].equals("max")) {
            mDlg.setMax(Integer.parseInt(progress[1]));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(String unused) {
        mDlg.dismiss();
    }
}
