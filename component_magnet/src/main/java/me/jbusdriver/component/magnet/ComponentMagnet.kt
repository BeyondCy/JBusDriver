package me.jbusdriver.component.magnet

import android.annotation.SuppressLint
import android.app.Activity
import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IComponent
import com.tbruyelle.rxpermissions2.RxPermissions
import com.umeng.analytics.pro.cc
import com.wlqq.phantom.library.PhantomCore
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.C
import me.jbusdriver.common.bean.plugin.IPluginComponent
import me.jbusdriver.common.bean.plugin.toPluginBean
import me.jbusdriver.component.magnet.ui.activity.MagnetPagerListActivity
import me.jbusdriver.component.magnet.ui.config.Configuration
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.jar.Manifest

class ComponentMagnet : IComponent, IPluginComponent {

    init {
        MagnetPluginHelper.init()
    }


    override fun getName() = C.Components.Manget

    override fun onCall(cc: CC): Boolean {
        val actionName = cc.actionName
        try {
            when (actionName) {
                "show" -> {
                    val keyWord = cc.getParamItem<String>("keyword")
                            ?: error("show activity must pass keyword")
                    MagnetPagerListActivity.start(cc, keyWord)
                    CC.sendCCResult(cc.callId, CCResult.success())
                }
                "allKeys" -> {
                    CC.sendCCResult(cc.callId, CCResult.success(mapOf("keys" to MagnetPluginHelper.getLoaderKeys())))
                }
                "config.save" -> {
                    val keys = cc.getParamItem<List<String>>("keys")
                            ?: error("call config.save must past keys")
                    Configuration.saveMagnetKeys(keys)
                    CC.sendCCResult(cc.callId, CCResult.success())

                }
                "config.getKeys" -> {
                    CC.sendCCResult(cc.callId, CCResult.success(mapOf("keys" to Configuration.getConfigKeys())))
                }
                "plugins.all" -> {
                    getAllPlugin(cc)
                }
                "plugins.install" -> {
                    val pluginPath = cc.getParamItem<String>("path")
                            ?: error("must past apk's path")
                    installPlugin(cc, pluginPath)
                    return true
                }
                else -> {
                    CC.sendCCResult(cc.callId, CCResult.error("not config action $actionName for $cc"))
                }

            }
        } catch (e: Exception) {
            KLog.w("$cc call error $e")
            CC.sendCCResult(cc.callId, CCResult.error(e.message))
        }

        return false
    }

    override fun getAllPlugin(cc: CC) {
        CC.sendCCResult(cc.callId, CCResult.success(mapOf("plugins" to PhantomCore.getInstance().allPlugins.map { it.toPluginBean() })))
    }

    @SuppressLint("CheckResult")
    override fun installPlugin(cc: CC, path: String) {
        MagnetPluginHelper.installApkFile(File(path)).timeout(10, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS)
                .subscribe({
                    CC.sendCCResult(cc.callId, CCResult.success("plugin", it.toPluginBean()))
                }, {
                    CC.sendCCResult(cc.callId, CCResult.error("install error $it"))
                })
    }

}