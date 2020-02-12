package com.takas125.camera2sample.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.takas125.camera2sample.R
import com.takas125.camera2sample.utils.Constants
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    companion object{
        private val REQUEST_CODE_CAMERA = 10
        private val REQUEST_CODE_STORAGE = 1000
    }
    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    private val STORAGE_PERMISSIONS = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        camera_open_button.setOnClickListener {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_CAMERA)
            requestPermissions(STORAGE_PERMISSIONS, REQUEST_CODE_STORAGE)
            if (allPermissionsGranted()) {
                val transaction = fragmentManager?.beginTransaction()
                val fragment = CameraFragment()
                val args = Bundle()
                args.putString("CAMERA_ID", "0")
                fragment.arguments = args

                transaction?.replace(R.id.fragment_container, fragment)
                transaction?.addToBackStack(null)
                transaction?.commit()
            }

        }
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == Constants.REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.fragment_container, CameraFragment())
                transaction?.addToBackStack(null)
                transaction?.commit()
            } else {
                Toast.makeText(context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Check camera permission is granted
    private fun allPermissionsGranted() = context?.let {context ->
        REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            context, it) == PackageManager.PERMISSION_GRANTED
        }
    }?: false
}