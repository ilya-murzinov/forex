package forex

import cats.data.Reader

package object config {

  type ApplicationConfigReader[A] =
    Reader[ApplicationEnvironment, A]

  def configure[A](c: ApplicationEnvironment)(implicit r: ApplicationConfigReader[A]): A = r.run(c)
}
