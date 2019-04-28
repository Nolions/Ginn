package tw.nolions.coffeebeanslife.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import tools.info;
import tw.nolions.coffeebeanslife.R;

public class ExportToCSV extends AsyncTask<HashMap<Long, JSONObject>, Boolean, Boolean> {
    private Context context;
    private ProgressDialog dialog;
    private String fileName;

    public ExportToCSV(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    @Override
    protected void onPreExecute() {
        Log.e(info.TAG(),"ExportToCSV::onPreExecute()");
        dialog = new ProgressDialog(context);
        dialog.setTitle(context.getResources().getString(R.string.export));
        dialog.setMessage(context.getResources().getString(R.string.exporting));
        dialog.setCancelable(false);
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(HashMap<Long, JSONObject>... params) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(info.TAG(),"ExportToCSV::doInBackground(), InternalError error : " + e.getMessage());
        }

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "coffee" + this.fileName + ".csv");

        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            String[] field = new String[]{
                    context.getString(R.string.export_time),
                    context.getString(R.string.temp_beans),
                    context.getString(R.string.temp_stove),
                    context.getString(R.string.temp_environment),
            };
            csvWrite.writeNext(field);

            for(int i = 0; i< params.length; i++) {
                Map<Long, JSONObject> param = new TreeMap<Long, JSONObject>(params[i]);
                for(Long key: param.keySet()) {
                    JSONObject jsonObject = param.get(key);
                    try {
                        String[] data = new String[]{
                                String.valueOf(key),
                                jsonObject.getString("b"),
                                jsonObject.getString("s"),
                                jsonObject.getString("e"),
                        };
                        csvWrite.writeNext(data);
                    } catch (JSONException e) {
                        Log.e(info.TAG(), "ExportToCSV::doInBackground(), JSONException error: " + e.getMessage());
                    }

                }
            }
            csvWrite.close();
            return true;
        } catch (IOException e) {
            Log.e(info.TAG(),"ExportToCSV::doInBackground(), IOException error : " + e.getMessage());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (result) {
            Toast.makeText(context, context.getResources().getString(R.string.export_success), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.export_fail), Toast.LENGTH_LONG).show();
        }
    }
}
