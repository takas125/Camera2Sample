package com.takas125.camera2sample.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.takas125.camera2sample.R
import com.takas125.camera2sample.utils.getPreviewOutputSize
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CameraFragment : Fragment() {

    private val cameraManager: CameraManager by lazy {
        val context = requireContext().applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private lateinit var characteristics: CameraCharacteristics

    private var cameraDevice: CameraDevice? = null

    private var captureSession: CameraCaptureSession? = null

    private lateinit var textureView: TextureView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        return inflater.inflate(R.layout.fragment_camera, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textureView = view_camera
        characteristics = cameraManager.getCameraCharacteristics("0")
        // TextureViewが使えるようになったらカメラ起動
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(texture: SurfaceTexture?, p1: Int, p2: Int) {
                    openCamera()
                }

                override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture?, p1: Int, p2: Int) {}
                override fun onSurfaceTextureUpdated(texture: SurfaceTexture?) {}
                override fun onSurfaceTextureDestroyed(texture: SurfaceTexture?): Boolean = true
            }
        }

        shutter_button.setOnClickListener{

            try {
                var savefile : File = createFile(requireContext(), "jpg")
                /**
                 * プレビューの更新を止める
                 */
                captureSession?.stopRepeating()
                if (textureView.isAvailable) {

                    val fos = FileOutputStream(savefile)
                    val bitmap: Bitmap = textureView.bitmap
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.close()
                }

                if (savefile != null) {
                    Toast.makeText(context, "Saved: $savefile", Toast.LENGTH_SHORT).show()
                }

            } catch (e: CameraAccessException) {
                Log.d("edulog", "CameraAccessException_Error: $e")
            } catch (e: FileNotFoundException) {
                Log.d("edulog", "FileNotFoundException_Error: $e")
            } catch (e: IOException) {
                Log.d("edulog", "IOException_Error: $e")
            }

            /**
             * プレビューを再開
             */
            createCameraPreviewSession()

        }
    }

    private fun openCamera() {
        cameraManager.openCamera("0", object: CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createCameraPreviewSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                cameraDevice?.close()
                cameraDevice = null
            }

            override fun onError(camera: CameraDevice, p1: Int) {
                cameraDevice?.close()
                cameraDevice = null
            }
        }, null)
    }

    private fun createCameraPreviewSession() {
        if (cameraDevice == null) {
            return
        }
        val texture = textureView.surfaceTexture
        // ビューサイズの指定
        val previewSize = getPreviewOutputSize(
            textureView.display, characteristics, SurfaceHolder::class.java)
        texture.setDefaultBufferSize(previewSize.width, previewSize.height)
        val surface = Surface(texture)

        val previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder.addTarget(surface)

        cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                captureSession?.setRepeatingRequest(previewRequestBuilder.build(), null, null)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }, null)
    }

    companion object{
        private fun createFile(context: Context, extension: String): File {
            val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.JAPAN)
            return File(getOutputDirectory(context), "IMG_${sdf.format(Date())}.$extension")
        }

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }
}