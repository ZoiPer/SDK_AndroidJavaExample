package com.zoiper.zdk.android.demo.video.out;

import android.media.Image;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * I420Helper
 * <p>
 * A set of various utility methods and algorithms to manipulate YUV420 (i420) byte arrays
 *
 * @since 27/02/2019
 */
@SuppressWarnings("WeakerAccess")
public class I420Helper {
    private static final String TAG = "I420Helper";
    private static final boolean DETAILED_LOG = false;

    // Post-capture editing stuff
    public final ImageDimensions captureDimensions;
    public final ImageDimensions postRotateDimensions;

    private final int timesToRotate;

    private final CorrectionOperation correctionOperation;

    public I420Helper(int sensorOrientation, int deviceOrientation, ImageDimensions captureDimensions) {
        this.captureDimensions = captureDimensions;
        this.timesToRotate = timesToRotateFrame(sensorOrientation, deviceOrientation);
        this.postRotateDimensions = rotaterotateDimensions(captureDimensions, timesToRotate);
        this.correctionOperation = chooseCorrectionOperation();
    }

    /**
     * To avoid running this if for every frame, we choose the logic once here
     * and contain it in a {@link CorrectionOperation} to be used for every frame
     */
    private CorrectionOperation chooseCorrectionOperation(){
        if(timesToRotate == 0){
            return (in) -> mirrorYUV420(in, postRotateDimensions.width, postRotateDimensions.height);
        }
        if(timesToRotate == 3){
            return (in) -> rotateYUV420Degree90(in, captureDimensions.width, captureDimensions.height);
        }
        if(timesToRotate == 1) {
            return (in) -> rotateYUV420Degree270(in, captureDimensions.width, captureDimensions.height);
        }
        if(timesToRotate == 2) {
            return (in) -> {
                in = rotateYUV420Degree180(in, captureDimensions.width, captureDimensions.height);
                return mirrorYUV420(in, postRotateDimensions.width, postRotateDimensions.height);
            };
        }
        return null;
    }

    private static int timesToRotateFrame(int sensorOrientation, int deviceOrientation){
        return ((sensorOrientation+deviceOrientation)/90) % 4;
    }

    private static ImageDimensions rotaterotateDimensions(ImageDimensions dimensions, int timesToRotate){
        return new ImageDimensions(
                (timesToRotate % 2 == 1) ? dimensions.height : dimensions.width,
                (timesToRotate % 2 == 1) ? dimensions.width : dimensions.height
        );
    }

    public byte[] straightenFrame(byte[] i420) {
        return correctionOperation.correct(i420);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static byte[] imageToI420ByteArray(Image image) {
        long started = SystemClock.elapsedRealtime();

        byte[] data = new byte[(int) (image.getWidth() * image.getHeight() * 1.5)];

        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        int offset = 0;
        for (int plane = 0; plane < planes.length; ++plane) {
            final ByteBuffer buffer = planes[plane].getBuffer();
            final int rowStride = planes[plane].getRowStride();
            // Experimentally, U and V planes have |pixelStride| = 2, which
            // essentially means they are packed. That's silly, because we are
            // forced to unpack here.
            final int pixelStride = planes[plane].getPixelStride();
            final int planeWidth = (plane == 0) ? imageWidth : imageWidth / 2;
            final int planeHeight = (plane == 0) ? imageHeight : imageHeight / 2;
            if (pixelStride == 1 && rowStride == planeWidth) {
                // Copy whole plane from buffer into |data| at once.
                buffer.get(data, offset, planeWidth * planeHeight);
                offset += planeWidth * planeHeight;
            } else {
                // Copy pixels one by one respecting pixelStride and rowStride.
                byte[] rowData = new byte[rowStride];
                for (int row = 0; row < planeHeight - 1; ++row) {
                    buffer.get(rowData, 0, rowStride);
                    for (int col = 0; col < planeWidth; ++col) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
                // Last row is special in some devices and may not contain the full
                // |rowStride| bytes of data. See  http://crbug.com/458701  and
                // http://developer.android.com/reference/android/media/Image.Plane.html#getBuffer()
                buffer.get(rowData, 0, Math.min(rowStride, buffer.remaining()));
                for (int col = 0; col < planeWidth; ++col) {
                    data[offset++] = rowData[col * pixelStride];
                }
            }
        }

        if(DETAILED_LOG) Log.d(TAG, "imageToI420ByteArray: " + (SystemClock.elapsedRealtime()-started));
        return data;
    }

    public static byte[] mirrorYUV420(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;

        for (int y = 0; y < imageHeight; y++) {
            for (int x = imageWidth - 1; x >= 0; x--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }

        int offset = imageWidth * imageHeight;
        for (int y = 0; y < (imageHeight/2); y++) {
            for (int x = (imageWidth/2) - 1; x >= 0; x--) {
                yuv[i] = data[y * (imageWidth/2) + x + offset];
                i++;
            }
        }

        offset += imageWidth * imageHeight / 4;
        for (int y = 0; y < (imageHeight/2); y++) {
            for (int x = (imageWidth/2) - 1; x >= 0; x--) {
                yuv[i] = data[y * (imageWidth/2) + x + offset];
                i++;
            }
        }

        return yuv;
    }


    public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];

        // Rotate the Y luma
        int i = 0;

        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        int offset = imageWidth * imageHeight;

        for (int x = 0; x < imageWidth/2; x++) {
            for (int y = (imageHeight/2) - 1; y >= 0; y--) {
                yuv[i] = data[(y * imageWidth/2) + x + offset];
                i++;
            }
        }
        offset += imageWidth * imageHeight / 4;
        for (int x = 0; x < imageWidth/2; x++) {
            for (int y = (imageHeight/2) - 1; y >= 0; y--) {
                yuv[i] = data[(y * imageWidth/2) + x + offset];
                i++;
            }
        }
        return yuv;
    }

    private static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;

        for (int x = imageWidth * imageHeight - 1; x >= 0; x--) {
            yuv[i] = data[x];
            i++;
        }

        int offset = imageWidth * imageHeight;

        for (int x = (imageWidth * imageHeight / 4) - 1; x >= 0; x--) {
            yuv[i] = data[offset + x];
            i++;
        }

        offset += imageWidth * imageHeight / 4;
        for (int x = (imageWidth * imageHeight / 4) - 1; x >= 0; x--) {
            yuv[i] = data[offset + x];
            i++;
        }
        return yuv;
    }

    public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth,
                                               int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];

        // ??Y
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            int nPos = 0;
            for (int y = 0; y < imageHeight; y++) {
                yuv[i] = data[nPos + x];
                i++;
                nPos += imageWidth;
            }
        }

        // U
        int offset = imageWidth * imageHeight;
        for (int x = 0; x < imageWidth/2; x++) {
            int nPos = 0;
            for (int y = 0; y < imageHeight/2; y++) {
                yuv[i] = data[offset + nPos + x];
                i++;
                nPos += imageWidth/2;
            }
        }

        // V
        offset += imageWidth * imageHeight / 4;
        for (int x = 0; x < imageWidth / 2; x++) {
            int nPos = 0;
            for (int y = 0; y < imageHeight/2; y++) {
                yuv[i] = data[offset + nPos + x];
                i++;
                nPos += imageWidth / 2;
            }
        }

        return rotateYUV420Degree180(yuv, imageWidth, imageHeight);
    }

    public static class ImageDimensions {
        public final int width;
        public final int height;

        public ImageDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int I420YUVArraySize(){
            return this.width * this.height + this.width * this.height/2;
        }
    }

    private interface CorrectionOperation{
        byte[] correct(byte[] in);
    }
}
