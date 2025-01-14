package pool

import com.raquo.laminar.api.L._

import org.scalajs.dom._

import pool.component.Container
import pool.dialog.{AccountDialog, LoginDialog, RegisterDialog}
import pool.menu.HomeMenu
import pool.proxy.NowProxy
import pool.view.PoolsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("Client")
class Client(publicUrl: String, apiUrl: String) extends js.Object {
  val context = Context(publicUrl, apiUrl)
  context.log(s"[context] public url: $publicUrl api url: $apiUrl")

  val container = Container(
    HomeMenu(
      context,
      RegisterDialog(context),
      LoginDialog(context),
      AccountDialog(context)
    ),
    PoolsView(context)
  )

  ServiceWorker.register()

  NowProxy.post(s"$publicUrl/now").foreach(now => context.log(s"[now] $now"))

  render(document.getElementById("content"), container)
}