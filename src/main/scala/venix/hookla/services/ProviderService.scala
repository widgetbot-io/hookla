package venix.hookla.services

import javax.inject.Inject
import venix.hookla.providers.BaseProvider
import venix.hookla.providers.gitlab.GitlabProvider

class ProviderService @Inject()(
   gitlabProvider: GitlabProvider
) {
  private def providers: List[BaseProvider] = gitlabProvider :: Nil

  def getById(id: String): Option[BaseProvider] = providers.find(p => p.id == id)
}
