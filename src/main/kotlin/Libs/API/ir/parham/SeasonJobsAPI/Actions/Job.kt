package ir.parham.SeasonJobsAPI.Actions

import Libs.API.ir.parham.SeasonJobsAPI.DriverManager.Config
import Libs.API.ir.parham.SeasonJobsAPI.Dependencies.Luckperms
import Libs.API.ir.parham.SeasonJobsAPI.Event.Job.JobCreateEvent
import Libs.API.ir.parham.SeasonJobsAPI.Event.Job.JobDeleteEvent
import Libs.API.ir.parham.SeasonJobsAPI.Event.Job.JobEditEvent
import Libs.API.ir.parham.SeasonJobsAPI.Event.Job.JobEvent
import Libs.API.ir.parham.SeasonJobsAPI.Event.SeasonEventManager
import Libs.API.ir.parham.SeasonJobsAPI.Senders.Logger
import ir.parham.SeasonJobsAPI.DriverManager.Configs
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import java.util.UUID

open class Job {
    val luckperms = Luckperms()
    val logger = Logger()


    class Jobs(val Name: String,val MaxWarn: Int, val MemberSize: Int, val PlayTime : Int, val Prefix : String, val Suffix : String)
    {
        init
        {
            val jobEvent = JobEvent(this)
            if (!jobEvent.isCancelled())
            {
                jobList.add(Name)
                jobs.put(Name, this)
            }
        }
    }
    companion object
    {
        var jobList = ArrayList<String>()
        var jobs : HashMap<String,Jobs> = HashMap()
    }

    fun getJobMembers(job: String) : List<UUID>
    {
        return Member().getJobMembers(job)
    }
    fun getJobMembersSize(job: String) : Int
    {
        return getJobMembers(job).size
    }
    fun create(name: String, maxWarn: Int, memberSize: Int, playTime: Int, prefix : String, suffix : String) : Boolean
    {
        try {
            if (!contains(name))
            {
                Jobs(name, maxWarn, memberSize, playTime, prefix, suffix)
                val jobCreateEvent = JobCreateEvent(get(name)!!)
                SeasonEventManager().fireEvent(jobCreateEvent)

                if (!jobCreateEvent.isCancelled())
                {
                    jobs.remove(name)
                    jobList.remove(name)

                    Jobs(
                        jobCreateEvent.getName(),
                        jobCreateEvent.getMaxWarn(),
                        jobCreateEvent.getMemberSize(),
                        jobCreateEvent.getPlayTime(),
                        jobCreateEvent.getPrefix(),
                        jobCreateEvent.getSuffix()
                    )
                    luckperms.createGroup(jobCreateEvent.getName())
                    return true
                }

                jobs.remove(name)
                jobList.remove(name)
                return false
            } else
            {
                return false
            }
        } catch (e : Exception) {
            return false
        }
    }
    fun set(name : String, maxWarn: Int, memberSize : Int, playTime : Int, prefix : String, suffix : String) : Boolean
    {
        try {
            if (contains(name))
            {
                Jobs(name, maxWarn, memberSize, playTime, prefix, suffix)
                val jobSetEvent = JobEditEvent(get(name)!!)
                SeasonEventManager().fireEvent(jobSetEvent)
                if (!jobSetEvent.isCancelled())
                {
                    jobs.remove(name)
                    jobList.remove(name)
                    Jobs(
                        jobSetEvent.getName(),
                        jobSetEvent.getMaxWarn(),
                        jobSetEvent.getMemberSize(),
                        jobSetEvent.getPlayTime(),
                        jobSetEvent.getPrefix(),
                        jobSetEvent.getSuffix()
                    )
                    luckperms.createGroup(name)
                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }
    fun list() : List<String>
    {
        return jobList;
    }

    fun contains(name: String): Boolean
    {
        var jobs: String = jobList.toString();
        jobs = jobs.replace("[", "")
        jobs = jobs.replace("]", "")
        jobs = jobs.replace(",", " ")
        if (jobs.contains(name))
        {
            return true
        }

        return false
    }

    fun get(name: String) : Jobs?
    {
        return jobs.get(name)
    }

    fun remove(name: String) : Boolean
    {
        try {
            if (contains(name))
            {
                val jobDeleteEvent = JobDeleteEvent(get(name)!!)
                SeasonEventManager().fireEvent(jobDeleteEvent)

                if (!jobDeleteEvent.isCancelled())
                {
                    jobs.remove(name)
                    jobList.remove(name)
                    luckperms.deleteGroup(name)

                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    fun save(name: String) : Boolean
    {
        if (jobs.containsKey(name))
        {
            try
            {
                Config().recreate(Configs.JOBS)
                var jobsConfig: FileConfiguration = Config().get(Configs.JOBS)!!

                for (key in jobList)
                {
                    jobsConfig.createSection(key)

                    val section: ConfigurationSection = jobsConfig.getConfigurationSection(key)!!

                    section.createSection("prefix")
                    section.createSection("suffix")
                    section.createSection("members")
                    section.createSection("playtime")
                    section.createSection("maxWarn")
                    section.set("prefix", get(key)?.Prefix)
                    section.set("suffix", get(key)?.Suffix)
                    section.set("members", get(key)?.MemberSize)
                    section.set("playtime", get(key)?.PlayTime)
                    section.set("maxWarn", get(key)?.MaxWarn)
                }

                Config().save(Configs.JOBS, jobsConfig)
                return true
            } catch (e : Exception)
            {
                logger.log("Exception while saving job ($name)")
                return false
            }
        }
        return false
    }

    @Synchronized
    fun loadAll()
    {
        val jobsConfig : FileConfiguration = Config().get(Configs.JOBS)!!

        for (key in jobsConfig.getKeys(false))
        {
            try
            {
                load(key)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    fun load(name: String) : Boolean
    {
        try
        {
            val jobsConfig: FileConfiguration = Config().get(Configs.JOBS)!!

            val jobSection: ConfigurationSection? = jobsConfig.getConfigurationSection(name)

            if (jobSection != null)
            {
                create(
                    jobSection.name,
                    jobSection.getInt("maxWarn"),
                    jobSection.getInt("members"),
                    jobSection.getInt("playtime"),
                    jobSection.getString("prefix").toString(),
                    jobSection.getString("suffix").toString()
                )
            }
            return true
        }
        catch (e : Exception)
        {
            logger.log("Exception while loading job ($name)")
            return false
        }
    }


    @Synchronized
    // sync task
    fun saveAll()
    {
        for (key in jobList)
        {
            save(key)
        }
    }
}