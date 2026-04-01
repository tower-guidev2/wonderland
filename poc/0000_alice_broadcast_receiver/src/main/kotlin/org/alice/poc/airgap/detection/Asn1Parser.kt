@file:Suppress("SpellCheckingInspection")

package org.alice.poc.airgap.detection

class KeyDescription(
    val attestationSecurityLevel: Int,
    val attestationChallenge: ByteArray,
    val isDeviceLocked: Boolean,
    val verifiedBootState: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is KeyDescription)
            return attestationSecurityLevel == other.attestationSecurityLevel &&
                attestationChallenge.contentEquals(other.attestationChallenge) &&
                isDeviceLocked == other.isDeviceLocked &&
                verifiedBootState == other.verifiedBootState
        return false
    }

    override fun hashCode(): Int {
        var result = attestationSecurityLevel
        result = 31 * result + attestationChallenge.contentHashCode()
        result = 31 * result + isDeviceLocked.hashCode()
        result = 31 * result + verifiedBootState
        return result
    }
}

object KeyDescriptionDefaults {
    private const val UNKNOWN_LEVEL = -1
    private const val UNKNOWN_BOOT_STATE = -1

    val EMPTY = KeyDescription(
        attestationSecurityLevel = UNKNOWN_LEVEL,
        attestationChallenge = ByteArray(0),
        isDeviceLocked = false,
        verifiedBootState = UNKNOWN_BOOT_STATE,
    )
}

object Asn1Parser {

    private const val TAG_SEQUENCE = 0x30
    private const val TAG_OCTET_STRING = 0x04
    private const val LONG_FORM_LENGTH_MARKER = 0x80
    private const val LONG_FORM_TAG_MARKER = 0x1F
    private const val HIGH_TAG_CONTINUATION_BIT = 0x80
    private const val HIGH_TAG_VALUE_MASK = 0x7F

    private const val ATTESTATION_SECURITY_LEVEL_INDEX = 1
    private const val ATTESTATION_CHALLENGE_INDEX = 4
    private const val TEE_ENFORCED_INDEX = 7

    private const val ROOT_OF_TRUST_TAG = 704
    private const val ROOT_OF_TRUST_DEVICE_LOCKED_INDEX = 1
    private const val ROOT_OF_TRUST_VERIFIED_BOOT_STATE_INDEX = 2

    private const val UNKNOWN_LEVEL = -1
    private const val UNKNOWN_BOOT_STATE = -1
    private const val BYTE_MASK = 0xFF
    private const val LENGTH_LONG_FORM_MASK = 0x7F

    fun parseOctetString(raw: ByteArray): ByteArray {
        if (raw[0].toInt() and BYTE_MASK != TAG_OCTET_STRING)
            return raw
        val length = readLength(raw, 1)
        val contentOffset = 1 + length.second
        return raw.copyOfRange(contentOffset, contentOffset + length.first)
    }

    fun parseSequence(data: ByteArray): KeyDescription {
        if (data[0].toInt() and BYTE_MASK != TAG_SEQUENCE)
            return KeyDescriptionDefaults.EMPTY

        val sequenceLength = readLength(data, 1)
        val contentStart = 1 + sequenceLength.second
        val topLevelElements = parseTlvElements(data, contentStart, contentStart + sequenceLength.first)

        val attestationSecurityLevel = topLevelElements.getOrNull(ATTESTATION_SECURITY_LEVEL_INDEX)
            ?.let { readIntegerValue(it.value) } ?: UNKNOWN_LEVEL

        val attestationChallenge = topLevelElements.getOrNull(ATTESTATION_CHALLENGE_INDEX)
            ?.value ?: ByteArray(0)

        val teeEnforced = topLevelElements.getOrNull(TEE_ENFORCED_INDEX)
        val rootOfTrustData = if (teeEnforced != null)
            findRootOfTrustInAuthorizationList(teeEnforced.value)
        else
            null

        val rootElements = if (rootOfTrustData != null)
            parseSequenceContent(rootOfTrustData)
        else
            emptyList()

        val isDeviceLocked = rootElements.getOrNull(ROOT_OF_TRUST_DEVICE_LOCKED_INDEX)
            ?.let { it.value.isNotEmpty() && it.value[0].toInt() != 0 } ?: false

        val verifiedBootState = rootElements.getOrNull(ROOT_OF_TRUST_VERIFIED_BOOT_STATE_INDEX)
            ?.let { readIntegerValue(it.value) } ?: UNKNOWN_BOOT_STATE

        return KeyDescription(
            attestationSecurityLevel = attestationSecurityLevel,
            attestationChallenge = attestationChallenge,
            isDeviceLocked = isDeviceLocked,
            verifiedBootState = verifiedBootState,
        )
    }

    private fun findRootOfTrustInAuthorizationList(data: ByteArray): ByteArray? {
        val innerData = unwrapSequence(data)
        var offset = 0

        while (offset < innerData.size) {
            val tagResult = readTag(innerData, offset)
            offset = tagResult.nextOffset
            if (offset >= innerData.size)
                break

            val length = readLength(innerData, offset)
            offset += length.second
            val valueEnd = (offset + length.first).coerceAtMost(innerData.size)

            if (tagResult.tagNumber == ROOT_OF_TRUST_TAG)
                return innerData.copyOfRange(offset, valueEnd)

            offset = valueEnd
        }
        return null
    }

    private fun parseSequenceContent(data: ByteArray): List<TlvElement> {
        val innerData = unwrapSequence(data)
        return parseTlvElements(innerData, 0, innerData.size)
    }

    private fun unwrapSequence(data: ByteArray): ByteArray {
        if (data.isEmpty())
            return data
        if (data[0].toInt() and BYTE_MASK == TAG_SEQUENCE) {
            val length = readLength(data, 1)
            val contentStart = 1 + length.second
            return data.copyOfRange(contentStart, (contentStart + length.first).coerceAtMost(data.size))
        }
        return data
    }

    private fun parseTlvElements(
        data: ByteArray,
        start: Int,
        end: Int,
    ): List<TlvElement> {
        val elements = mutableListOf<TlvElement>()
        var offset = start
        while (offset < end) {
            val tagResult = readTag(data, offset)
            offset = tagResult.nextOffset
            if (offset >= end)
                break

            val length = readLength(data, offset)
            offset += length.second
            val valueEnd = (offset + length.first).coerceAtMost(end)
            val value = data.copyOfRange(offset, valueEnd)
            elements.add(TlvElement(tagResult.tagNumber, value))
            offset = valueEnd
        }
        return elements
    }

    private fun readTag(
        data: ByteArray,
        offset: Int,
    ): TagResult {
        val firstByte = data[offset].toInt() and BYTE_MASK
        val lowBits = firstByte and LONG_FORM_TAG_MARKER

        if (lowBits != LONG_FORM_TAG_MARKER)
            return TagResult(tagNumber = lowBits, nextOffset = offset + 1)

        var tagNumber = 0
        var currentOffset = offset + 1
        while (currentOffset < data.size) {
            val byte = data[currentOffset].toInt() and BYTE_MASK
            tagNumber = (tagNumber shl 7) or (byte and HIGH_TAG_VALUE_MASK)
            currentOffset++
            if (byte and HIGH_TAG_CONTINUATION_BIT == 0)
                break
        }
        return TagResult(tagNumber = tagNumber, nextOffset = currentOffset)
    }

    private fun readLength(
        data: ByteArray,
        offset: Int,
    ): Pair<Int, Int> {
        val firstByte = data[offset].toInt() and BYTE_MASK
        return if (firstByte < LONG_FORM_LENGTH_MARKER) {
            Pair(firstByte, 1)
        } else {
            val numberOfBytes = firstByte and LENGTH_LONG_FORM_MASK
            var length = 0
            for (index in 1..numberOfBytes) {
                length = (length shl 8) or (data[offset + index].toInt() and BYTE_MASK)
            }
            Pair(length, 1 + numberOfBytes)
        }
    }

    private fun readIntegerValue(data: ByteArray): Int {
        var result = 0
        for (byte in data) {
            result = (result shl 8) or (byte.toInt() and BYTE_MASK)
        }
        return result
    }

    private class TlvElement(
        val tag: Int,
        val value: ByteArray,
    )

    private class TagResult(
        val tagNumber: Int,
        val nextOffset: Int,
    )
}
