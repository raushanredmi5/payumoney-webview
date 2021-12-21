package univershila.com.univershila.payumoney;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import univershila.com.univershila.R;
import univershila.com.univershila.util.CommonUtils;

public class PayUMoneyActivity extends AppCompatActivity {
    WebView webView;

    /**
     * Context for Activity
     */
    Context activity;
    /**
     * Order Id
     * To Request for Updating Payment Status if Payment Successfully Done
     */
    int userId, prodId, year; //Getting from Previous Activity
    String strUserId, formattedDate, orderId;

    /**
     * Required Fields
     */
    // Test Variables

    //TODO: test Accout
    /*private String mMerchantKey = "FCyqqZ";
    private String mSalt = "sfBpGA8E";
    private String mBaseURL = "https://test.payu.in";*/
    boolean addMoneyStatus = false;


    // TODO:Final Secure Variables for univershila
    private String mMerchantKey = "3h0GiC5R";
    private String mSalt = "neTpJVDWhv";
    private String mBaseURL = "https://secure.payu.in";

    private String mAction = ""; // For Final URL
    private String mTXNId; // This will create below randomly
    private String mHash; // This will create below randomly
    private String mProductInfo = "Course Items"; //Passing String only
    private String mFirstName = "abc"; // From Previous Activity
    private String mEmailId = "xyz@gmail.com"; // From Previous Activity
    private double mAmount = 10; // From Previous Activity
    private String sAmount; // From Previous Activity
    private String mPhone = "9122221122"; // From Previous Activity
    private String mServiceProvider = "payu_paisa";
    private String mSuccessUrl = "https://www.payumoney.com/mobileapp/payumoney/success.php";
    private String mFailedUrl = "https://www.payumoney.com/mobileapp/payumoney/failure.php";
    ProgressDialog progressDialog;


    boolean isFromOrder;
    /**
     * Handler
     */
    Handler mHandler = new Handler();

    /**
     * @param savedInstanceState
     */
    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled", "JavascriptInterface", "WrongConstant"})

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_pay_umoney);
        CommonUtils.setStatusBarGradiant(this);
        //webView = new WebView(this);
        webView = (WebView) findViewById(R.id.webView);
        progressDialog = new ProgressDialog(PayUMoneyActivity.this);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(c.getTime());
        year = c.get(Calendar.YEAR);
        /**
         * Context Variable
         */
        activity = getApplicationContext();

        /**
         * Actionbar Settings
         /* *//*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        ab.setHomeButtonEnabled(true);
        ab.setTitle(getString(R.string.title_activity_online_payment));
*/
        /**
         * Getting Intent Variables...
         */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            mFirstName = bundle.getString("name");
            mEmailId = bundle.getString("email");
            mAmount = bundle.getDouble("amount");
            mPhone = bundle.getString("phone");
            userId = bundle.getInt("id");
            prodId = bundle.getInt("prodid");
            orderId = bundle.getString("orderid");
            isFromOrder = bundle.getBoolean("isFromOrder");
            strUserId = String.valueOf(userId);
            sAmount = String.valueOf(mAmount);
            //Log.i(TAG, "" + mFirstName + " : " + mEmailId + " : " + mAmount + " : " + mPhone);
            /**
             * Creating Transaction Id
             */
            Random rand = new Random();
            String randomString = Integer.toString(rand.nextInt()) + (System.currentTimeMillis() / 1000L);
            mTXNId = hashCal("SHA-256", randomString).substring(0, 20);

            mAmount = new BigDecimal(mAmount).setScale(0, RoundingMode.UP).intValue();

            /**
             * Creating Hash Key
             */
            mHash = hashCal("SHA-512", mMerchantKey + "|" +
                    mTXNId + "|" +
                    mAmount + "|" +
                    mProductInfo + "|" +
                    mFirstName + "|" +
                    mEmailId + "|||||||||||" +
                    mSalt);
            System.out.println("hashecodeeeee: " + mHash);
            /**
             * Final Action URL...
             */
            mAction = mBaseURL.concat("/_payment");

            /**
             * WebView Client
             */
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    //  Toast.makeText(activity, "Oh no! " + error, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReceivedSslError(WebView view,
                                               final SslErrorHandler handler, SslError error) {
                    // Toast.makeText(activity, "SSL Error! " + error, Toast.LENGTH_SHORT).show();
                    //handler.proceed();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(PayUMoneyActivity.this);
                    builder.setMessage("Are you sure to continue");
                    builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.proceed();
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.cancel();
                        }
                    });
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return super.shouldOverrideUrlLoading(view, url);
                }

                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    //make sure dialog is showing
                    if (!progressDialog.isShowing()) {
                        progressDialog.show();
                        progressDialog.setCancelable(false);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    progressDialog.dismiss();
                    if (url.equals(mSuccessUrl)) {

                        //Log.e("PayuMoneyActivity", "onPageFinished: IsPaid :"+isPaid );
                       /* System.out.println("status: "+true);
                        System.out.println("transaction_id: "+mTXNId);
                        System.out.println("userId: "+userId);
                        System.out.println("isFromOrder: "+isFromOrder);*/
                        //String statusMsg = "Success Full Transaction";
                        /*if (!isPaid){
                            statusMsg = "Payment transaction has been successfull, But Course is still not Enrolled. Please Contact to UniversHila Support Team";
                        }*/
                        Intent intent = new Intent(PayUMoneyActivity.this, PaymentStatusActivity.class);
                        intent.putExtra("status", "Success");
                        intent.putExtra("transaction_id", mTXNId);
                        intent.putExtra("id", userId);
                        intent.putExtra("orderid",orderId);
                        intent.putExtra("prodid",String.valueOf(prodId));
                        intent.putExtra("amount", mAmount);
                        intent.putExtra("isFromOrder", isFromOrder);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else if (url.equals(mFailedUrl)) {
                       /* System.out.println("false status: "+false);
                        System.out.println("false transaction_id: "+mTXNId);
                        System.out.println("false userId: "+userId);
                        System.out.println("false isFromOrder: "+isFromOrder);*/

                        Intent intent = new Intent(PayUMoneyActivity.this, PaymentStatusActivity.class);
                        intent.putExtra("status", "Failed");
                        intent.putExtra("amount", mAmount);
                        intent.putExtra("transaction_id", mTXNId);
                        intent.putExtra("id", userId);
                        intent.putExtra("prodid",prodId);
                        intent.putExtra("orderid",orderId);
                        intent.putExtra("isFromOrder", isFromOrder);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    super.onPageFinished(view, url);
                }
            });

            webView.setVisibility(View.VISIBLE);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setCacheMode(2);
            webView.getSettings().setDomStorageEnabled(true);
            webView.clearHistory();
            webView.clearCache(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setUseWideViewPort(false);
            webView.getSettings().setLoadWithOverviewMode(false);
            webView.addJavascriptInterface(new PayUJavaScriptInterface(PayUMoneyActivity.this), "PayUMoney");
            //webView.addJavascriptInterface(new PayUJavaScriptInterface(), "PayUMoney");
            /**
             * Mapping Compulsory Key Value Pairs
             */
            Map<String, String> mapParams = new HashMap<>();

            mapParams.put("key", mMerchantKey);
            mapParams.put("txnid", mTXNId);
            mapParams.put("amount", String.valueOf(mAmount));
            mapParams.put("productinfo", mProductInfo);
            mapParams.put("firstname", mFirstName);
            mapParams.put("email", mEmailId);
            mapParams.put("phone", mPhone);
            mapParams.put("surl", mSuccessUrl);
            mapParams.put("furl", mFailedUrl);
            mapParams.put("hash", mHash);
            mapParams.put("service_provider", mServiceProvider);

            webViewClientPost(webView, mAction, mapParams.entrySet());
        } else {
            Toast.makeText(activity, "Something went wrong, Try again.", Toast.LENGTH_LONG).show();
        }


    }

    public void webViewClientPost(WebView webView, String url,
                                  Collection<Map.Entry<String, String>> postData) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head></head>");
        sb.append("<body onload='form1.submit()'>");
        sb.append(String.format("<form id='form1' action='%s' method='%s'>", url, "post"));

        for (Map.Entry<String, String> item : postData) {
            sb.append(String.format("<input name='%s' type='hidden' value='%s' />", item.getKey(), item.getValue()));
        }
        sb.append("</form></body></html>");

        Log.d("TAG", "webViewClientPost called: " + sb.toString());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading. Please wait...");
        webView.loadData(sb.toString(), "text/html", "utf-8");
    }

    /**
     * Hash Key Calculation
     *
     * @param type
     * @param str
     * @return
     */
    public String hashCal(String type, String str) {
        byte[] hashSequence = str.getBytes();
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest algorithm = MessageDigest.getInstance(type);
            algorithm.reset();
            algorithm.update(hashSequence);
            byte messageDigest[] = algorithm.digest();

            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1)
                    hexString.append("0");
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException NSAE) {
        }
        return hexString.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onPressingBack();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        onPressingBack();
    }

    /**
     * On Pressing Back
     * Giving Alert...
     */
    private void onPressingBack() {

        final Intent intent;

        /*if(isFromOrder)
            intent = new Intent(PayUMoneyActivity.this, ProductInCartList.class);
        else
            intent = new Intent(PayUMoneyActivity.this, MainActivity.class);*/
       /* intent = new Intent(PayUMoneyActivity.this, AddMoney.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PayUMoneyActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Warning");

        // Setting Dialog Message
        alertDialog.setMessage("Do you cancel this transaction?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                // startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public class PayUJavaScriptInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        PayUJavaScriptInterface(Context c) {
            mContext = c;
        }

        public void success(long id, final String paymentId) {
            mHandler.post(new Runnable() {

                public void run() {
                    mHandler = null;
                    Toast.makeText(PayUMoneyActivity.this, "Payment Successfully.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
