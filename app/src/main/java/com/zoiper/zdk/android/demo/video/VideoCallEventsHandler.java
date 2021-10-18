package com.zoiper.zdk.android.demo.video;

import android.content.DialogInterface;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;

import com.zoiper.zdk.Call;
import com.zoiper.zdk.CallStatus;
import com.zoiper.zdk.ExtendedError;
import com.zoiper.zdk.NetworkStatistics;
import com.zoiper.zdk.Types.CallLineStatus;
import com.zoiper.zdk.Types.CallMediaChannel;
import com.zoiper.zdk.Types.CallSecurityLevel;
import com.zoiper.zdk.Types.NetworkQualityLevel;
import com.zoiper.zdk.Types.OriginType;
import com.zoiper.zdk.Types.Zrtp.ZRTPAuthTag;
import com.zoiper.zdk.Types.Zrtp.ZRTPCipherAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPHashAlgorithm;
import com.zoiper.zdk.Types.Zrtp.ZRTPKeyAgreement;
import com.zoiper.zdk.Types.Zrtp.ZRTPSASEncoding;

/**
 * VideoCallEventsHandler
 *
 * @since 31/01/2019
 */
public class VideoCallEventsHandler implements com.zoiper.zdk.EventHandlers.CallEventsHandler {
    private final InVideoCallActivity activity;

    VideoCallEventsHandler(InVideoCallActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCallStatusChanged(Call call, CallStatus callStatus) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printStatusThreadSafe(callStatus.lineStatus().toString());
        }
        if(callStatus.lineStatus() == CallLineStatus.Terminated) activity.runOnUiThread(activity::delayedFinish);
    }

    @Override
    public void onCallExtendedError(Call call, ExtendedError extendedError) {

    }

    @Override
    public void onCallNetworkStatistics(Call call, NetworkStatistics networkStatistics) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printNetworkThreadSafe(
                    "BitrateOut: " + networkStatistics.averageOutputBitrate() +
                    "\nBitrateIn: " + networkStatistics.currentInputBitrate() +
                    "\nInputLoss: " + networkStatistics.currentInputLossPermil());
        }
    }

    @Override
    public void onCallNetworkQualityLevel(Call call, int callChannel, NetworkQualityLevel qualityLevel) {

    }

    @Override
    public void onCallTransferSucceeded(Call call) {

    }

    @Override
    public void onCallTransferFailure(Call call, ExtendedError extendedError) {

    }

    @Override
    public void onCallTransferStarted(Call call, String s, String s1, String s2) {

    }

    @Override
    public void onCallZrtpFailed(Call call, ExtendedError error) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printGeneralThreadSafe("onCallZrtpFailed: call= " + call.callHandle() + "; error= " + error.message());
        }
    }

    @Override
    public void onCallZrtpSuccess(Call call, String zidHex, int knownPeer, int cacheMismatch, int peerKnowsUs, ZRTPSASEncoding zrtpsasEncoding, String sas,
                                  ZRTPHashAlgorithm zrtpHashAlgorithm, ZRTPCipherAlgorithm zrtpCipherAlgorithm, ZRTPAuthTag zrtpAuthTag, ZRTPKeyAgreement zrtpKeyAgreement) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printGeneralThreadSafe("onCallZrtpSuccess: call= " + call.callHandle());
        }

        if ((knownPeer != 0) && (cacheMismatch == 0) && (peerKnowsUs != 0))
        {
            activity.runOnUiThread(() -> call.confirmZrtpSas(true));
        }
        else
        {
            activity.runOnUiThread(() -> new AlertDialog.Builder(this.activity)
                    .setTitle("SAS Verification")
                    .setMessage("SAS Verification is \"" + sas + "\". Please compare the string with your peer!")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            call.confirmZrtpSas(true);
                        }
                    })
                    .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            call.confirmZrtpSas(false);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show());
        }
    }

    @Override
    public void onCallZrtpSecondaryError(Call call, int channel, ExtendedError error) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printGeneralThreadSafe("onCallZrtpSecondaryError: call= " + call.callHandle() + "; error= " + error.message());
        }
    }

    @Override
    public void onCallSecurityLevelChanged(Call call, CallMediaChannel channel, CallSecurityLevel level)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printGeneralThreadSafe("OnCallSecurityLevelChanged channel: " + channel.toString() + " level: " + level.toString());
        }
    }

    @Override
    public void onVideoStopped(Call call, OriginType origin) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printGeneralThreadSafe("onVideoStopped: call= " + call.callHandle() + "; origin= " + origin.toString());
        }
    }

    @Override
    public void onVideoFormatSelected(Call call, OriginType dir, int width, int height, float fps) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printGeneralThreadSafe("onVideoFormatSelected: call= ${call?.callHandle()}; dir= ${dir?.toString()}; res= ${width}x${height}@${fps}");
        }
    }

    @Override
    public void onVideoStarted(Call call, OriginType origin) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printGeneralThreadSafe("onVideoStarted: call= " + call.callHandle() + "; origin= " + origin.toString());
        }
    }

    @Override
    public void onVideoCameraChanged(Call call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printGeneralThreadSafe("onVideoCameraChanged: call= " + call.callHandle());
        }
    }
}
