package uz.smd.mprinter.utils

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions

/**
 * Created by Siddikov Mukhriddin on 5/20/22
 */
fun Fragment.checkPermission(permission: String, granted: () -> Unit) {
    val mContext = context ?: return
    val options = Permissions.Options()
    options.setSettingsDialogMessage("Вы навсегда запретили приложению доступ к геопозиции! Разрешите приложению использовать ваши геоданные в настройках!")
    options.setSettingsText("Настройки")
    options.setSettingsDialogTitle("Требуются разрешения")
    options.setCreateNewTask(true)
    Permissions.check(mContext, arrayOf(permission), null, options, object : PermissionHandler() {
        override fun onGranted() {
            granted()
        }
    })
}

fun checkPermission(context: Context, permission: String, granted: () -> Unit, denied: () -> Unit) {
    val mContext = context ?: return
    val options =  Permissions.Options()
//    val options = Permissions.Options()
    options.setSettingsDialogMessage("Вы навсегда запретили приложению доступ к хранилишу! Разрешите приложению использовать ваши Файлы и медиа в настройках!")
    options.setSettingsText("Настройки")
    options.setSettingsDialogTitle("Требуются разрешения")
    options.setCreateNewTask(true)
    Permissions.check(mContext, arrayOf(permission), null, options, object : PermissionHandler() {
        override fun onGranted() {
            granted()
        }

        override fun onDenied(context: Context?, deniedPermissions: java.util.ArrayList<String>?) {
            denied()
        }

        override fun onBlocked(
            context: Context?,
            blockedList: java.util.ArrayList<String>?
        ): Boolean {
            denied()
            return super.onBlocked(context, blockedList)
        }

    })
}


fun Fragment.checkPermissionPhoto(permission: String, granted: () -> Unit) {
    val mContext = context ?: return
    val options =  Permissions.Options()
//    val options = Permissions.Options()
    options.setSettingsDialogMessage("Вы навсегда запретили приложению доступ к хранилишу! Разрешите приложению использовать ваши Файлы и медиа в настройках!")
    options.setSettingsText("Настройки")
    options.setSettingsDialogTitle("Требуются разрешения")
    options.setCreateNewTask(true)
    Permissions.check(mContext, arrayOf(permission), null, options, object : PermissionHandler() {
        override fun onGranted() {
            granted()
        }

    })
}
fun Activity.checkPermissionPhoto(permission: String, granted: () -> Unit) {
    val options =  Permissions.Options()
//    val options = Permissions.Options()
    options.setSettingsDialogMessage("Вы навсегда запретили приложению доступ к хранилишу! Разрешите приложению использовать ваши Файлы и медиа в настройках!")
    options.setSettingsText("Настройки")
    options.setSettingsDialogTitle("Требуются разрешения")
    options.setCreateNewTask(true)
    Permissions.check(this, arrayOf(permission), null, options, object : PermissionHandler() {
        override fun onGranted() {
            granted()
        }

    })
}