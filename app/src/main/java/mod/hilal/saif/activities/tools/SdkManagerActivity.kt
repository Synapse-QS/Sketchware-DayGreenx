package mod.hilal.saif.activities.tools

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import com.besome.sketch.editor.manage.library.LibraryItemView
import com.besome.sketch.lib.base.BaseAppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.chrisbanes.insetter.Insetter
import dev.pranav.filepicker.FilePickerCallback
import dev.pranav.filepicker.FilePickerDialogFragment
import dev.pranav.filepicker.FilePickerOptions
import dev.pranav.filepicker.SelectionMode
import org.sketchware.daygreen.DownloadUtility
import org.sketchware.daygreen.FileCheckUtils
import pro.sketchware.R
import pro.sketchware.databinding.ActivityGenericListBinding
import java.io.File

class SdkManagerActivity : BaseAppCompatActivity() {
    private lateinit var binding: ActivityGenericListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeNoContrast()
        super.onCreate(savedInstanceState)
        binding = ActivityGenericListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Insetter.builder()
            .padding(WindowInsetsCompat.Type.statusBars())
            .applyToView(binding.toolbar)
            
        Insetter.builder()
            .padding(WindowInsetsCompat.Type.navigationBars())
            .applyToView(binding.listContainer)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "SDK Manager"
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupItems()
    }

    private fun setupItems() {
        val abi = DownloadUtility.getDeviceAbi()
        binding.listContainer.removeAllViews()

        for (api in 26..37) {
            val destination = File(filesDir, "libs/android-$api.jar")
            addToolCard(
                title = "Android SDK $api",
                description = "Download android.jar for API $api",
                isInstalled = destination.exists() && destination.length() > 0,
                size = FileCheckUtils.getSdkVersionSize(this, api.toString()),
                url = "https://github.com/gus23-okta/sketchware-daygreen-build-tools/releases/download/37/android-$api.jar",
                destination = destination
            )
        }
    }

    private fun addToolCard(
        title: String,
        description: String,
        isInstalled: Boolean,
        size: String,
        url: String,
        destination: File
    ) {
        val cardView = layoutInflater.inflate(R.layout.item_download_card, binding.listContainer, false)
        
        val tvTitle = cardView.findViewById<TextView>(R.id.title)
        val tvDesc = cardView.findViewById<TextView>(R.id.description)
        val tvStatusChip = cardView.findViewById<TextView>(R.id.status_chip)
        val tvStatusText = cardView.findViewById<TextView>(R.id.status_text)
        val btnImport = cardView.findViewById<View>(R.id.btn_import)
        val btnDownload = cardView.findViewById<View>(R.id.btn_download)
        val imgIcon = cardView.findViewById<ImageView>(R.id.icon)

        tvTitle.text = title
        tvDesc.text = description
        tvStatusChip.text = if (isInstalled) "Installed" else "Not Installed"
        tvStatusChip.alpha = if (isInstalled) 1.0f else 0.6f
        
        tvStatusText.text = "Status: ${if (isInstalled) "Installed" else "Not installed"} ($size ${if (isInstalled) "used" else "download"})"
        
        val downloadBtn = btnDownload as MaterialButton
        if (isInstalled) {
            downloadBtn.text = "Remove"
            downloadBtn.setIconResource(R.drawable.ic_mtrl_delete)
            downloadBtn.setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Remove SDK")
                    .setMessage("Are you sure you want to remove $title?")
                    .setPositiveButton("Remove") { _, _ ->
                        if (destination.exists()) {
                            destination.delete()
                        }
                        setupItems()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            downloadBtn.text = "Download"
            downloadBtn.setIconResource(R.drawable.ic_mtrl_download)
            downloadBtn.setOnClickListener {
                DownloadUtility.downloadFile(this, url, destination) {
                    setupItems()
                }
            }
        }

        btnImport.setOnClickListener {
            importArchive(destination)
        }

        imgIcon.setImageResource(R.drawable.ic_mtrl_android)
        imgIcon.setColorFilter(MaterialColors.getColor(this, R.attr.colorPrimary, Color.BLACK))

        binding.listContainer.addView(cardView)
    }

    private fun importArchive(destination: File) {
        val options = FilePickerOptions().apply {
            selectionMode = SelectionMode.FILE
            extensions = arrayOf("zip", "tar.xz", "jar")
        }
        
        val callback = object : FilePickerCallback() {
            override fun onFileSelected(file: File) {
                destination.parentFile?.mkdirs()
                file.copyTo(destination, overwrite = true)
                Toast.makeText(this@SdkManagerActivity, "Imported ${file.name}", Toast.LENGTH_SHORT).show()
                setupItems()
            }
        }
        
        FilePickerDialogFragment(options, callback)
            .show(supportFragmentManager, "file_picker")
    }
}
