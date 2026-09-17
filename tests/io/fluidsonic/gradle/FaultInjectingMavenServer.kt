package io.fluidsonic.gradle

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * In-memory Maven repository that mimics the GitHub Packages behaviour the plugin's snapshot retry works around.
 *
 * `PUT` stores the body and answers `201`, but a second `PUT` of an already stored artifact path answers `409 Conflict`
 * without overwriting it — exactly like GitHub Packages. Only `maven-metadata.xml` and its checksum siblings may be
 * overwritten, since no snapshot could ever be published otherwise. Once, for the first `PUT` whose path matches
 * [faultPredicate], the file is stored but the response is `503`: the upload succeeded, only the response was "lost".
 *
 * `GET` answers `200` with the stored body or `404`; `HEAD` mirrors `GET` without a body. The one exception is the
 * versioned SNAPSHOT `maven-metadata.xml`: like GitHub Packages, the server generates it from the artifact files it has
 * stored in that directory (see [generatedSnapshotMetadata]) instead of serving whatever a client uploaded. That is what
 * lets a republish after a failed attempt see the partial build and take the next build number — Gradle writes its own
 * metadata only after all artifacts, so a stored-content server would report the failed build as nonexistent.
 *
 * Every request is recorded in [requests] as `(method, path, status)`.
 */
internal class FaultInjectingMavenServer(private val faultPredicate: (path: String) -> Boolean) : AutoCloseable {

	data class Request(val method: String, val path: String, val status: Int)

	private class StoredArtifact(val version: String, val timestamp: String, val buildNumber: Int, val classifier: String?, val extension: String)

	private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
	private val store = ConcurrentHashMap<String, ByteArray>()
	private val recordedRequests = CopyOnWriteArrayList<Request>()
	private val faultConsumed = AtomicBoolean(false)

	/** The path whose first `PUT` was answered with `503`, or `null` if the fault has not fired yet. */
	@Volatile
	var faultedPath: String? = null
		private set

	val requests: List<Request>
		get() = recordedRequests

	val storedPaths: Set<String>
		get() = store.keys

	val url: String
		get() = "http://127.0.0.1:${server.address.port}/"

	init {
		server.createContext("/") { exchange -> handle(exchange) }
		server.start()
	}

	override fun close() {
		server.stop(0)
	}

	/** The body a client uploaded to [path], if any — for a snapshot `maven-metadata.xml` that is Gradle's own upload. */
	fun storedText(path: String): String? = store[path]?.toString(Charsets.UTF_8)

	/** The response statuses of all requests for [path], in order. */
	fun statusesFor(path: String): List<Int> = recordedRequests.filter { it.path == path }.map { it.status }

	/** The `(method, status)` pairs of all requests for [path], in order. */
	fun requestsFor(path: String): List<Pair<String, Int>> = recordedRequests.filter { it.path == path }.map { it.method to it.status }

	/**
	 * The versioned SNAPSHOT `maven-metadata.xml` this server serves for [path], generated from the artifact files stored
	 * in the same directory the way GitHub Packages does it. Checksum files and uploaded metadata are ignored. Returns
	 * `null` if [path] is not a snapshot metadata path or no artifact has been stored yet.
	 */
	fun generatedSnapshotMetadata(path: String): String? {
		val directory = path.substringBeforeLast('/')
		val artifactId = directory.substringBeforeLast('/').substringAfterLast('/')
		val groupId = directory.substringBeforeLast('/').substringBeforeLast('/').trimStart('/').replace('/', '.')
		val artifacts = if (isSnapshotMetadata(path)) storedArtifactsIn(directory = directory, artifactId = artifactId) else emptyList()
		if (artifacts.isEmpty()) return null

		val latest = artifacts.maxBy { it.buildNumber }
		val lastUpdated = latest.timestamp.replace(".", "")

		return buildString {
			appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
			appendLine("<metadata modelVersion=\"1.1.0\">")
			appendLine("  <groupId>$groupId</groupId>")
			appendLine("  <artifactId>$artifactId</artifactId>")
			appendLine("  <version>${directory.substringAfterLast('/')}</version>")
			appendLine("  <versioning>")
			appendLine("    <snapshot>")
			appendLine("      <timestamp>${latest.timestamp}</timestamp>")
			appendLine("      <buildNumber>${latest.buildNumber}</buildNumber>")
			appendLine("    </snapshot>")
			appendLine("    <lastUpdated>$lastUpdated</lastUpdated>")
			appendLine("    <snapshotVersions>")
			for (artifact in artifacts) {
				appendLine("      <snapshotVersion>")
				if (artifact.classifier != null) {
					appendLine("        <classifier>${artifact.classifier}</classifier>")
				}
				appendLine("        <extension>${artifact.extension}</extension>")
				appendLine("        <value>${artifact.version}-${artifact.timestamp}-${artifact.buildNumber}</value>")
				appendLine("        <updated>${artifact.timestamp.replace(".", "")}</updated>")
				appendLine("      </snapshotVersion>")
			}
			appendLine("    </snapshotVersions>")
			appendLine("  </versioning>")
			appendLine("</metadata>")
		}
	}

	private fun handle(exchange: HttpExchange) {
		val path = exchange.requestURI.path
		val body = exchange.requestBody.use { it.readAllBytes() }

		val (status, responseBody) = when (exchange.requestMethod) {
			"PUT" -> put(path, body) to null
			"GET" -> get(path)?.let { HTTP_OK to it } ?: (HTTP_NOT_FOUND to null)
			"HEAD" -> (if (get(path) != null) HTTP_OK else HTTP_NOT_FOUND) to null
			else -> HTTP_METHOD_NOT_ALLOWED to null
		}

		recordedRequests += Request(method = exchange.requestMethod, path = path, status = status)

		if (responseBody == null) {
			exchange.sendResponseHeaders(status, -1)
		} else {
			exchange.sendResponseHeaders(status, responseBody.size.toLong())
			exchange.responseBody.use { it.write(responseBody) }
		}
		exchange.close()
	}

	private fun get(path: String): ByteArray? = if (isSnapshotMetadata(path)) generatedSnapshotMetadata(path)?.toByteArray(Charsets.UTF_8) else store[path]

	private fun put(path: String, body: ByteArray): Int {
		if (store.containsKey(path) && !isMetadata(path)) {
			return HTTP_CONFLICT
		}

		store[path] = body

		val isFaulted = faultPredicate(path) && faultConsumed.compareAndSet(false, true)
		if (isFaulted) {
			faultedPath = path
		}

		return if (isFaulted) HTTP_SERVICE_UNAVAILABLE else HTTP_CREATED
	}

	private fun isMetadata(path: String) = path.substringAfterLast('/').startsWith("maven-metadata.xml")

	private fun isSnapshotMetadata(path: String): Boolean {
		val directoryName = path.substringBeforeLast('/').substringAfterLast('/')

		return path.endsWith("/maven-metadata.xml") && directoryName.endsWith("-SNAPSHOT")
	}

	private fun storedArtifactsIn(directory: String, artifactId: String): List<StoredArtifact> = store.keys
		.filter { it.substringBeforeLast('/') == directory }
		.mapNotNull { parseArtifact(fileName = it.substringAfterLast('/'), artifactId = artifactId) }
		.sortedWith(compareBy({ it.buildNumber }, { it.classifier ?: "" }, { it.extension }))

	private fun parseArtifact(fileName: String, artifactId: String): StoredArtifact? {
		val isChecksumOrMetadata = CHECKSUM_EXTENSIONS.any { fileName.endsWith(it) } || fileName.startsWith("maven-metadata.xml")
		val regex = Regex("^${Regex.escape(artifactId)}-(.+?)-(\\d{8}\\.\\d{6})-(\\d+)(?:-([^.]+))?\\.(.+)$")
		val match = fileName.takeUnless { isChecksumOrMetadata }?.let { regex.matchEntire(it) } ?: return null

		return StoredArtifact(
			version = match.groupValues[1],
			timestamp = match.groupValues[2],
			buildNumber = match.groupValues[3].toInt(),
			classifier = match.groups[4]?.value,
			extension = match.groupValues[5],
		)
	}

	companion object {

		const val HTTP_CONFLICT = 409
		const val HTTP_CREATED = 201
		const val HTTP_METHOD_NOT_ALLOWED = 405
		const val HTTP_NOT_FOUND = 404
		const val HTTP_OK = 200
		const val HTTP_SERVICE_UNAVAILABLE = 503

		private val CHECKSUM_EXTENSIONS = listOf(".md5", ".sha1", ".sha256", ".sha512")
	}
}
