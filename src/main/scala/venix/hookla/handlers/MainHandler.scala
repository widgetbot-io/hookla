package venix.hookla.handlers

import venix.hookla.types.Provider

class MainHandler {
  val gitlabProvider = Provider(
    "github",
    "GitHub",
    "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png",
    eventHeader = Some("X-GitHub-Event"),
  )
}
