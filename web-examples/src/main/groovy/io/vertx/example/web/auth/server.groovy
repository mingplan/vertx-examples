import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.CookieHandler
import io.vertx.groovy.ext.web.handler.SessionHandler
import io.vertx.groovy.ext.web.sstore.LocalSessionStore
import io.vertx.groovy.ext.web.handler.BodyHandler
import io.vertx.groovy.ext.auth.shiro.ShiroAuth
import io.vertx.ext.auth.shiro.ShiroAuthRealmType
import io.vertx.groovy.ext.web.handler.RedirectAuthHandler
import io.vertx.groovy.ext.web.handler.StaticHandler
import io.vertx.groovy.ext.web.handler.FormLoginHandler

def router = Router.router(vertx)

// We need cookies, sessions and request bodies
router.route().handler(CookieHandler.create())
router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)))
router.route().handler(BodyHandler.create())

// Simple auth service which uses a properties file for user/role info
def authProvider = ShiroAuth.create(vertx, ShiroAuthRealmType.PROPERTIES, [:])

// Any requests to URI starting '/private/' require login
router.route("/private/*").handler(RedirectAuthHandler.create(authProvider, "/loginpage.html"))

// Serve the static private pages from directory 'private'
router.route("/private/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("private"))

// Handles the actual login
router.route("/loginhandler").handler(FormLoginHandler.create(authProvider))

// Implement logout
router.route("/logout").handler({ context ->
  context.setUser(null)
  // Redirect back to the index page
  context.response().putHeader("location", "/").setStatusCode(302).end()
})

// Serve the non private static pages
router.route().handler(StaticHandler.create())

vertx.createHttpServer().requestHandler(router.&accept).listen(8080)
