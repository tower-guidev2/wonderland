package org.alice.poc.airgap.detection

data class KeyDescription(
    val attestationSecurityLevel: Int,
    val attestationChallenge: ByteArray,
    val isDeviceLocked: Boolean,
    val verifiedBootState: Int,
)

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

    private const val ATTESTATION_SECURITY_LEVEL_INDEX = 1
    private const val ATTESTATION_CHALLENGE_INDEX = 4

    private const val ROOT_OF_TRUST_DEVICE_LOCKED_INDEX = 1
    private const val ROOT_OF_TRUST_VERIFIED_BOOT_STATE_INDEX = 2

    private const val CONTEXT_CLASS_MASK = 0xE0
    private const val CONTEXT_CLASS_VALUE = 0xA0
    private const val TAG_NUMBER_MASK = 0x1F
    private const val ROOT_OF_TRUST_MINIMUM_TAG = 4

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
        val elements = parseTlvElements(data, contentStart, contentStart + sequenceLength.first)

        val attestationSecurityLevel = elements.getOrNull(ATTESTATION_SECURITY_LEVEL_INDEX)
            ?.let { readIntegerValue(it.value) } ?: UNKNOWN_LEVEL

        val attestationChallenge = elements.getOrNull(ATTESTATION_CHALLENGE_INDEX)
            ?.value ?: ByteArray(0)

        val rootOfTrust = findRootOfTrust(elements)
        val rootElements = if (rootOfTrust != null)
            parseInnerSequenceElements(rootOfTrust)
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

    private fun findRootOfTrust(elements: List<TlvElement>): ByteArray? {
        for (element in elements) {
            val isContextClass = element.tag and CONTEXT_CLASS_MASK == CONTEXT_CLASS_VALUE
            val tagNumber = element.tag and TAG_NUMBER_MASK
            if (isContextClass && tagNumber >= ROOT_OF_TRUST_MINIMUM_TAG)
                return element.value
        }
        return null
    }

    private fun parseTlvElements(
        data: ByteArray,
        start: Int,
        end: Int,
    ): List<TlvElement> {
        val elements = mutableListOf<TlvElement>()
        var offset = start
        while (offset < end) {
            val tag = data[offset].toInt() and BYTE_MASK
            offset++
            val length = readLength(data, offset)
            offset += length.second
            val value = data.copyOfRange(offset, offset + length.first)
            elements.add(TlvElement(tag, value))
            offset += length.first
        }
        return elements
    }

    private fun parseInnerSequenceElements(data: ByteArray): List<TlvElement> {
        if (data.isEmpty())
            return emptyList()

        var contentStart = 0
        var contentEnd = data.size

        if (data[0].toInt() and BYTE_MASK == TAG_SEQUENCE) {
            val length = readLength(data, 1)
            contentStart = 1 + length.second
            contentEnd = contentStart + length.first
        }

        return parseTlvElements(data, contentStart, contentEnd.coerceAtMost(data.size))
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

    private data class TlvElement(
        val tag: Int,
        val value: ByteArray,
    )
}
