package com.febino.dependencies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.aquafish.BuildConfig;
import com.febino.aquafish.MainActivity;
import com.febino.dataclass.BillDetails;
import com.febino.dataclass.OrderDetails;
import com.febino.dataclass.TraderDetails;
import com.itextpdf.text.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

public class BillGenerator {


    private Document billDocument;
    private File targetFolder;
    private Context context;
    DataBaseManager dbm;
    private String fileName = "";
    private File folder;


    DecimalFormat decimalFormat;

    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private BillDetails billDetails;
    private static final int PERMISSION_ALL = 1;

    private Bitmap billScreenShotBitMap;

    private View billView;
    public BillGenerator(Context context,BillDetails billDetails, Activity activity, View billView){
        this.billView = billView;
        this.billDetails = billDetails;
        this.context = context;


        if (!ProjectUtils.hasPermission(context, ProjectUtils.STORAGE_PERMISSIONS)) {
            ActivityCompat.requestPermissions( activity, PERMISSIONS, PERMISSION_ALL);
            return;
        }

        String target;



        this.fileName = getFileName(billDetails);

        this.fileName = fileName.replace(" ","_").replace(":","_").replace("-","_");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            folder = new File("/sdcard/", MainActivity.APP_NAME);
        }else{
            folder = new File(context.getFilesDir(), MainActivity.APP_NAME);
        }

        try {
            if(!folder.exists())    folder.mkdirs();
        }catch (Exception e){
            e.printStackTrace();
        }

        target = folder.getPath()+"/"+this.fileName+".png";

        targetFolder = new File(target);


    }

    public void share() {
        billScreenShotBitMap = getScreenShot(billView);
        store(billScreenShotBitMap);
        shareImage(targetFolder);
    }

    public void share(ListView listView) {
        billScreenShotBitMap = getFullViewScreenShot(billView, listView);
        store(billScreenShotBitMap);
        shareImage(targetFolder);
    }

    public void shareImage(File file){

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent shareIntentFile = new Intent(Intent.ACTION_SEND);
        Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(context), BuildConfig.APPLICATION_ID + ".provider", file);
        shareIntentFile.setType(URLConnection.guessContentTypeFromName(file.getName()));
        shareIntentFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntentFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        shareIntentFile.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntentFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...");
        shareIntentFile.putExtra(Intent.EXTRA_TEXT, "Sharing File " + file.getName());
        context.startActivity(Intent.createChooser(shareIntentFile,"Share File"));

    }

    private Bitmap getFullViewScreenShot(View view, ListView listView) {
        View screenView = view;

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//
//        int screenWidth = displayMetrics.widthPixels;
//        int screenHeight = displayMetrics.heightPixels;
//
//



        ListAdapter adapter = listView.getAdapter();

        if (adapter == null || adapter.getCount() == 0) return null;
        int totalHeight = 0;

        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.UNSPECIFIED
            );
            totalHeight += listItem.getMeasuredHeight();
        }

        totalHeight = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));

        int oldHeight = listView.getHeight();
        int heightDifference = totalHeight - oldHeight;

        int addHeight = totalHeight - oldHeight < 0 ? oldHeight : totalHeight;

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = addHeight;
        listView.setLayoutParams(params);


        screenView.measure(
                View.MeasureSpec.makeMeasureSpec(screenView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        screenView.layout(0, 0, screenView.getMeasuredWidth(), screenView.getMeasuredHeight());
//
//        Log.i("old List Height", "" + oldHeight);
//        Log.i("measured list Height", "" + totalHeight);
//        Log.i("Difference Height", "" + heightDifference);
//        Log.i("old Scree Height", "" + screenView.getMeasuredHeight());
//        Log.i("new Scree Height", "" + (screenView.getMeasuredHeight()+heightDifference));
//
//        Log.i("Actual Screen Width", "" + screenWidth);
//        Log.i("Actual Screen Height", "" + screenHeight);


        Bitmap bitmap = Bitmap.createBitmap(screenView.getMeasuredWidth(), screenView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        screenView.draw(canvas);

        return bitmap;
    }

    private static Bitmap getScreenShot(View view) {
        View screenView = view;
//        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void store(Bitmap bm){
//        final static String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
//        File dir = new File(dirPath);
//        if(!dir.exists())
//            dir.mkdirs();
//        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(targetFolder);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public String getFileName(BillDetails billDetails) {
        String fileName = "Bill_";
        fileName += billDetails.getBillNo() + "_";
        fileName += billDetails.getBillDate();
        return fileName;
    }
}
