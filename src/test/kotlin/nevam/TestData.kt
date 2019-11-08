package nevam

import nevam.nexus.StagingProfileRepository

val StagingProfileRepository.Companion.FAKE: StagingProfileRepository
  get() = StagingProfileRepository(
      id = "cagenicolas_1206",
      type = "closed",
      isTransitioning = false,
      updatedDate = "Sometime",
      profileId = "9000",
      profileName = "cage.nicolas"
  )
