package me.digi.androiddemo;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;

import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.errorhandling.DigiMeClientException;
import me.digi.sdk.core.errorhandling.SDKException;
import me.digi.sdk.core.SDKListener;
import me.digi.sdk.core.entities.CAAccounts;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.internal.AuthorizationException;
import me.digi.sdk.core.session.CASession;

/**
 * Created by Oli on 29/11/2017.
 */

public class NativeBridge extends ReactContextBaseJavaModule implements SDKListener {
    private static final String TAG = "NativeBridge";

    public NativeBridge(ReactApplicationContext reactContext) {
        super(reactContext);

        try {
            DigiMeClient.checkClientInitialized();
        } catch (DigiMeClientException ex) {
            DigiMeClient.init(getCurrentActivity());
        }
        DigiMeClient.getInstance().addListener(this);
    }

    private void sendEvent(String eventName) {
        sendEvent(eventName, null);
    }

    private void sendEvent(String eventName, @Nullable String data) {
        super.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, data);
    }

    @Override
    public String getName() {
        return "NativeBridge";
    }

    @ReactMethod
    public void initSDK() {
        Log.d(TAG, "initSDK");
        if (getCurrentActivity() != null) {
            DigiMeClient.getInstance().authorize(getCurrentActivity(), null);
        } else {
            Log.d(TAG, "initSDK: Can't Authorize - Current Activity not set");
        }
    }

    //
    // SDK LISTENER FUNCTIONS
    //
    public void sessionCreated(CASession session) {
        Log.d(TAG, "SessionCreated. Token: " + session.getSessionKey());
    }

    public void sessionCreateFailed(SDKException reason) {
        Log.d(TAG, "sessionCreateFailed. " + reason.getMessage());
    }

    public void authorizeSucceeded(CASession session) {
        Log.d(TAG, "authorizeSucceeded. Token: " + session.getSessionKey());
        sendEvent("userAuthAccept");
        DigiMeClient.getInstance().getFileList(null);
    }

    public void authorizeDenied(AuthorizationException reason) {
        Log.d(TAG, "authorizeDenied. Failed to authorize session; Reason " + reason.getThrowReason().name());
        sendEvent("userAuthReject");
    }

    public void authorizeFailedWithWrongRequestCode() {
        Log.d(TAG, "authorizeFailedWithWrongRequestCode.");
    }

    public void clientRetrievedFileList(CAFiles files) {
        Number downloaded = files.fileIds.size();
        Log.d(TAG, "clientRetrievedFileList: downloaded "+downloaded+" files");
        for (final String fileId :files.fileIds) {
            Log.d(TAG, "clientRetrievedFileList. getting JSON for fileId" + fileId);
            DigiMeClient.getInstance().getFileJSON(fileId, null);
        }
    }

    public void clientFailedOnFileList(SDKException reason) {
        Log.d(TAG, "clientFailedOnFileList. Failed to retrieve file list: " + reason.getMessage());
    }

    public void contentRetrievedForFile(String fileId, CAFileResponse content) {
        Log.d(TAG, "contentRetrievedForFile: ");
    }

    public void jsonRetrievedForFile(String fileId, JsonElement content) {
        Log.d(TAG, "jsonRetrievedForFile. " + content.toString());
        String data = ((JsonObject) content).get("fileContent").toString();
        sendEvent("fileData", data);
    }

    public void contentRetrieveFailed(String fileId, SDKException reason) {
        Log.d(TAG, "contentRetrieveFailed. Failed to retrieve file content for file: " + fileId + "; Reason: " + reason);
    }

    public void accountsRetrieveFailed(SDKException var1) {
        Log.d(TAG, "accountsRetrieveFailed. " + var1.toString());
    }

    public void accountsRetrieved(CAAccounts var1) {
        Log.d(TAG, "accountsRetrieved. " + var1.toString());
    }
}
