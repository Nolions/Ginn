package tw.nolions.coffeebeanslife.service.asyncTask;

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

import tw.nolions.coffeebeanslife.R;
import tw.nolions.coffeebeanslife.service.Application.MainApplication;
import tw.nolions.coffeebeanslife.widget.SmallProgressDialogUtil;

public class ExportToCSVAsyncTask extends AsyncTask<HashMap<Integer, JSONObject>, Boolean, Boolean> {
    private Context context;
    private SmallProgressDialogUtil smallProgressDialog;
    private String fileName;
    private String mTag;

    public ExportToCSVAsyncTask(Context context, MainApplication app, String fileName) {
        this.context = context;
        this.fileName = fileName;
        setTag(app.TAG());
    }

    @Override
    protected void onPreExecute() {
        Log.e(getTag(),"ExportToCSVAsyncTask::onPreExecute()");
        smallProgressDialog = new SmallProgressDialogUtil(context, context.getResources().getString(R.string.exporting));
        smallProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(HashMap<Integer, JSONObject>... params) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(getTag(),"ExportToCSV::doInBackground(), InternalError error : " + e.getMessage());
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
                Map<Integer, JSONObject> param = new TreeMap<Integer, JSONObject>(params[i]);
                for(Integer key: param.keySet()) {
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
                        Log.e(getTag(), "ExportToCSV::doInBackground(), JSONException error: " + e.getMessage());
                    }

                }
            }
            csvWrite.close();
            return true;
        } catch (IOException e) {
            Log.e(getTag(),"ExportToCSV::doInBackground(), IOException error : " + e.getMessage());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        smallProgressDialog.dismiss();

        if (result) {
            Toast.makeText(context, context.getResources().getString(R.string.export_success), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.export_fail), Toast.LENGTH_LONG).show();
        }
    }

    private void setTag(String tag) {
        mTag = tag;
    }

    private String getTag() {
        return mTag;
    }
}
