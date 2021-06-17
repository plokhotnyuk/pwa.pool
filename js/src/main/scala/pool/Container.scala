package pool

import com.raquo.laminar.api.L._

object Container {
  val id = "container"

  def apply(commandMenu: Div, poolsView: Div): Div =
    div( idAttr(id), cls("w3-container"),
      commandMenu,
      poolsView
    )
}