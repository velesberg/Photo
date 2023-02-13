package eti.inTouch.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

final class ImageManager {
    private enum ResizeMode {
        EQUAL_OR_GREATER, EQUAL_OR_LOWER
    }

    private enum ScaleMode {
        EQUAL_OR_GREATER, EQUAL_OR_LOWER
    }

    private Context _ctx;
    private int _boxWidth;
    private int _boxHeight;
    private ResizeMode _resizeMode;
    private ScaleMode _scaleMode;
    private Bitmap.Config _rgbMode;
    private boolean _isScale;
    private boolean _isResize;
    private boolean _isCrop;
    private boolean _isCompress;
    private boolean _isRecycleSrcBitmap;
    private boolean _useOrientation;
    private int _compressRate;

    ImageManager(Context ctx, int boxWidth, int boxHeight, int compressRate) {
        this(ctx);
        _boxWidth = boxWidth;
        _boxHeight = boxHeight;
        _resizeMode = ResizeMode.EQUAL_OR_LOWER;
        _scaleMode = ScaleMode.EQUAL_OR_GREATER;
        _compressRate = compressRate;
    }

    private ImageManager(Context ctx) {
        _ctx = ctx;
        _isScale = false;
        _isResize = false;
        _isCrop = false;
        _isRecycleSrcBitmap = true;
        _useOrientation = true;
    }


    ImageManager setIsScale(boolean isScale) {
        _isScale = isScale;
        return this;
    }

    ImageManager setIsResize(boolean isResize) {
        _isResize = isResize;
        return this;
    }

    ImageManager setIsCrop(boolean isCrop) {
        _isCrop = isCrop;
        return this;
    }


    Bitmap getFromFile(String path) {
        Bitmap bitmap = null;

        try {
            Uri uri = Uri.parse(path);
            int orientation = -1;
            if (_useOrientation) {
                orientation = getOrientation(_ctx, uri);
            }
            bitmap = scale(new StreamFromFile(_ctx, path), orientation);

            return getFromBitmap(bitmap);
        } catch (Exception e) {

            return bitmap;
        }
    }

    private Bitmap getFromBitmap(Bitmap bitmap) {
        if (bitmap == null)
            return null;

        try {
            if (_isResize) bitmap = resize(bitmap);
            if (_isCrop) bitmap = crop(bitmap);
            if (_isCompress) bitmap = compress(bitmap);
            return bitmap;
        } catch (Exception e) {

            return bitmap;
        }
    }

    private Bitmap compress(Bitmap scaledBitmap) {
        if (scaledBitmap == null)
            return null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, _compressRate, output);

            recycleBitmap(scaledBitmap);
        } catch (Exception e) {
        }

        byte[] rawImage = output.toByteArray();

        if (rawImage == null) {
            return null;
        }

        return BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
    }


    /*public byte[] getRawFromFile(String path) {
        return getRawFromFile(path, 75);
    }*/

    byte[] getRawFromFile(Bitmap scaledBitmap, int compressRate) {
        if (scaledBitmap == null) return null;

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            boolean compress = scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compressRate, output);

            if (compress)
                recycleBitmap(scaledBitmap);
            else

            return output.toByteArray();
        }
        catch (Exception e){
            return null;
        }
        return new byte[0];
    }


    @SuppressLint("NewApi")
    private Bitmap scale(IStreamGetter streamGetter, int orientation) {
        try {
            InputStream in = streamGetter.Get();
            if (in == null) return null;

            Bitmap bitmap;
            Bitmap.Config rgbMode = _rgbMode != null ? _rgbMode : Bitmap.Config.RGB_565;

            if (!_isScale) {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inPreferredConfig = rgbMode;
                if (11 <= android.os.Build.VERSION.SDK_INT) {
                    o.inMutable = true;
                }
                bitmap = BitmapFactory.decodeStream(in, null, o);
                in.close();
                return bitmap;
            }

            if (_boxWidth == 0 || _boxHeight == 0) {

                in.close();
                return null;
            }

            ScaleMode scaleMode = _scaleMode != null ? _scaleMode : ScaleMode.EQUAL_OR_GREATER;
            int bytesPerPixel = rgbMode == Bitmap.Config.ARGB_8888 ? 4 : 2;
            int maxSize = _boxWidth * _boxWidth * bytesPerPixel;
            int desiredSize = _boxWidth * _boxHeight * bytesPerPixel;
            if (desiredSize < maxSize) maxSize = desiredSize;

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            int scale = 1;

            int origWidth;
            int origHeight;
            if (orientation == 90 || orientation == 270) {
                origWidth = o.outHeight;
                origHeight = o.outWidth;
            } else {
                origWidth = o.outWidth;
                origHeight = o.outHeight;
            }

            while ((origWidth * origHeight * bytesPerPixel) * (1 / Math.pow(scale, 2)) > maxSize) {
                scale++;
            }
            if (scaleMode == ScaleMode.EQUAL_OR_LOWER) {
                scale++;
            }

            if (scale > 3)
                scale = 3;

            o = new BitmapFactory.Options();
            o.inSampleSize = scale;
            o.inPreferredConfig = rgbMode;

            in = streamGetter.Get();
            if (in == null) return null;
            bitmap = BitmapFactory.decodeStream(in, null, o);
            in.close();

            if (orientation > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                Bitmap decodedBitmap = bitmap;

                assert decodedBitmap != null;
                bitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (!decodedBitmap.equals(bitmap)) {
                    recycleBitmap(decodedBitmap);
                }
            }

            return bitmap;
        }
        catch (Exception e) {
            return null;
        }
    }

    private Bitmap resize(Bitmap sourceBitmap) {
        if (sourceBitmap == null) return null;
        if (_resizeMode == null) _resizeMode = ResizeMode.EQUAL_OR_GREATER;
        float srcRatio;
        int srcWidth;
        int srcHeight;
        int resizedWidth;
        int resizedHeight;

        Bitmap resizedBitmap = null;
        try {
            srcWidth = sourceBitmap.getWidth();
            srcHeight = sourceBitmap.getHeight();

            srcRatio = (float) srcWidth / (float) srcHeight;
            if (srcWidth > srcHeight) {
                resizedWidth = _boxWidth;
                resizedHeight = (int) ((float) resizedWidth / srcRatio);
            } else {
                resizedHeight = _boxHeight;
                resizedWidth = (int) ((float) resizedHeight * srcRatio);
            }

            resizedBitmap = Bitmap.createScaledBitmap(sourceBitmap, resizedWidth, resizedHeight, true);

            if (_isRecycleSrcBitmap && !sourceBitmap.equals(resizedBitmap)) {
                recycleBitmap(sourceBitmap);
            }
        } catch (Exception e) {
        }
        return resizedBitmap;
    }

    private Bitmap crop(Bitmap sourceBitmap) {
        if (sourceBitmap == null) return null;
        int srcWidth = sourceBitmap.getWidth();
        int srcHeight = sourceBitmap.getHeight();
        int croppedX;
        int croppedY;

        Bitmap croppedBitmap = null;
        try {
            croppedX = (srcWidth > _boxWidth) ? (srcWidth - _boxWidth) / 2 : 0;
            croppedY = (srcHeight > _boxHeight) ? (srcHeight - _boxHeight) / 2 : 0;

            if (croppedX == 0 && croppedY == 0)
                return sourceBitmap;

            try {
                croppedBitmap = Bitmap.createBitmap(sourceBitmap, croppedX, croppedY, _boxWidth, _boxHeight);
            } catch (Exception e) {
            }
            if (_isRecycleSrcBitmap && !sourceBitmap.equals(croppedBitmap)) {
                recycleBitmap(sourceBitmap);
            }
        } catch (Exception e) {
        }

        if (croppedBitmap == null)
            return sourceBitmap;
        return croppedBitmap;
    }

    private static void recycleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return;

        try {
            bitmap.recycle();
            System.gc();
        } catch (Exception e) {
        }
    }

    private interface IStreamGetter {
        InputStream Get();
    }

    private static class StreamFromFile implements IStreamGetter {
        private String _path;
        private Context _ctx;
        StreamFromFile(Context ctx, String path) {
            _path = path;
            _ctx = ctx;
        }
        @SuppressWarnings("resource")
        public InputStream Get() {
            try {
                Uri uri = Uri.parse(_path);
                return "content".equals(uri.getScheme())
                        ? _ctx.getContentResolver().openInputStream(uri)
                        : new FileInputStream(_path);
            }
            catch (FileNotFoundException e) {

                return null;
            }
        }
    }


    private static int getOrientation(Context context, Uri uri) {
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

            if (cursor == null || cursor.getCount() != 1) {
                return -1;
            }

            cursor.moveToFirst();
            int orientation = cursor.getInt(0);
            cursor.close();
            return orientation;
        }
        else {
            try {
                ExifInterface exif = new ExifInterface(uri.getPath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        return 270;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        return 180;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        return 90;
                    case ExifInterface.ORIENTATION_NORMAL:
                        return 0;
                    default:
                        return -1;
                }
            } catch (IOException e) {
                return -1;
            }
        }
    }
}