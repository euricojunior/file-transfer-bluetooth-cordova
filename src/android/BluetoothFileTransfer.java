package btFileTransferIndigo;

import org.apache.cordova.*;
import android.content.Context;
import android.content.ClipData;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;
import android.util.Log;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import btFileTransferIndigo.FileProvider;
import android.os.Environment;
/**
 * This class echoes a string called from JavaScript.
 */
public class BluetoothFileTransfer extends CordovaPlugin {
  private static final int DISCOVER_DURATION = 300;
  private static final int REQUEST_BLU = 1;
  private static final int FETCH_FILE  = 2;
  private static final int SEND_FILE  = 3;
  private static boolean sendText=false;
  private static JSONObject jsonData;
  CallbackContext callbackContextBt;
  BluetoothAdapter btAdatper = BluetoothAdapter.getDefaultAdapter();

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
	sendText=false;
    if (action.equals("sendFile")) {
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);              
		  
        this.sendFile(callbackContext);
        return true;
    }
	else if(action.equals("sendObject")){
		sendText=true;
		jsonData=args.optJSONObject(0);
		
        this.sendFile(callbackContext);
        return true;
	}
    return false;
  }

  private void sendFile(CallbackContext callbackContext) {
    callbackContextBt = callbackContext;
    if (btAdatper == null) {
        Toast.makeText(cordova.getActivity().getWindow().getContext(), "Device not support bluetooth", Toast.LENGTH_LONG).show();
		PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Device not support bluetooth");
        result.setKeepCallback(true);
        callbackContextBt.sendPluginResult(result);
    } else {
		if(sendText)
			sendText();
	    else
            enableBluetooth();
    }
  }
  
public void sendText()
{
		if(jsonData!=null)
		{
            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_TEXT, jsonData.toString());
            i.setType("application/octet-stream");
            PackageManager pm = cordova.getActivity().getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
            if (list.size() > 0) {
                String packageName = null;
                String className = null;
                boolean found = false;
            
                for (ResolveInfo info : list) {
                    packageName = info.activityInfo.packageName;
                    if (packageName.equals("com.android.bluetooth")) {
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }
                //CHECK BLUETOOTH available or not
                if (!found) {
                    Toast.makeText(cordova.getActivity().getWindow().getContext(), "Bluetooth not been found", Toast.LENGTH_LONG).show();
		            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Bluetooth not been found");
                    result.setKeepCallback(true);
                    callbackContextBt.sendPluginResult(result);
                } else {
                    i.setClassName(packageName, className);		
				    cordova.setActivityResultCallback(this);
                    cordova.getActivity().startActivityForResult(i,SEND_FILE);
                }
            }
		}
		else
	    {
			Toast.makeText(cordova.getActivity().getWindow().getContext(), "No data entered", Toast.LENGTH_LONG).show();
		    PluginResult result = new PluginResult(PluginResult.Status.ERROR, "No data entered");
            result.setKeepCallback(true);
            callbackContextBt.sendPluginResult(result);
		}
}

  //exit to application
  public void exit(View V) {
    btAdatper.disable();
    Toast.makeText(cordova.getActivity().getWindow().getContext(),"*** Now Bluetooth is off... Thanks. ***",Toast.LENGTH_LONG).show();
    cordova.getActivity().finish(); 
  }

public void enableBluetooth() {
    /*Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
    cordova.setActivityResultCallback(this);
    cordova.getActivity().startActivityForResult(discoveryIntent, REQUEST_BLU);*/
	
	Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
	intent.addCategory(Intent.CATEGORY_OPENABLE);
	intent.setType("*/*");
	intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
	cordova.setActivityResultCallback(this);
	cordova.getActivity().startActivityForResult(intent, FETCH_FILE);
}

//Override method for sending data via bluetooth availability
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU) {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		cordova.setActivityResultCallback(this);
		cordova.getActivity().startActivityForResult(intent, FETCH_FILE);
    } else if(resultCode == cordova.getActivity().RESULT_OK && requestCode == FETCH_FILE){
        //File file = new File(Environment.getExternalStorageDirectory(),"Sample.txt");
		//File file = new File(path);
        //i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		
        ClipData clipData = data.getClipData();
		if(data!=null)
		{
			ArrayList<Uri> uris=new ArrayList<Uri>();
			if(clipData==null)	
				uris.add(data.getData());
		    else
			{
                for(int temp=0; temp<clipData.getItemCount(); temp++){
                    ClipData.Item item = clipData.getItemAt(temp);
                    uris.add(item.getUri());
				}
			}
            
            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND_MULTIPLE);
		    i.setType("*/*");
			i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            PackageManager pm = cordova.getActivity().getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
            if (list.size() > 0) {
                String packageName = null;
                String className = null;
                boolean found = false;
            
                for (ResolveInfo info : list) {
                    packageName = info.activityInfo.packageName;
                    if (packageName.equals("com.android.bluetooth")) {
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }
                //CHECK BLUETOOTH available or not
                if (!found) {
                    Toast.makeText(cordova.getActivity().getWindow().getContext(), "Bluetooth not been found", Toast.LENGTH_LONG).show();
		            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Bluetooth not been found");
                    result.setKeepCallback(true);
                    callbackContextBt.sendPluginResult(result);
                } else {
                    i.setClassName(packageName, className);		
				    cordova.setActivityResultCallback(this);
                    cordova.getActivity().startActivityForResult(i,SEND_FILE);
                }
            }
		}
		else
	    {
			Toast.makeText(cordova.getActivity().getWindow().getContext(), "No file selected", Toast.LENGTH_LONG).show();
		    PluginResult result = new PluginResult(PluginResult.Status.ERROR, "No file selected");
            result.setKeepCallback(true);
            callbackContextBt.sendPluginResult(result);
		}
	}else if(requestCode == SEND_FILE){
		//Toast.makeText(cordova.getActivity().getWindow().getContext(), "Done...", Toast.LENGTH_LONG).show();
        PluginResult result = new PluginResult(PluginResult.Status.OK, "Done...");
        result.setKeepCallback(true);
        callbackContextBt.sendPluginResult(result);                        
	}
	else{
        Toast.makeText(cordova.getActivity().getWindow().getContext(), "Transfer is cancelled", Toast.LENGTH_LONG).show();
		PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Transfer is cancelled");
        result.setKeepCallback(true);
        callbackContextBt.sendPluginResult(result);
    }
}

}
