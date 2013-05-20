package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import js.jquery.JQueryArtifacts
import sitemap._
import Loc._
import mapper._

import code.model._
import net.liftmodules.JQueryModule
import net.liftweb.http.auth.{userRoles, AuthRole, HttpBasicAuthentication}


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    def authenticates: LiftRules.HttpAuthProtectedResourcePF = {
      case Req(List("user", "login"), _, GetRequest) => Full(AuthRole("user"))
    }

    LiftRules.httpAuthProtectedResource.append(authenticates)

    LiftRules.authentication = HttpBasicAuthentication("PocketChange") {
      case (userEmail, password, protection) => {
        User.login(userEmail, password) match {// this is a Box
          case Full(x) =>
            println("reached");
            userRoles(List(AuthRole("user")))
            true
          case _ => false
        }
      }
    }

    // where to search snippet
    LiftRules.addToPackages("code")

    // Build SiteMap
    def sitemap = SiteMap(
      Menu("Login Required") / "user" / "login",
      Menu.i("Home") / "index")

    def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery172
    JQueryModule.init()

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    


  }
}
