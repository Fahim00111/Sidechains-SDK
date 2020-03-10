package com.horizen.utils

import java.time.Instant

import com.horizen.block.SidechainBlock
import com.horizen.companion.SidechainTransactionsCompanion
import com.horizen.fixtures.{CompanionsFixture, ForgerBoxFixture, MainchainBlockReferenceFixture, MerkleTreeFixture, VrfGenerator}
import com.horizen.params.NetworkParams
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.scalatest.junit.JUnitSuite
import org.scalatest.mockito.MockitoSugar
import scorex.util.bytesToId


class WithdrawalEpochUtilsTest extends JUnitSuite with MockitoSugar with MainchainBlockReferenceFixture with CompanionsFixture {

  val sidechainTransactionsCompanion: SidechainTransactionsCompanion = getDefaultTransactionsCompanion
  val params: NetworkParams = mock[NetworkParams]
  val withdrawalEpochLength: Int = 100

  @Test
  def getWithdrawalEpochInfo(): Unit = {
    Mockito.when(params.withdrawalEpochLength).thenReturn(withdrawalEpochLength)

    val (forgerBox1, forgerMeta1) = ForgerBoxFixture.generateForgerBox(32)
    // Test 1: block with no mc block references
    var block: SidechainBlock = SidechainBlock.create(
      bytesToId(new Array[Byte](32)),
      Instant.now.getEpochSecond - 10000,
      Seq(), // no mc block refs
      Seq(),
      Seq(),
      Seq(),
      forgerMeta1.rewardSecret,
      forgerBox1,
      VrfGenerator.generateProof(456L),
      MerkleTreeFixture.generateRandomMerklePath(456L),
      sidechainTransactionsCompanion,
      null
    ).get

    assertEquals("Epoch info expected to be the same as previous.",
      WithdrawalEpochInfo(1, 1),
      WithdrawalEpochUtils.getWithdrawalEpochInfo(block, WithdrawalEpochInfo(1, 1), params)
    )

    assertEquals("Epoch info expected to be the changed: epoch switch expected.",
      WithdrawalEpochInfo(2, 0),
      WithdrawalEpochUtils.getWithdrawalEpochInfo(block, WithdrawalEpochInfo(1, withdrawalEpochLength), params)
    )


    // Test 2: block with 1 mc block ref
    val (forgerBox2, forgerMeta2) = ForgerBoxFixture.generateForgerBox(322)

    block = SidechainBlock.create(
      bytesToId(new Array[Byte](32)),
      Instant.now.getEpochSecond - 10000,
      Seq(generateMainchainBlockReference()),
      Seq(),
      Seq(),
      Seq(),
      forgerMeta2.rewardSecret,
      forgerBox2,
      VrfGenerator.generateProof(456L),
      MerkleTreeFixture.generateRandomMerklePath(456L),
      sidechainTransactionsCompanion,
      null
    ).get

    assertEquals("Epoch info expected to be the changed: epoch index should increase.",
      WithdrawalEpochInfo(1, 2),
      WithdrawalEpochUtils.getWithdrawalEpochInfo(block, WithdrawalEpochInfo(1, 1), params)
    )

    assertEquals("Epoch info expected to be the changed: epoch index should increase.",
      WithdrawalEpochInfo(1, withdrawalEpochLength),
      WithdrawalEpochUtils.getWithdrawalEpochInfo(block, WithdrawalEpochInfo(1, withdrawalEpochLength - 1), params)
    )

    assertEquals("Epoch info expected to be the changed: epoch switch expected.",
      WithdrawalEpochInfo(2, 1),
      WithdrawalEpochUtils.getWithdrawalEpochInfo(block, WithdrawalEpochInfo(1, withdrawalEpochLength), params)
    )


    // Test 3: block with 2 mc block ref
    val (forgerBox3, forgerMeta3) = ForgerBoxFixture.generateForgerBox(332)

    block = SidechainBlock.create(
      bytesToId(new Array[Byte](32)),
      Instant.now.getEpochSecond - 10000,
      Seq(generateMainchainBlockReference(), generateMainchainBlockReference()),
      Seq(),
      Seq(),
      Seq(),
      forgerMeta3.rewardSecret,
      forgerBox3,
      VrfGenerator.generateProof(456L),
      MerkleTreeFixture.generateRandomMerklePath(456L),
      sidechainTransactionsCompanion,
      null
    ).get

    assertEquals("Epoch info expected to be the changed: epoch index should increase.",
      WithdrawalEpochInfo(1, 3),
      WithdrawalEpochUtils.getWithdrawalEpochInfo(block, WithdrawalEpochInfo(1, 1), params)
    )

    assertEquals("Epoch info expected to be the changed: epoch index should increase.",
      WithdrawalEpochInfo(1, withdrawalEpochLength),
      WithdrawalEpochUtils.getWithdrawalEpochInfo(block, WithdrawalEpochInfo(1, withdrawalEpochLength - 2), params)
    )

    assertEquals("Epoch info expected to be the changed: epoch switch expected.",
      WithdrawalEpochInfo(2, 1),
      WithdrawalEpochUtils.getWithdrawalEpochInfo(block, WithdrawalEpochInfo(1, withdrawalEpochLength - 1), params)
    )
  }

}
