package utils.auth


import com.mohiva.play.silhouette.api.Identity

case class User(username: String) extends Identity
