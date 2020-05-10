package com.drobisch.partkeeprscannrapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import androidx.appcompat.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * This sample performs continuous scanning, displaying the barcode and source image whenever
 * a barcode is scanned.
 */
public class ContinuousCaptureActivity extends Activity {
    private static final String TAG = ContinuousCaptureActivity.class.getSimpleName();
    private DecoratedBarcodeView barcodeView;
    private boolean isNewTag = false;
    private String  actualCode = "";
    private String mUser;
    private String mPassword;
    private String mServer;
    private Integer mPartPartID = -1;
    private TextView mPartNameView;
    private TextView mPartStockView;
    private TextView mPartLocationView;



    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                if(actualCode.equals(result.getText()))
                {
                    actualCode = result.getText();
                } else {
                    actualCode = result.getText();

                    Log.d("barcodeResult", "New tag detected");
                    isNewTag = true;
                }
            }
            if(isNewTag == true) {
                checkInternetConenction();
                Toast infoToast = Toast.makeText(getApplicationContext(),actualCode,Toast.LENGTH_SHORT);
                //specify the toast display position exact parent layout center. no x or y offset
                infoToast.setGravity(Gravity.BOTTOM,0,390);
                infoToast.show();
                updatePartInfo(Integer.parseInt(actualCode));
                isNewTag = false;
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Bundle bundle = getIntent().getExtras();

        Log.d("CaptureActivity", "User:" + bundle.getString("user"));
        Log.d("CaptureActivity", "Pwd:" + bundle.getString("password"));
        Log.d("CaptureActivity", "Server:" + bundle.getString("server"));

        mUser =  bundle.getString("user");
        mServer =  bundle.getString("server");
        mPassword =  bundle.getString("password");


        mPartNameView = (TextView) findViewById(R.id.partName);
        mPartLocationView = (TextView) findViewById(R.id.partLocation);
        mPartStockView = (TextView) findViewById(R.id.partStock);

        mPartNameView.setText("");
        mPartLocationView.setText("");
        mPartStockView.setText("0");
        mPartPartID = -1;

        ImageButton mAddStockButton = (ImageButton) findViewById(R.id.addStock_button);
        mAddStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStock();
            }
        });

        ImageButton mRemoveStockButton = (ImageButton) findViewById(R.id.removeStock_button);
        mRemoveStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeStock();
            }
        });

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.setStatusText("");
        barcodeView.decodeContinuous(callback);
    }

    private void updatePartInfo(int partID) {
        ApiPartTask task = new ApiPartTask(mUser,mPassword,mServer,partID,"","");
        task.execute((Void) null);
    }

    private void addStock() {
        Log.d("CaptureActivity", "addStock");
        if(mPartPartID != -1) {
            ApiPartTask task = new ApiPartTask(mUser, mPassword, mServer, mPartPartID,"addStock", "quantity=1&price=0&comment=");
            task.execute((Void) null);
        }
    }

    private void removeStock() {
        Log.d("CaptureActivity", "removeStock");
        if(mPartPartID != -1) {
            ApiPartTask task = new ApiPartTask(mUser, mPassword, mServer, mPartPartID,"addStock", "quantity=-1&price=0&comment=");
            task.execute((Void) null);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||

                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {
            return false;
        }
        return false;
    }

    private Pair<InputStream, HttpURLConnection> doHttpConnection(String urlStr, String user, String password, String json) {
        InputStream in = null;
        HttpURLConnection httpConn = null;
        String restURI = urlStr; // "http://" + urlStr + "/api/parts/1/addStock";
        int resCode = -1;

        try {
            URL url = new URL(restURI);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }

            String userToken= user + ":" + password;
            byte[] data = userToken.getBytes("UTF-8");
            String encode = Base64.encodeToString(data,  Base64.NO_WRAP);
            httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("PUT");
            httpConn.setRequestProperty("Authorization", "Basic " + encode);
            httpConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");

            httpConn.setRequestProperty("Content-length", json.getBytes().length + "");
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setUseCaches(false);

            OutputStream outputStream = httpConn.getOutputStream();
            outputStream.write(json.getBytes("UTF-8"));
            outputStream.close();

            httpConn.connect();

            resCode = httpConn.getResponseCode();

            boolean redirect = false;

            if (resCode != HttpURLConnection.HTTP_OK) {
                if (resCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || resCode == HttpURLConnection.HTTP_MOVED_PERM
                        || resCode == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;

            }

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
                Log.d("LoginActivity","Successful URL-connection" + String.valueOf(resCode));
            }
            else {
                Log.d("LoginActivity","Error URL-connection" + String.valueOf(resCode));
                return Pair.create(null,httpConn);
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return Pair.create(in,httpConn);
    }

    public class ApiPartTask extends AsyncTask<Void, Void, Boolean> {
        private final String mUser;
        private final String mPassword;
        private final String mServer;
        private final String mJson;
        private final String mCommand;

        private String mPartName = "";
        private Integer mPartStock = 0;
        private String mPartLocation = "";
        private Integer mPartID;
        private Boolean error = false;
        private String errorString;


        ApiPartTask(String user, String password, String server, int partID, String command, String json) {
            mUser = user;
            mPassword = password;
            mServer = server;
            mPartID = partID;
            mJson = json;
            mCommand = command;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            InputStream in = null;
            HttpURLConnection httpcon = null;
            String jsonString = "";
            Message msg = Message.obtain();
            msg.what = 1;
            String Name = "";
            Pair<InputStream, HttpURLConnection> httpResult;
            try {
                String restURL = mServer + "/api/parts/" + mPartID.toString();
                if(mCommand != "")
                    restURL += "/" + mCommand;
                httpResult = doHttpConnection(restURL,mUser,mPassword,mJson);
                in = httpResult.first;
                httpcon = httpResult.second;
                /*Bundle b = new Bundle();
                b.putString("bitmap", "test");
                msg.setData(b);
                */
                if(in != null) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    StringBuilder total = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line).append('\n');
                    }
                    jsonString = total.toString();
                    in.close();
                }
                else
                {
                    if(httpcon != null) {

                        switch (httpcon.getResponseCode())
                        {
                            case 401:
                                error = true;
                                errorString = getString(R.string.error_incorrect_password_user);
                            break;
                            case 404:
                                error = true;
                                errorString = getString(R.string.error_part_not_exists);
                            break;
                            default:
                                error = true;
                                errorString = getString(R.string.error_http_long);
                            break;
                        }
                    }
                    else {
                        error = true;
                        errorString = getString(R.string.error_connection_long);
                    }
                }
            }

            catch (IOException e1) {
                e1.printStackTrace();
                error = true;
                errorString = getString(R.string.error_server_connect_failed);
            }

            try {
                JSONObject json= new JSONObject(jsonString);
                mPartName = (String) json.get("name") + " (ID: " + mPartID.toString() + ")";
                JSONObject jsonStorage = json.getJSONObject("storageLocation");
                mPartLocation = (String) jsonStorage.get("name");
                mPartStock = json.getInt("stockLevel");
            } catch (JSONException e) {
                e.printStackTrace();
                error = true;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mPartNameView.setText(mPartName);
            mPartLocationView.setText(mPartLocation);
            mPartStockView.setText(mPartStock.toString());
            mPartPartID = mPartID;
            if(error == true) {
                mPartPartID = -1;
                openMessageBox("Error", errorString);
            }
        }

        @Override
        protected void onCancelled() {

        }


        protected void openMessageBox(String headline, String message)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(ContinuousCaptureActivity.this);
            builder.setMessage(message).setTitle(headline);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int id) {}});
            AlertDialog dialog = builder.create();
            dialog.show();
        }


    }
}
