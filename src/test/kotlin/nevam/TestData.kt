package nevam

import nevam.nexus.StagingProfileRepository

val StagingProfileRepository.Companion.FAKE: StagingProfileRepository
  get() = StagingProfileRepository(
      id = "cagenicolas_1206",
      profileName = "cage.nicolas",
      type = "closed",
      isTransitioning = false,
      updatedDate = "Sometime",
      profileId = "9000"
  )
