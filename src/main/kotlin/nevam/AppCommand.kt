@file:JvmName("App")

package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
  AppCommand()
      .subcommands(ReleaseCommand())
      .main(args)
}

class AppCommand : CliktCommand(name = "startship") {
  override fun run() {
    // Kudos to https://asciiart.website for this piece of art.
    echo(
        """
        |      ______ 
        |     |''   ``\                 
        |     |        \                         /b.
        |     |         \  ,,,---===?A`\     ,==y'
        |   ___,,,,,---==""\        |M] \   ;|\ |>
        |           _   _   \   ___,|H,,---==''''bno,
        |    o  O  (_) (_)   \ /          _     AWAW/
        |                     /         _(+)_  dMM/
        |      \@_,,,,,,---=="   \      \\|//  MW/
        |--''''"                         ===  d/
        |                                    //
        |                                    ,'__________________________
        |   \    \    \     \               ,/~~~~~~~~~~~~~~~~~~~~~~~~~~~
        |                         _____    ,'  ~~~   .-""-.~~~~~~  .-""-.
        |      .-""-.           ///==---   /`-._ ..-'      -.__..-'
        |            `-.__..-' =====\\\\\\ V/  .---\.
        |                      ~~~~~~~~~~~~, _',--/_.\  .-""-.
        |                            .-""-.___` --  \|         -.__..-
        |
        """.trimMargin()
    )
  }
}
