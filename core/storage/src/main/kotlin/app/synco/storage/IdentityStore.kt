package app.synco.storage

import app.synco.crypto.IdentityKeyPair

interface IdentityStore {

    suspend fun identity(): IdentityKeyPair

    suspend fun replaceWithFreshIdentity(): IdentityKeyPair
}
