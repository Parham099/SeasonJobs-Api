package ir.parham.SeasonJobsAPI

import Config
import Libs.API.ir.parham.SeasonJobsAPI.Dependencies.Depends
import Libs.API.ir.parham.SeasonJobsAPI.Senders.Logger
import Libs.API.ir.parham.SeasonJobsAPI.Senders.Message
import ir.parham.SeasonJobsAPI.Actions.Job
import ir.parham.SeasonJobsAPI.Actions.Member
import ir.parham.SeasonJobsAPI.DriverManager.Configs
import ir.parham.seasonJob.SeasonJob
import ir.parham.seasonJob.Updater
import org.bukkit.plugin.Plugin
import java.io.File

class LoadJobAPI {
    companion object
    {
        var PluginFolder : File? = null
        var Instance : Plugin? = null
    }

    fun LoadJobAPI() : Boolean {
        PluginFolder = SeasonJob.instance?.dataFolder
        Instance = SeasonJob.instance

        if (PluginFolder != null && Instance != null)
        {
            val logger : Logger = Logger()
            logger.log("&6&l&n==============================SeasonJobs==============================")
            logger.log("&6Wiki&7: &ahttps://github.com/Parham099/SeasonJobs-Api")
            logger.log("")
            Config().load(Configs.CONFIG)
            Depends().check()
            logger.log("&6message.yml &eIs Generation &7...")
            Config().load(Configs.MESSAGE)
            Message().loadAll()
            logger.log("&2message.yml &aGenerated &7!")
            logger.log("&6data.yml &eIs Generation &7...")
            Config().load(Configs.DATA)
            logger.log("&2data.yml &aGenerated &7!")
            logger.log("&6jobs.yml &eIs Generation &7...")
            Config().load(Configs.JOBS)
            logger.log("&2jobs.yml &aGenerated &7!")

            logger.log("&2Jobs &aAre Loading &7...")
            Job().loadAll()
            logger.log("&2${Job().list()} &aAre Loaded.")
            logger.log("&2Users &aAre Loading &7...")
            Member().loadAll()
            logger.log("&2Users &aAre Loaded.")

            Updater.start()
            logger.log(" ")
            logger.log("&2Plugin loading is finished.")
            logger.log("&6&l&n==============================SeasonJobs==============================")
            return true
        }
        else
        {
            println("&cPlugin Disabled &4Instance &cOr &4PluginFolder &cIs Null.")
            return false
        }
    }
}