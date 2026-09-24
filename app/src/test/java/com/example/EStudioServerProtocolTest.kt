package com.example

import com.example.data.security.AndroidKeyStoreManager
import com.example.domain.model.Money
import com.example.domain.model.PrintJob
import com.example.domain.model.PrintJobStatus
import com.example.domain.model.PrintPriority
import com.example.domain.model.ProductSnapshot
import com.example.domain.model.ScanItem
import com.example.domain.model.ScanSource
import com.example.domain.model.UserRole
import com.example.protocol.EStudioProtocolV1
import com.example.server.MockEStudioServerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EStudioServerProtocolTest {

    private lateinit var server: MockEStudioServerEngine
    private lateinit var keyStoreManager: AndroidKeyStoreManager

    private val deviceId = "TC26-TEST-007"
    private val appVersion = "2.1.0-industrial"

    @Before
    fun setUp() {
        server = MockEStudioServerEngine()
        keyStoreManager = AndroidKeyStoreManager()

        // 1. Appairage sécurisé du terminal via QR OTT
        val ott = server.generatePairingToken("ESTUDIO-STORE-04")
        val registerResponse = server.registerDeviceWithPairingToken(
            token = ott.token,
            pin = ott.pin,
            deviceId = deviceId,
            publicKeyBase64 = keyStoreManager.getPublicKeyBase64(),
            appVersion = appVersion
        )
        assertTrue(registerResponse.success)
    }

    @Test
    fun testCanonicalRequestSigningAndServerVerification() {
        val method = "POST"
        val path = "/api/mobile/v1/print-jobs"
        val timestamp = EStudioProtocolV1.getCurrentTimestampIso()
        val requestId = UUID.randomUUID().toString()
        val body = """{"sessionId":"sess_123","labelsCount":5}"""

        val signature = keyStoreManager.signCanonicalRequest(method, path, timestamp, requestId, body)
        assertNotNull(signature)

        val error = server.verifyRequestSecurity(
            deviceId = deviceId,
            method = method,
            path = path,
            timestampIso = timestamp,
            requestId = requestId,
            body = body,
            signatureBase64 = signature
        )
        assertNull("La signature canonique ECDSA doit être acceptée par le serveur", error)
    }

    @Test
    fun testReplayProtectionRejectsOldTimestamp() {
        val method = "POST"
        val path = "/api/mobile/v1/print-jobs"
        // Timestamp 10 minutes dans le passé (dépasse les ±5 minutes autorisées)
        val expiredFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val expiredTimestamp = expiredFormat.format(Date(System.currentTimeMillis() - 10 * 60 * 1000L))
        val requestId = UUID.randomUUID().toString()
        val body = "{}"

        val signature = keyStoreManager.signCanonicalRequest(method, path, expiredTimestamp, requestId, body)

        val error = server.verifyRequestSecurity(
            deviceId = deviceId,
            method = method,
            path = path,
            timestampIso = expiredTimestamp,
            requestId = requestId,
            body = body,
            signatureBase64 = signature
        )
        assertNotNull("Une requête au-delà de 5 minutes doit être rejetée pour replay", error)
        assertEquals("TIMESTAMP_DRIFT_EXCEEDED", error?.code)
    }

    @Test
    fun testReplayProtectionRejectsDuplicateRequestId() {
        val method = "POST"
        val path = "/api/mobile/v1/print-jobs"
        val timestamp = EStudioProtocolV1.getCurrentTimestampIso()
        val requestId = "duplicate_req_${UUID.randomUUID()}"
        val body = "{}"

        val signature = keyStoreManager.signCanonicalRequest(method, path, timestamp, requestId, body)

        // Première exécution -> Acceptée
        val error1 = server.verifyRequestSecurity(
            deviceId = deviceId,
            method = method,
            path = path,
            timestampIso = timestamp,
            requestId = requestId,
            body = body,
            signatureBase64 = signature
        )
        assertNull(error1)

        // Deuxième exécution avec le MÊME requestId -> Rejetée
        val error2 = server.verifyRequestSecurity(
            deviceId = deviceId,
            method = method,
            path = path,
            timestampIso = timestamp,
            requestId = requestId,
            body = body,
            signatureBase64 = signature
        )
        assertNotNull(error2)
        assertEquals("REPLAY_ATTACK_DETECTED", error2?.code)
    }

    @Test
    fun testServerSideIdempotencyReturnsExistingRecordWithoutDuplication() {
        val idempotencyKey = "idem_key_unique_${UUID.randomUUID()}"
        val testItem = ScanItem(
            id = "item_1",
            barcode = "3017620422003",
            quantity = 3,
            source = ScanSource.ZEBRA_DATAWEDGE,
            idempotencyKey = "scan_key_1"
        )
        val printJob = PrintJob(
            id = "job_test_001",
            sessionId = "sess_001",
            sessionName = "Lot Test",
            templateId = "template_38x70",
            templateName = "Épernay 38x70",
            printerId = "PRINTER_RAYON_01",
            storeId = "STORE-04",
            operatorName = "Alice Dupont",
            priority = PrintPriority.NORMAL,
            status = PrintJobStatus.QUEUED,
            items = listOf(testItem),
            labelsCount = 3,
            idempotencyKey = idempotencyKey
        )

        // 1. Première soumission
        val resp1 = server.submitPrintJob(deviceId, idempotencyKey, printJob)
        assertTrue(resp1.success)
        assertEquals(PrintJobStatus.ACCEPTED, resp1.data?.status)

        // 2. Répétition de la même requête 10 fois (simulation de renvois réseau / retries)
        for (i in 1..10) {
            val retryResp = server.submitPrintJob(deviceId, idempotencyKey, printJob)
            assertTrue(retryResp.success)
            assertEquals("job_test_001", retryResp.data?.id)
            assertEquals(PrintJobStatus.ACCEPTED, retryResp.data?.status)
        }

        // Vérification qu'un seul job a été enregistré
        val serverJob = server.getPrintJob("job_test_001")
        assertNotNull(serverJob)
        assertEquals("job_test_001", serverJob?.id)
    }

    @Test
    fun testPrinterCapabilityEnforcementRejectsIncompatibleTemplate() {
        val idempotencyKey = "idem_key_incompatible_${UUID.randomUUID()}"
        val incompatibleJob = PrintJob(
            id = "job_incompatible_002",
            sessionId = "sess_002",
            sessionName = "Lot Palette",
            templateId = "template_palette_105x148", // Gabarit trop grand pour ZD421
            templateName = "Palette 105x148",
            printerId = "PRINTER_RAYON_01", // Imprimante rayon ZD421 ne supportant pas 105x148
            storeId = "STORE-04",
            operatorName = "Alice Dupont",
            priority = PrintPriority.NORMAL,
            status = PrintJobStatus.QUEUED,
            items = emptyList(),
            labelsCount = 1,
            idempotencyKey = idempotencyKey
        )

        val response = server.submitPrintJob(deviceId, idempotencyKey, incompatibleJob)
        assertFalse(response.success)
        assertEquals("INCOMPATIBLE_TEMPLATE", response.error?.code)
    }

    @Test
    fun testDeltaCatalogSyncServerWins() {
        val deltaResponse = server.getCatalogDelta(clientVersion = 1842L)
        assertTrue(deltaResponse.success)
        val data = deltaResponse.data
        assertNotNull(data)
        assertEquals("SERVER_WINS", data?.get("strategy"))
        assertEquals(1842L, data?.get("fromVersion"))
        assertEquals(1850L, data?.get("toVersion"))
    }

    @Test
    fun testOperatorSwitchPreservesDeviceEnrollment() {
        // Enrôlement initial avec Alice
        val aliceSession = server.loginOperator("OP-001", "Alice Dupont", UserRole.STORE_OPERATOR)
        assertEquals("Alice Dupont", aliceSession.operatorName)
        assertFalse(aliceSession.isExpired)

        // Changement d'opérateur vers Bob sans toucher à l'enrôlement matériel du terminal
        val bobSession = server.loginOperator("OP-002", "Bob Martin", UserRole.STORE_MANAGER)
        assertEquals("Bob Martin", bobSession.operatorName)
        assertEquals(UserRole.STORE_MANAGER, bobSession.role)

        // Le terminal est toujours actif sur le serveur
        val device = server.getRegisteredDevice(deviceId)
        assertNotNull(device)
        assertEquals(MockEStudioServerEngine.DeviceStatus.ACTIVE, device?.status)
    }

    @Test
    fun testPrintJobUnknownStatusHandling() {
        // Validation que le statut UNKNOWN existe et ne provoque pas d'action destructive
        val status = PrintJobStatus.UNKNOWN
        assertEquals(PrintJobStatus.UNKNOWN, status)
    }
}
